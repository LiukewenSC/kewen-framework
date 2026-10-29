## 1. 使用的框架与工具

- 日志门面：SLF4J（通过 `lombok.extern.slf4j.Slf4j` 的 `@Slf4j` 注解注入 `Logger`）。
- 结构化字段传播：`org.slf4j.MDC`，以 key `traceId` 在 MDC 中传递请求流水号。
- Spring 事件机制：`ApplicationEventPublisher` / `ApplicationListener` 作为请求日志采集与持久化之间的解耦通道。
- Servlet Filter：`javax.servlet.Filter` + `OncePerRequestFilter` 包装为自定义 `EarlyRequestFilter`，由 `EarlyRequestFilterProxy` 统一装配。
- 异步落库/通知：Spring `@Async` + `EnableAsync` + 自定义 `ThreadPoolTaskExecutor`（`AsyncConfig`）。

项目中没有引入独立的日志框架实现（如 Logback/Log4j2 配置类），也未定义业务级的 Logger 封装类；业务代码直接调用 SLF4J。因此该系统的重点是 **请求级结构化日志采集、traceId 传播以及事件驱动的异步持久化**，而非通用日志级别管理。

## 2. 关键文件与包

- `basic/src/main/java/com/cu/framework/basic/logger/LoggerConstant.java` — 定义 MDC key 常量 `TRACE_ID_KEY = "traceId"`。
- `basic/src/main/java/com/cu/framework/basic/logger/TraceContext.java` — 对 MDC 的 get/set/remove 静态封装。
- `basic/src/main/java/com/cu/framework/basic/logger/TraceRequestFilter.java` — 全局 traceId 过滤器（`@Order(1)`），从 `TraceIdProcessor`（默认 `HeaderTraceIdProcessor`）解析 traceId 写入 MDC，并在 finally 中清理。
- `basic/src/main/java/com/cu/framework/basic/logger/RequestLoggerFilter.java` — 请求日志采集过滤器（`@Order(3)`），读取 headers/params/body，构造 `RequestLogger`，打印 info 日志，发布 `RequestLoggerEvent`。
- `basic/src/main/java/com/cu/framework/basic/logger/request/RequestLogger.java` — 结构化请求日志 DTO（traceId/url/method/params/body/ip/headers/execMillisecond）。
- `basic/src/main/java/com/cu/framework/basic/logger/request/RequestLoggerEvent.java` — 携带 `RequestLogger` 的 Spring ApplicationEvent。
- `boot/basic-spring-boot-starter/src/main/java/com/cu/framework/boot/basic/config/EarlyFilterConfig.java` — 注册 `TraceRequestFilter`、`RequestLoggerFilter`、`EarlyRequestFilterProxy` Bean。
- `boot/basic-spring-boot-starter/src/main/java/com/cu/framework/boot/basic/config/AsyncConfig.java` — 启用 `@EnableAsync` 并暴露 `asyncExecutor` 线程池（核心线程数 `2*CPU+1`，最大 `5*CPU`，队列容量 `2*CPU`，拒绝策略 `CallerRunsPolicy`）。
- `basic-support/src/main/java/com/cu/framework/basic/support/log/RequestLogDatabasePersistentListener.java` — 监听 `RequestLoggerEvent`，`@Async` 后通过 MyBatis-Plus 将请求日志持久化到 `sys_request_log` 表。
- `basic-support/src/main/java/com/cu/framework/basic/support/log/RequestLogFangTangMessageListener.java` — 监听 `RequestLoggerEvent`，`@Async` 后通过 `FangTangMessageClient` 推送方糖消息（按 IP 使用 Guava Cache 做 30 分钟去重）。

## 3. 架构与约定

### 3.1 过滤器链顺序

两个核心 Filter 均实现 `com.cu.framework.basic.filter.EarlyRequestFilter`，由 `EarlyRequestFilterProxy` 统一代理执行，并通过 `@Order` 控制顺序：

1. `TraceRequestFilter` (`@Order(1)`)：最先执行，负责解析 traceId 并放入 MDC。
2. `RequestLoggerFilter` (`@Order(3)`)：之后执行，负责采集请求参数、Body、Headers、IP、耗时并发布事件。

这种顺序是强制性的——如果 traceId 未先入 MDC，`RequestLoggerFilter` 构造的 `RequestLogger.traceId` 将为空。

