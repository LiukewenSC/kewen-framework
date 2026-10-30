# Tasks

## 1. 基础错误模型

- [x] 1.1 在 `basic` 新增 `ErrorType` 枚举（七个分类、HTTP 状态、服务端故障标记、沿 cause 链识别数据库异常）与 `ErrorLog` 工具；类注释按仓库约定写 `@author kewen` / `@since 2026-10-30`。验证：`mvn -q -pl basic -am compile` 通过，且新增 `ErrorTypeTest` 覆盖七个映射与“包装后的数据库异常”识别。
- [x] 1.2 `Result` 增加 `errorType` 字段与 `failed(ErrorType, String)` 工厂，字段使用 `@JsonInclude(NON_NULL)`；`TraceResult.of` 复制该字段。验证：新增 `ResultErrorTypeTest`，断言成功响应不含 `errorType`、失败响应含分类、`TraceResult.of` 不丢分类。
- [x] 1.3 `basic/pom.xml` 增加 `org.springframework:spring-tx` 依赖以引用数据库异常类型，版本交由父 BOM 管理。验证：`mvn -q -pl basic test` 通过。
- [x] 1.4 更新 `ExceptionConstant` 的 `BIZ_FAILED` / `PARAM_VALID_FAILED` 为 400，并同步 `basic/README.md` 的统一响应与异常章节（成功无 `errorType`、失败带分类、工厂方法示例）。验证：README 示例与现有工厂方法一致，`mvn -q -pl basic test` 通过。

## 2. 全局异常出口

- [x] 2.1 改造 `GlobalExceptionHandlerAdvice`：`BizException` → 400 `BUSINESS`；`BindException` 覆盖参数校验并修复消息末尾截断；`HttpMessageNotReadableException`、`MethodArgumentTypeMismatchException`、`MissingServletRequestParameterException` → 400 `VALIDATION`；`DataAccessException` → 500 `DATABASE`；`Throwable` → 500 `SYSTEM`（cause 链含数据库异常时归 `DATABASE`）；删除独立的空指针处理器；每个方法补 `@ResponseStatus` 并用 `ErrorLog` 记录。验证：新增 `GlobalExceptionHandlerAdviceTest` 覆盖七类映射、校验消息完整性与数据库异常通用文案，`mvn -q -pl basic test` 通过。
- [x] 2.2 在 `basic/README.md` 的异常章节记录新的映射表与日志分级，并注明数据库异常只返回通用提示。验证：文档映射与已实现的 advice 逐项一致。

## 3. 登录安全异常出口

- [x] 3.1 改造 `AuthenticationExceptionAdviceHandler`：匿名/会话失效 401 `AUTHENTICATION`（INFO 单行）、凭据错误与账号状态 401 `AUTHENTICATION`（WARN 单行）、`AuthenticationServiceException` 400 `USAGE`、`AccessDeniedException` 403 `AUTHORIZATION`、`InternalAuthenticationServiceException` 按 cause 链归 `DATABASE` 或 `SYSTEM`（ERROR 带堆栈）；补 `@ResponseStatus` 与 `ErrorLog`。验证：`mvn -q -pl boot/login-security-spring-boot-starter -am test` 通过，新增 `AuthenticationExceptionAdviceHandlerTest` 逐类断言 `code`、`errorType`、提示文案与 HTTP 状态注解。
- [x] 3.2 `SessionConfig` 的过期会话响应改为带 `AUTHENTICATION` 分类的统一信封，保留 HTTP 401 与“登录状态已失效，请重新登录”。验证：`SessionConfigTest` 直接调用过期响应写出，断言 HTTP 401、`errorType=AUTHENTICATION`、固定提示且无堆栈文本；示例的并发会话控制在当前实现下未触发该策略（既有行为，不在本变更范围）。
- [x] 3.3 `SecurityAuthenticationExceptionResolverHandler` 与 `WebSecurityGlobalExceptionHandlerFilter` 在无解析器时改为输出 `SYSTEM` 分类的 JSON 信封，异常原文只进日志。验证：在现有解析器测试中补“解析器返回 null”用例，断言响应为 JSON 且含 `errorType=SYSTEM`；`mvn -q -pl boot/login-security-spring-boot-starter -am test` 通过。
- [x] 3.4 更新 `boot/login-security-spring-boot-starter/README.md` 的响应示例与“不能只看 HTTP 状态码”的说明：未登录现在是 HTTP 401 + `errorType=AUTHENTICATION`，账号密码错误为 401 + “用户名或密码错误”。验证：README 表格与实际响应逐项一致。

