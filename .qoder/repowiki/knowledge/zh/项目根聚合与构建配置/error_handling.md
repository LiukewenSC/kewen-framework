## 1. 采用的方案

该项目使用 Spring MVC 的 `@RestControllerAdvice` + `@ExceptionHandler` 作为统一的 Web 异常入口，通过自定义异常类型、错误码常量与标准响应体 `Result<T>` 实现「业务异常 → HTTP JSON」的转换。同时借助 `Ordered`/`@Order` 在多个 starter 之间形成优先级链，让安全、认证、权限等模块可以抢占式地优先处理各自领域的异常。

## 2. 关键文件与包

- 基础异常模型：`basic/src/main/java/com/cu/framework/basic/exception/`
  - `BackendException.java` — `RuntimeException` 子类，默认携带 `code = 500`，是所有后端异常的根类型。
  - `BizException.java` — 继承 `BackendException`，表示业务校验失败，由业务代码主动抛出。
  - `ExceptionConstant.java` — 集中定义全局错误码常量（当前为 `BIZ_FAILED = 500`、`PARAM_VALID_FAILED = 500`）。
- 全局统一处理器：`basic/src/main/java/com/cu/framework/basic/exception/GlobalExceptionHandlerAdvice.java`
- 标准响应体：`basic/src/main/java/com/cu/framework/basic/model/Result.java`
- 权限框架异常处理器：`auth/auth-core-spring-boot-starter/src/main/java/com/cu/framework/boot/auth/core/AuthExceptionHandler.java`
- 登录/认证异常处理器：`boot/login-security-spring-boot-starter/src/main/java/com/cu/framework/authentication/handler/AuthenticationExceptionAdviceHandler.java`

## 3. 架构与约定

### 3.1 异常类型分层

```
RuntimeException
 └── BackendException (code=500)
      └── BizException
```

- 所有业务抛出的可预期失败应封装为 `BizException`，携带人类可读的 message；`BackendException` 提供可扩展的 `code` 字段，但当前仅 `BizException` 直接复用父类默认值 500。
- 未预期的系统级错误由全局兜底处理器捕获。

### 3.2 全局处理器 `GlobalExceptionHandlerAdvice`

位于 `basic` 模块，`@Order(Ordered.LOWEST_PRECEDENCE)`，是最低优先级的兜底处理器，负责：

| 被捕获异常 | 行为 | 返回结果 |
|---|---|---|
| `BizException` | 记录 `log.error("bizException >> ", e)` | `Result.failed(BIZ_FAILED, message)` |
| `NullPointerException` | 按业务异常处理，记录日志 | `Result.failed(BIZ_FAILED, message)` |
| `MethodArgumentNotValidException` | 拼接所有 `FieldError` 为 `[field]message;...` 格式 | `Result.failed(PARAM_VALID_FAILED, message)` |
| `Throwable` | 兜底，`@ResponseStatus` 无参即 500 | `Result.failed(e.getMessage())` |

该处理器不处理通用 `Throwable` 以外的其他 Spring Security / 认证异常，以免抢占更高优先级的处理器。

### 3.3 各 Starter 的抢占式处理器

每个功能模块通过独立的 `@RestControllerAdvice` + `@Order` 抢占自身领域异常：

- `AuthExceptionHandler`（权限模块）：`@Order(HIGHEST_PRECEDENCE + 100)`
  - `AuthException` → code 403
  - `AuthServiceException` → code 400（注释明确说明区分“无权”和“无法判定调用方式”）
- `AuthenticationExceptionAdviceHandler`（登录安全模块）：`@Order(HIGHEST_PRECEDENCE + 200)`，优先级最高
  - `AccessDeniedException` → code 401
  - `NoLoginException` → HTTP 401 + code 401
  - `AuthenticationException` → HTTP 403 + code 403
  - `InternalAuthenticationServiceException` → code 500
  - `InsufficientAuthenticationException` → code 401 + 固定提示“请求的权限不足，需要提升，请重新登录”

### 3.4 统一响应体 `Result<T>`

- 成功：`Result.success()` / `Result.success(data)`，固定 `code=200`、`success=true`、`message="成功"`。
- 失败：`Result.failed(code, message)` 或 `Result.failed(message)`（默认 code=500），`success=false`。
- 所有 `@ExceptionHandler` 最终都返回 `Result<Void>`，保证前端契约一致。

### 3.5 异常传播策略

- 业务层：显式 `throw new BizException(...)`，禁止吞掉 checked exception 后以空字符串 message 继续抛出。
- 参数校验层：依赖 Hibernate Validator / JSR-303，由 `MethodArgumentNotValidException` 统一收集并格式化。
- 安全层：Spring Security 相关异常由 `AuthenticationExceptionAdviceHandler` 拦截，不向外泄漏堆栈。
- 兜底层：`GlobalExceptionHandlerAdvice` 捕获 `Throwable`，记录完整堆栈后返回 code 500，避免浏览器裸 HTML 500。

## 4. 观察到的约定与约束

- **所有 Web 端失败必须经 `@RestControllerAdvice` 转为 `Result`**：各 Handler 中均使用 `Result.failed(...)`，未在 Controller 中直接返回裸错误对象。
- **异常处理器通过 `@Order` 严格排序**：认证 > 权限 > 全局，新增模块若需拦截异常应在对应 starter 中注册新的 `@RestControllerAdvice` 并设置合适的 `@Order`，不要修改已有顺序。
- **错误码来源**：业务失败走 `ExceptionConstant.BIZ_FAILED`，参数校验失败走 `ExceptionConstant.PARAM_VALID_FAILED`；两者当前值均为 500，后续扩展应区分语义。
- **安全异常映射表**：401 用于“未登录/鉴权不足”，403 用于“拒绝访问/认证失败”，400 用于“调用方式错误”，500 用于内部服务异常——该映射在 `AuthenticationExceptionAdviceHandler` 中有明确注释说明。
- **日志规范**：所有 `@ExceptionHandler` 均以 `logger.error(...)` 记录完整异常堆栈，便于线上排查。
- **`BackendException.code` 字段尚未被广泛使用**：当前只有 `BackendException` 默认 500，`BizException` 并未覆盖该字段，因此实际错误码主要由 `ExceptionConstant` 与各个 Handler 硬编码决定；新增业务异常码应优先在 `ExceptionConstant` 中声明而非直接传数字。
- **未使用 `@ResponseStatus` 指定具体 HTTP 状态码**：除 `AuthenticationExceptionAdviceHandler` 中的几个 handler 外，多数 handler 依赖 `Result.code` 传递业务码，HTTP 状态码多为 200 或 500（兜底）。这是当前实现的事实，不是强制约束。