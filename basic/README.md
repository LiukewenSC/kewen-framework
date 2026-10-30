# basic

`basic` 是框架的基础库：统一响应模型、异常基类、请求追踪与请求日志、通用工具类。它是库模块，不是 starter；自动装配与过滤器注册在 [basic-spring-boot-starter](../boot/basic-spring-boot-starter/README.md)。

## 包结构

| 包 | 内容 |
| --- | --- |
| `com.cu.framework.basic.model` | `Result`、`PageResult`、`PageReq`、`IdReq`、`IdsReq` |
| `com.cu.framework.basic.exception` | `BackendException`、`BizException`、`ExceptionConstant`、`ErrorType`、`ErrorLog`、`GlobalExceptionHandlerAdvice` |
| `com.cu.framework.basic.filter` | `EarlyRequestFilter` 与统一执行代理 `EarlyRequestFilterProxy` |
| `com.cu.framework.basic.logger` | `TraceRequestFilter`、`RequestLoggerFilter`、`LoggerConstant`；`request` 与 `trace` 子包 |
| `com.cu.framework.basic.utils` | `BeanUtil`、`CopyObject`、`FileUtils`、`MapUtil`、`RegUtil`、`RequestIpUtil`、`TreeUtil`、`YmlUtils` |

## 统一响应

```java
Result.success();                 // code=200, success=true, message="成功"，成功响应不输出 errorType
Result.success(data);             // 携带业务数据
Result.failed(ErrorType.BUSINESS, "业务失败");   // code=400, errorType=BUSINESS
Result.failed(ErrorType.DATABASE, "数据库操作失败，请稍后重试"); // code=500, errorType=DATABASE
Result.failed("业务失败");         // 兼容旧用法：code=500，不带 errorType
Result.failed(400, "参数错误");    // 兼容旧用法：指定 code，不带 errorType
```

失败信封通过 `errorType` 区分七类错误，HTTP 状态与 `code` 固定对应：

| `errorType` | HTTP / code | 典型来源 |
| --- | --- | --- |
| `AUTHENTICATION` | 401 | 未登录、凭据错误、账号状态异常、会话失效 |
| `AUTHORIZATION` | 403 | 已认证但无权限 |
| `VALIDATION` | 400 | 参数校验、绑定、解析、类型转换失败 |
| `BUSINESS` | 400 | `BizException` |
| `USAGE` | 400 | 权限框架调用方式错误 |
| `DATABASE` | 500 | 数据库访问失败 |
| `SYSTEM` | 500 | 空指针及其余未处理异常 |

`PageResult` 固定四个字段：`page`、`size`、`total`、`data`，用 `PageResult.of(page, size, total, data)` 构建。

对外契约的完整要求见 [basic/request-response-contract](../openspec/specs/basic/request-response-contract/spec.md)。这一层只提供模型，不负责把异常转成响应；后者由 `GlobalExceptionHandlerAdvice` 与各 starter 的异常出口完成。

## 异常

- `BackendException`：基础运行时异常，默认 `code=500`。
- `BizException`：业务异常，继承 `BackendException`，由全局 advice 归入 `BUSINESS`（400）。
- `GlobalExceptionHandlerAdvice`：`@RestControllerAdvice`，顺序最低；处理 `BizException`、参数校验/绑定/解析/类型转换、数据库访问异常与其余 `Throwable`。空指针归入 `SYSTEM`，不再按业务异常处理。
- `ErrorLog`：统一输出 `[分类] 请求方法 路径 - 摘要`。认证状态、参数、业务等预期失败以 INFO/WARN 单行记录且不带堆栈；数据库与系统故障以 ERROR 记录并保留完整堆栈。
- 数据库异常只向调用方返回“数据库操作失败，请稍后重试”，SQL、表名与连接信息只进入日志；被其他异常包装的数据库失败沿 cause 链识别后仍归入 `DATABASE`。
- 认证 / 授权 / 权限框架的异常出口由对应 starter 提供，不在这里。

## 请求追踪

- `TraceRequestFilter`（`@Order(1)`）从请求头 `traceId` 取流水号，没有时生成 UUID；写入 `TraceContext`（底层是 SLF4J `MDC`），请求结束在 `finally` 清除。
- 容器中存在自定义 `TraceIdProcessor` 时优先使用；默认 `HeaderTraceIdProcessor`。
- `TraceResponseBodyAdvice` 把返回值为 `Result` 的响应包装成 `TraceResult`，附带当前 `traceId`。
- 日志格式与 traceId 输出的完整说明见 [observability/request-tracing](../openspec/specs/observability/request-tracing/spec.md)。

## 请求日志

`RequestLoggerFilter`（`@Order(3)`）解析请求头、query 参数与 JSON body，打印请求日志，并在请求结束后发布 `RequestLoggerEvent`，带上耗时与 traceId。落库与消息推送是监听器的事，在 [basic-support](../basic-support/README.md) 中实现。

## 工具类

- `BeanUtil` / `CopyObject`：对象拷贝与转换。
- `TreeUtil`：列表转树、按条件裁剪树、收集子树 ID。
- `FileUtils`、`MapUtil`、`RegUtil`、`RequestIpUtil`、`YmlUtils`：文件、Map、正则、IP、YAML 的常用封装。

工具类没有自动装配，直接静态调用；依赖 `basic` 即可使用。

## 最小依赖

```xml
<dependency>
    <groupId>com.cu.framework</groupId>
    <artifactId>basic</artifactId>
</dependency>
```

只引 `basic` 不会注册任何过滤器或异常 advice；需要开箱即用的 Web 行为时引 [basic-spring-boot-starter](../boot/basic-spring-boot-starter/README.md)。

## 相关文档

- [basic-support](../basic-support/README.md)
- [basic-spring-boot-starter](../boot/basic-spring-boot-starter/README.md)
- [basic 示例](../sample/basic-boot-sample/README.md)
- [统一响应契约](../openspec/specs/basic/request-response-contract/spec.md)
- [请求追踪规格](../openspec/specs/observability/request-tracing/spec.md)
