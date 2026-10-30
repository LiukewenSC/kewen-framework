# Proposal

## Why

当前异常出口把不同性质的失败混在一起：匿名访问受保护资源被 `InsufficientAuthenticationException` 记为 ERROR 级“请求的权限不足，需要提升”并打印整段堆栈；账号密码错误返回 `code=403` 与英文 `Bad credentials`；无权限（`AccessDeniedException`）反而返回 `code=401`；校验失败、业务失败、数据库失败与未预期异常都落在 `500`。调用方无法只凭响应判断问题属于认证、授权、业务还是数据库，排查时也只能靠读堆栈区分。

## What Changes

- 新增统一的错误分类枚举 `ErrorType`：`AUTHENTICATION`、`AUTHORIZATION`、`VALIDATION`、`BUSINESS`、`USAGE`、`DATABASE`、`SYSTEM`，每个分类固定对应的 HTTP 状态与 `code`。
- 统一响应 `Result` 增加 `errorType` 字段（失败时出现，成功时省略）；`TraceResult` 复制该字段，过期会话等直接写响应的出口也补齐分类。
- 修正语义错位：未认证/凭据错误 → `401 AUTHENTICATION`，无权限 → `403 AUTHORIZATION`，参数校验/请求体格式/类型转换 → `400 VALIDATION`，业务规则失败 → `400 BUSINESS`，调用方式错误 → `400 USAGE`，数据库失败 → `500 DATABASE`，未预期异常 → `500 SYSTEM`。**BREAKING**：现有 `code` 与 HTTP 状态取值会改变（认证失败 403→401、无权限 401→403、校验 HTTP 200+500→HTTP 400+400、业务 500→400）。
- 重写三类异常 advice 的映射与文案：错误凭据统一为“用户名或密码错误”；匿名访问改为“未登录或登录状态已失效”；不再把认证类失败写成“权限不足”。
- 日志按分类输出：预期失败（认证、授权、校验、业务、用法）单行 INFO/WARN、不带堆栈；服务端故障（数据库、系统）ERROR 并保留堆栈；每行带分类标签、请求方法与路径。
- 数据库异常单独出口：`DataAccessException` 返回通用提示，SQL、表名、连接信息只进日志；登录过程中被 `InternalAuthenticationServiceException` 包裹的数据库异常也归入 `DATABASE`。
- 无解析器时的兜底出口由纯文本改为与统一响应一致的 JSON（`SYSTEM`），不再把异常原文直接写给调用方。
- 顺带修复参数校验消息末尾被多截两个字符的已知缺陷，并同步更新示例、README、测试与相关规格。

## Capabilities

### New Capabilities
- `basic/error-taxonomy`: 错误分类集合、异常归属、HTTP 状态与 `code` 映射、日志分级与脱敏、所有异常出口（含兜底）的输出约定。

### Modified Capabilities
- `basic/request-response-contract`: 失败信封新增 `errorType`；校验/请求体解析/类型转换失败改为 HTTP 400 + `code` 400；未预期异常返回通用提示而非异常原文；修正校验消息截断缺陷。
- `identity/login-outcome-posture`: 账号密码认证失败的响应改为 `401 AUTHENTICATION` 与固定中文提示；单点登录失败仍按回跳目标重定向，不受影响。

## Impact

- `basic`：`Result`、`TraceResult`、新增 `ErrorType`、`ExceptionConstant`、`GlobalExceptionHandlerAdvice`、`pom.xml`（新增 `spring-tx` 以处理 `DataAccessException`）。
- `auth/auth-core-spring-boot-starter`：`AuthExceptionHandler`（403 授权、400 用法错误，补 `errorType` 与 HTTP 状态）。
- `boot/login-security-spring-boot-starter`：`AuthenticationExceptionAdviceHandler`、`SessionConfig`、`SecurityAuthenticationExceptionResolverHandler`、`WebSecurityGlobalExceptionHandlerFilter`、README。
- 测试与示例：`AdviceOrderTest`、`ValidatedTestControllerTest`、登录/基础示例的前端与验证脚本、相关 README。
- 对外契约：所有依赖 `code` 或 HTTP 状态做分支的调用方需要按新映射调整；成功响应结构不变。