## 4. 权限框架异常出口

- [x] 4.1 改造 `AuthExceptionHandler`：`AuthException` → 403 `AUTHORIZATION`，`AuthServiceException` → 400 `USAGE`，补 `@ResponseStatus` 与 `ErrorLog` 单行日志。验证：`mvn -q -pl auth/auth-core-spring-boot-starter -am test` 通过。
- [x] 4.2 更新 `AdviceOrderTest`：权限拒绝为 403、用法错误为 400、未登录为 401、无权限为 403、业务异常为 400，并断言各自 `errorType`；补充“授权与用法错误分类不同”的用例。验证：`mvn -q -pl auth/auth-rbac-security-spring-boot-starter -am test` 通过。
- [x] 4.3 更新 `auth/auth-core-spring-boot-starter/README.md`，说明 403/400 的 `errorType` 与 HTTP 状态已对齐。验证：README 与实际响应一致。

## 5. 示例、测试与文档对齐

- [x] 5.1 更新 `ValidatedTestControllerTest`：期望 HTTP 400、`code` 400、`errorType=VALIDATION`，单字段消息为完整的 `[age]年龄不能为空`。验证：`mvn -q -pl sample/basic-boot-sample -am test` 通过。
- [x] 5.2 更新 `JacksonSerializerControllerTest` 中查询参数与请求体日期格式不符的断言：HTTP 400、`code` 400、`errorType=VALIDATION`。验证：上述测试通过。
- [x] 5.3 更新样例 README 与页面说明：`sample/auth-rbac-security-boot-sample/README.md`（权限拒绝不再是 HTTP 200）、`sample/auth-core-boot-sample/README.md`、`sample/auth-rbac-boot-sample/README.md`，并检查前端脚本没有依赖旧的 401/403 映射。验证：`grep` 不再出现“HTTP 200 + code 401/403”一类旧描述，样例页面可正常展示新提示。

## 6. 集成验证

- [x] 6.1 运行全量构建与测试（至少 `mvn -q -DskipTests install` 后对 basic、login-security、auth-core、auth-rbac-security、两个示例执行测试），全部通过。验证：`mvn -q -DskipTests install` 与最终 `mvn -q test` 均成功。
- [x] 6.2 启动 `auth-rbac-security-boot-sample`，手工验证：错误密码 → 401 + `AUTHENTICATION` + “用户名或密码错误”；匿名 `/currentUser` → 401 + `AUTHENTICATION` 且日志为单行 INFO 无堆栈；无权限端点 → 403 + `AUTHORIZATION`；会话失效 → 401 + `AUTHENTICATION`；单点登录失败仍按回跳目标重定向。验证：8084 实例实测前三项；会话过期写出由 `SessionConfigTest` 覆盖；SSO 失败重定向由 `LoginDeliveryPostureTest.failureOnSingleSignOnCallbackRedirects` 覆盖。
- [x] 6.3 运行 `sample/auth-core-boot-sample/verify.sh` 与 `sample/auth-rbac-security-boot-sample/verify.sh`，确认 403/400 用例全部通过。验证：32/32 与 70/70 通过。
- [x] 6.4 检查一次完整登录失败与数据库故障日志：预期失败只出现带分类标签的单行 WARN/INFO，数据库故障出现带 `[DATABASE]` 的 ERROR 堆栈，日志中不含密码或会话令牌。验证：8084 日志中预期失败为单行且无 ERROR，密码与会话标识计数为 0；`GlobalExceptionHandlerAdviceTest` 输出 `[DATABASE]` ERROR 与完整堆栈。
