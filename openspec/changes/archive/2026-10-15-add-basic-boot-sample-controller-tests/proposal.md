# Proposal

## Why

`sample/basic-boot-sample` 的 3 个 controller 不承载业务逻辑，它们存在的意义是演示框架 web 层对外的承诺：日期参数怎么绑定、日期字段怎么序列化、校验失败返回什么形状、响应体为什么多出一个 `traceId`。这套承诺目前只写在 `RequestResponseFormatConfig`、`GlobalExceptionHandlerAdvice`、`TraceResponseBodyAdvice` 的代码里，仓库没有任何测试或文档把它落到纸面——改坏了不会有任何测试变红。本 change 把它固化成可执行契约。

`/jackson` 下的 3 个 `paramSerializer*` 方法已改为把绑定结果回显在 `data` 中（该改动尚在 working tree），使"解析出的值对不对"从只能推断变成可直接断言。

## What Changes

- 为 `sample/basic-boot-sample` 新建 `src/test`，覆盖 3 个 controller 的全部 6 个接口。
- 该模块 pom 增加 `spring-boot-starter-test`（test scope）。这将是全仓库第一份能真正跑起来的 HTTP 层测试：其余 sample 的数据源都指向本机不存在的 MySQL，只有本模块自 de067cb 起默认走 H2 内存库。
- 测试专用属性关闭方糖推送开关。生产配置下每个请求都会经 `@Async` 监听器向 `https://sctapi.ftqq.com` 发一次出站 HTTP，测试不能带着这个行为进 CI。
- 断言口径一律按**当前实际行为**，包含 2 处已知缺陷（见 Impact）。本 change 不修任何生产代码。
- 新增能力 `basic/request-response-contract`，把这 6 个接口对外承诺的行为写成需求。

**非目标**：其余 26 个 controller（分布在 12 个模块，其中有真实业务逻辑，测法完全不同）；修复上述缺陷；断言"请求日志落库"这类 `@Async` 副作用；断言 filter 链的执行顺序序列。

## Capabilities

### New Capabilities

- `basic/request-response-contract`: 约束 basic-boot-sample 的探针接口所体现的对外 HTTP 契约——日期参数的绑定格式、日期字段的序列化格式、参数校验失败与类型转换失败时的响应形状、响应体携带流水号。

### Modified Capabilities

无。`observability/request-tracing` 约束的是流水号在线程日志上下文中的写入与清除，不涉及响应体的 `traceId` 字段，两者不重叠。

## Impact

- **改动文件**：`sample/basic-boot-sample/pom.xml`、新增 `sample/basic-boot-sample/src/test/**`。
- **依赖既有装配**：本模块能在 H2 上装配，依赖 `exclude = MybatisPlusConfig.class` 与自建 `DbType.H2` 分页插件两处改动（已由 f98ee5c 提交）。它们若被回退，本 change 的 harness 前提失效（详见 design.md Risks）。
- **不改动** `basic`、`basic-support`、`boot/**` 的任何生产代码，因此不破坏现有调用方。
- **按现状固化的已知缺陷**（将来修复时必须同步更新这些测试）：
  - `basic/src/main/java/com/cu/framework/basic/exception/GlobalExceptionHandlerAdvice.java:65` 想去掉拼接消息结尾的分号，实际 `substring(0, length() - 2)` 多删了一个字符。
  - `sample/.../controller/JacksonSerializerController.java:51` 的 `paramSerializer3` 是 `@GetMapping` 配 `@RequestBody`，非标准用法。
- **原第 3 处缺陷已由 fd64842 修复**：mysql profile 曾引用 starter 中不存在的 `com.cu.framework.boot.basic.p6spy.KP6SpyDriver`，现已改为原生 `com.p6spy.engine.spy.P6SpyDriver`。测试仍固定走 h2，但那是为了不依赖外部 MySQL，不再是因为该缺陷。