### 3.2 结构化字段模型

所有请求日志围绕 `RequestLogger` 这一中间结构体流转，包含以下字段：

| 字段 | 含义 |
|---|---|
| `traceId` | 来自 `TraceContext.get()`，最终也取自事件对象（避免异步线程丢失 ThreadLocal/MDC） |
| `ip` | 通过 `RequestIpUtil.getIp(request)` 提取 |
| `url` | `request.getRequestURI()` |
| `method` | HTTP 方法 |
| `params` | query 参数经 `JSONObject.toJSONString` 序列化后的字符串 |
| `body` | 经 fastjson2 `JSON.parse(byte[])` 解析后的对象（通常为 Map），无 body 时为 null |
| `headers` | 全部请求头 Map（名称 → 值） |
| `execMillisecond` | `System.currentTimeMillis()` 前后差值 |

### 3.3 采集 → 事件 → 多 Sink 模式

`RequestLoggerFilter` 在 `finally` 块中发布 `RequestLoggerEvent`，不关心具体消费方。`basic-support` 模块提供多个 `ApplicationListener<RequestLoggerEvent>` 实现：

- `RequestLogDatabasePersistentListener`：异步写入数据库。
- `RequestLogFangTangMessageListener`：异步发送第三方通知。

新增 sink 只需再实现一个 `ApplicationListener<RequestLoggerEvent>`，无需修改采集逻辑。

### 3.4 TraceId 来源可插拔

`TraceRequestFilter` 通过 `ObjectProvider<TraceIdProcessor>` 获取处理器；若容器中未注册自定义实现，则回退到默认的 `HeaderTraceIdProcessor`（从请求头读取）。这使 traceId 来源可被外部扩展。

### 3.5 异步配置约束

`AsyncConfig` 显式实现了 `AsyncConfigurer` 并返回名为 `asyncExecutor` 的 `ThreadPoolTaskExecutor`，这使得所有 `@Async` 方法共享同一线程池。异常处理回调目前仅通过 `System.out.println` 输出类名、方法名和异常信息，未集成日志框架。

## 4. 约定与约束

- **MDC key 固定为 `traceId`**：由 `LoggerConstant.TRACE_ID_KEY` 集中声明，`TraceContext` 的所有操作都使用该常量，禁止散列硬编码。
- **traceId 生命周期**：`TraceRequestFilter` 在 `try` 前 `TraceContext.set`，在 `finally` 中 `TraceContext.remove()`，确保每次请求结束后清除，避免线程复用污染。
- **Filter 顺序不可颠倒**：`TraceRequestFilter` 必须排在 `RequestLoggerFilter` 之前（分别标注 `@Order(1)` 与 `@Order(3)`），否则 `RequestLogger` 中的 traceId 为空。
- **请求 Body 只能读一次**：`RequestLoggerFilter` 使用自定义的 `BodyHttpServletRequest` 包装原始 request 以支持重复读取 body，下游不应直接再次读取原 request。
- **异步消费者必须依赖事件对象中的 traceId**：`RequestLogDatabasePersistentListener.save()` 注释明确指出“traceId 取自事件对象携带的数据，避免异步线程 TraceContext 丢失”，因为 `@Async` 切换线程后 MDC/ThreadLocal 不再可用。
- **方糖消息通知按 IP 去重**：`RequestLogFangTangMessageListener` 使用 Guava `CacheBuilder.expireAfterAccess(30, TimeUnit.MINUTES)` 对同一 IP 进行去重，缓存过期后才重新推送。
- **日志级别**：采集侧使用 `log.info(...)` 输出请求摘要；持久化与通知侧仅在 `isDebugEnabled()` 时输出完整 JSON；数据库持久化监听器构造函数中用 `log.info("开启请求日志数据库持久化")` 标识自身已启用。
- **项目未定义统一的 log level 策略或 Logger 封装类**：业务代码直接通过 Lombok `@Slf4j` 注入 SLF4J Logger，未见统一的日志等级规范（如 WARN/ERROR 分类规则）。
- **异步异常未接入日志框架**：`AsyncConfig.getAsyncUncaughtExceptionHandler()` 当前使用 `System.out.println` 打印异常，而不是走 SLF4J，这是待改进点。