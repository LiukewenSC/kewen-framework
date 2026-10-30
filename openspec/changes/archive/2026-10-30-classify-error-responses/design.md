# Design

## Context

现状由三个 advice 分担异常出口：`AuthenticationExceptionAdviceHandler`（登录安全，最高优先级）、`AuthExceptionHandler`（权限框架）、`GlobalExceptionHandlerAdvice`（基础兜底，最低优先级）。三处各自硬编码状态码与文案，导致同一语义在不同入口不一致：

| 异常场景 | 现在 HTTP | 现在 code | 现在文案 |
| --- | --- | --- | --- |
| 匿名访问受保护资源 | 200 | 401 | 请求的权限不足，需要提升，请重新登录 |
| 账号密码错误 | 403 | 403 | `Bad credentials` |
| 已认证但无权限 | 200 | 401 | Access Denied |
| 参数校验失败 | 200 | 500 | 字段拼接（末尾被截断） |
| 业务异常 | 200 | 500 | 业务 message |
| 数据库异常 | 500 | 500 | 底层异常原文 |
| 未预期异常 | 500 | 500 | 底层异常原文 |

另有两处绕过 advice 的出口：会话过期策略直接写 JSON；安全过滤器在无解析器时写纯文本。`TraceResponseBodyAdvice` 会把 advice 返回的 `Result` 包装成 `TraceResult`，新增字段必须随复制逻辑保留。`basic` 模块当前没有 `spring-tx` 依赖，但 `org.springframework.dao.DataAccessException` 位于该包。

相关规格见 `specs/basic/error-taxonomy/spec.md`、`specs/basic/request-response-contract/spec.md`、`specs/identity/login-outcome-posture/spec.md`。

## Goals / Non-Goals

**Goals:**

- 用单一 `ErrorType` 枚举承载“分类 → HTTP 状态 / code / 日志级别 / 堆栈策略”的唯一事实来源。
- 三个 advice、会话过期出口、两个兜底出口全部使用同一分类与同一信封。
- 认证、授权、参数、业务、用法、数据库、系统七类失败在响应与日志上都能被直接识别。
- 保持成功信封、`traceId`、SSO 重定向姿态不变。

**Non-Goals:**

- 不引入业务模块级自定义错误码体系，不改 `BizException`/`BackendException` 的继承结构。
- 不做国际化；文案固定为中文。
- 不改认证协议流程、单点登录回跳规则、`NoLoginException` 对“认证主体类型不匹配”的既有处理。
- 不重构 `Result` 的既有工厂方法签名。

## Decisions

### 1. `ErrorType` 枚举放在 basic，`code` 直接等于 HTTP 状态

新增 `com.cu.framework.basic.exception.ErrorType`：

| 枚举值 | HTTP | code | 典型来源 |
| --- | --- | --- | --- |
| `AUTHENTICATION` | 401 | 401 | 未登录、凭据错误、账号状态异常、会话失效 |
| `AUTHORIZATION` | 403 | 403 | 已认证后的权限拒绝 |
| `VALIDATION` | 400 | 400 | 参数校验、绑定、解析、类型转换失败 |
| `BUSINESS` | 400 | 400 | `BizException` |
| `USAGE` | 400 | 400 | 权限框架无法判定调用方式（如未提供业务数据 ID） |
| `DATABASE` | 500 | 500 | 数据库访问失败（含被认证流程包装的） |
| `SYSTEM` | 500 | 500 | 空指针及其余未处理异常 |

`code` 不再单独设计数值段。理由：现有调用方已经把 `code` 当作 HTTP 同义码使用；新增 `40101` 一类段位会同时破坏旧值又增加一层映射。分类的区分职责交给 `errorType`，`code` 只承担粗粒度分档。

备选方案：只加 `errorType`、保留旧 `code`（未采纳，用户已确认要修正映射）；用 `4000/5000` 段位（未采纳，兼容成本更高）。

### 2. `errorType` 只出现在失败信封

