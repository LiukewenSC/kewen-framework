# basic

`basic` 是框架的基础库：统一响应模型、异常基类、请求追踪与请求日志、通用工具类。它是库模块，不是 starter；自动装配与过滤器注册在 [basic-spring-boot-starter](../boot/basic-spring-boot-starter/README.md)。

## 包结构

| 包 | 内容 |
| --- | --- |
| `com.cu.framework.basic.model` | `Result`、`PageResult`、`PageReq`、`IdReq`、`IdsReq` |
| `com.cu.framework.basic.exception` | `BackendException`、`BizException`、`ExceptionConstant`、`GlobalExceptionHandlerAdvice` |
| `com.cu.framework.basic.filter` | `EarlyRequestFilter` 与统一执行代理 `EarlyRequestFilterProxy` |
| `com.cu.framework.basic.logger` | `TraceRequestFilter`、`RequestLoggerFilter`、`LoggerConstant`；`request` 与 `trace` 子包 |
| `com.cu.framework.basic.utils` | `BeanUtil`、`CopyObject`、`FileUtils`、`MapUtil`、`RegUtil`、`RequestIpUtil`、`TreeUtil`、`YmlUtils` |

## 统一响应

```java
Result.success();                 // code=200, success=true, message="成功"
Result.success(data);             // 携带业务数据
Result.failed("业务失败");         // code=500, success=false
Result.failed(400, "参数错误");    // 指定错误码
```

`PageResult` 固定四个字段：`page`、`size`、`total`、`data`，用 `PageResult.of(page, size, total, data)` 构建。

对外契约的完整要求见 [basic/request-response-contract](../openspec/specs/basic/request-response-contract/spec.md)。这一层只提供模型，不负责把异常转成响应；后者由 `GlobalExceptionHandlerAdvice` 与各 starter 的异常出口完成。

## 异常

- `BackendException`：基础运行时异常，默认 `code=500`。
- `BizException`：业务异常，继承 `BackendException`。
- `GlobalExceptionHandlerAdvice`：`@RestControllerAdvice`，顺序最低；处理 `BizException`、空指针、参数校验失败与其他 `Throwable`，统一返回 `Result.failed(...)`。
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