`Result` 增加字段：

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
protected String errorType;
```

全局 Jackson 配置是 `Include.ALWAYS`，字段级 `NON_NULL` 会覆盖全局配置，因此成功响应不出现 `errorType`，失败响应才出现。字段存分类名而不是枚举：fastjson 直接把枚举序列化成序号（`errorType:6`），存 `ErrorType.name()` 可让 Jackson 与 fastjson 输出一致。新增工厂方法 `Result.failed(ErrorType, String)` 同时设置 `code` 与 `errorType`。既有 `failed(Integer, String)`、`failed(String)` 保留给外部调用方，框架内部出口不再使用。

`TraceResult.of` 必须复制 `errorType`，否则响应 advice 包装后字段丢失。fastjson 直接序列化 `Result` 的出口（会话过期、兜底）同样会输出枚举名。

备选方案：新增独立 `ErrorResult` 类型（未采纳，调用方只认一个信封）；把 `errorType` 做成字符串常量（未采纳，枚举能在编译期约束取值）。

### 3. 各 advice 保持分域，只统一映射与日志

不合并三个 advice：Spring Security 异常需要在过滤器链出口尽早拦截，权限框架异常有独立优先级，合并会破坏既有 `@Order` 契约与 starter 独立性。改动限定在方法内部：

- `AuthenticationExceptionAdviceHandler`
  - `NoLoginException` / `InsufficientAuthenticationException` → 401 `AUTHENTICATION`，文案“未登录或登录状态已失效，请重新登录”，INFO 单行。
  - `BadCredentialsException` → 401 `AUTHENTICATION`，“用户名或密码错误”，WARN 单行。
  - `DisabledException` / `LockedException` / `AccountExpiredException` / `CredentialsExpiredException` → 401 `AUTHENTICATION`，各自状态文案，WARN 单行。
  - 其余 `AuthenticationException` → 401 `AUTHENTICATION`，“认证失败，请重试”，WARN 单行。
  - `AuthenticationServiceException`（过滤器侧的请求方式/解析错误）→ 400 `USAGE`，保留其原始中文 message，WARN 单行。
  - `AccessDeniedException` → 403 `AUTHORIZATION`，“没有访问该资源的权限”，WARN 单行。
  - `InternalAuthenticationServiceException` → 沿 cause 链判断：命中数据库访问异常则 500 `DATABASE` + “数据库操作失败，请稍后重试”，否则 500 `SYSTEM` + “认证服务内部错误，请稍后重试”；ERROR 带堆栈。
  - 每个方法补 `@ResponseStatus`，让 HTTP 状态与 `code` 一致。
- `AuthExceptionHandler`
  - `AuthException` → 403 `AUTHORIZATION`，保留业务拒绝 message，WARN 单行。
  - `AuthServiceException` → 400 `USAGE`，保留 message，WARN 单行。
- `GlobalExceptionHandlerAdvice`
  - `BizException` → 400 `BUSINESS`，WARN 单行。
  - `BindException`（覆盖 `MethodArgumentNotValidException`）→ 400 `VALIDATION`，字段错误按 `[字段]提示` 用 `;` 连接，去掉末尾 `substring`，无字段错误时回落到“参数校验失败”。
  - `HttpMessageNotReadableException` → 400 `VALIDATION`，“请求体格式不正确”。
  - `MethodArgumentTypeMismatchException` → 400 `VALIDATION`，“参数[名称]格式不正确”。
  - `MissingServletRequestParameterException` → 400 `VALIDATION`，“缺少必需参数[名称]”。
  - `DataAccessException` → 500 `DATABASE`，通用文案，ERROR 带堆栈。
  - `Throwable` → 500 `SYSTEM`；若 cause 链含数据库访问异常则归 `DATABASE`。删除独立的 `NullPointerException` 处理器，让空指针回到 `SYSTEM`。

异常方法的签名增加 `HttpServletRequest` 参数，用于日志中的方法与路径。

### 4. 日志统一由小工具输出

新增 `com.cu.framework.basic.exception.ErrorLog`，提供 `info/warn/error(ErrorType, HttpServletRequest, String detail[, Throwable])`。格式固定为：

```
[AUTHENTICATION] GET /currentUser - 未登录或登录状态已失效
```

- INFO：匿名访问、会话失效。
- WARN：凭据错误、账号状态、权限拒绝、参数校验、业务失败、用法错误；不打印堆栈。
- ERROR：数据库与系统故障；打印完整堆栈。
- 不输出密码、令牌、请求体原文；异常 message 可进日志，但响应只给通用文案。

备选方案：各 handler 直接拼日志（未采纳，格式容易再次分叉）；用 AOP 统一记录（未采纳，异常已经被 advice 拦截，多一层切面只会增加定位成本）。

### 5. 数据库异常用 cause 链识别

`DataAccessException` 直接命中专门 handler；被 `InternalAuthenticationServiceException` 或其他包装异常包住的数据库失败，沿 `getCause()` 逐层判断。这样登录查库失败不会被误报成“认证失败”，业务层被 MyBatis 包装的失败也不会落进 `SYSTEM`。

`basic` 增加 `org.springframework:spring-tx` 依赖以引用 `DataAccessException`，版本由 `spring-boot-dependencies` 管理。该依赖只增加一个编译期类引用，使用方已有 MyBatis/JDBC 时本来就在 classpath 上。

### 6. 兜底出口改为统一信封

- `SecurityAuthenticationExceptionResolverHandler` 与 `WebSecurityGlobalExceptionHandlerFilter` 在无解析器时，不再写纯文本，改为写 `Result.failed(ErrorType.SYSTEM, "系统内部错误，请稍后重试")` 的 JSON，并打 ERROR 日志。
- `SessionConfig` 的过期会话响应改为 `Result.failed(ErrorType.AUTHENTICATION, "登录状态已失效，请重新登录")`，HTTP 401 不变。
- SSO 失败重定向分支保持原样，不写响应体。

### 7. 兼容处理

`ExceptionConstant.BIZ_FAILED` / `PARAM_VALID_FAILED` 保留但值改为 400，避免仍引用常量的外部代码返回旧的 500 语义；框架内部改用 `ErrorType`。`Result.failed(Integer, String)` 暂不删除，减少外部编译破坏。

## Risks / Trade-offs

- [调用方按旧 401/403 分支] → 变更已在 proposal 标记 BREAKING；仓库内示例前端本身只按 `success`/`message` 判断，不受影响；README 与迁移说明同步更新。
- [校验失败从 HTTP 200 改为 400] → 仓库内 JS 不看 HTTP 状态；外部调用方需要按 `errorType` 或 `code` 调整。迁移说明中明确。
- [字段级 `@JsonInclude(NON_NULL)` 可能被全局 `ALWAYS` 覆盖] → 增加序列化测试，断言成功响应无 `errorType`、失败响应有。
- [fastjson 与 Jackson 对枚举的输出差异] → 字段存分类名称字符串，两个序列化器输出一致；兜底出口增加断言。
- [INFO 级匿名访问日志过静] → 这是为了消除登录页每次加载 `/currentUser` 产生的 ERROR 堆栈噪声；需要时可按 logger 名称调高级别。
- [cause 链遍历遇到自引用] → 以 `cause != t` 作为循环条件，避免异常链成环时死循环。
- [新增 `spring-tx` 依赖] → 版本由父 BOM 管理，无传递冲突；纯 Web 使用方会多一个轻量 jar。
- [原有 `AdviceOrderTest` 与示例测试断言旧码] → 作为任务的一部分同步更新，避免测试与规格脱节。

## Migration Plan

1. 先在 `basic` 落地 `ErrorType`、`Result` 字段、`ErrorLog` 与全局 advice，再改 `auth-core` 与登录 starter；编译顺序由 Maven 模块依赖保证。
2. 更新示例与 README 后运行：`mvn -q -DskipTests install`（或按仓库既有构建方式），再运行 `basic-boot-sample`、`auth-rbac-security-boot-sample` 的测试与 `verify.sh`。
3. 手工验证：错误密码登录、匿名 `/currentUser`、无权限端点、校验失败、会话过期、SSO 失败回跳。
4. 回滚：还原本次代码即可恢复旧映射；无数据库结构或持久化数据变更。

## Open Questions

无。分类集合、映射、日志级别与数据库脱敏口径已由用户确认。
