# Design

## Context

动机见 `proposal.md - Why`。此处只记录约束方案的技术现状：

- 被测的行为载体是 `boot/basic-spring-boot-starter/src/main/resources/META-INF/spring.factories` 里 `EnableAutoConfiguration` 下的 6 个配置类。日期绑定用的是普通 `Converter` bean，异常信封用的是 `RequestResponseFormatConfig` 里 `@Bean` 出来的 advice——**都不在 `basic-boot-sample` 自己的组件扫描范围内**，只能靠 autoconfiguration 生效。
- `RequestLoggerFilter` 每个请求发布一次 `RequestLoggerEvent`，两个 `@Async` 监听器分别做内存 H2 写库和向 `https://sctapi.ftqq.com` 发 HTTP POST；出站那一路的异常被吞掉。
- 校验失败的处理路径不设 HTTP 状态码（返回 200）；类型转换失败落入兜底的 `Throwable` handler，该 handler 带裸 `@ResponseStatus`（500）。
- `basic-boot-sample` 是全仓库唯一默认走 H2 内存库的 sample，其余 sample 的数据源指向本机不存在的 MySQL。全仓库仅 auth/login 侧引用 Redis，本模块的依赖树里没有 Redis。
- 该模块能在 H2 上装配起来，依赖两处改动：`BasicBootSampleApp` 的 `@SpringBootApplication(exclude = MybatisPlusConfig.class)`，以及 `BasicBootSampleConfig` 自建的 `MybatisPlusInterceptor(DbType.H2)` bean。它们在本 change 执行期间由 f98ee5c 提交落地。本设计的全部 harness 结论以这两处仍在为前提。

## Goals / Non-Goals

**Goals:**

- 6 个接口的对外契约成为可执行断言，改坏框架时有测试变红。
- 三个测试类共用同一个 Spring 上下文，只启动一次。
- CI 里不产生任何出站网络请求。

**Non-Goals:**

- 不补框架模块自身的单元测试缺口（`boot/**` 的测试属于 starter 自己的责任范围）。
- 不断言异步副作用（请求日志是否落库）。
- 不改任何生产代码，包括已知的 2 处缺陷。
- 不引入 Awaitility、Testcontainers 等新测试库；只用 `spring-boot-starter-test` 自带的 JUnit 5 + MockMvc。

## Decisions

### D1 harness 用 `@SpringBootTest(webEnvironment = MOCK)` + `@AutoConfigureMockMvc`，不用 `@WebMvcTest`

`@WebMvcTest` 是基于 `@ImportAutoConfiguration` 的切片，只加载 Spring Boot 自带的 web 相关 autoconfiguration，**不加载第三方在 `EnableAutoConfiguration` 下登记的配置类**。也就是说本 change 要测的 `Converter`、异常 advice、Jackson customizer 在切片里一个都不会生效，必须显式 `@Import` 手抄清单。

风险不在于麻烦，而在于**抄漏了测试照样绿**：漏掉 `localDateConverter` 后，`LocalDate` 参数会退回 Spring 默认绑定行为，断言仍然可能通过，测的却不是本项目实际对外提供的那套规则。

选全量装配后拿到与生产一致的上下文。代价是启动含 DataSource 与 `spring.sql.init` 执行 `docs/sql/basic-support.sql`（该脚本已做 MySQL/H2 双兼容，并由 `scripts/check-sql-compat.sh` 实测校验）。

保留的退路见 Risks 第一条。

### D2 配置集中在一个抽象基类

新增 `AbstractBasicSampleWebTest`，把 `@SpringBootTest`、`@AutoConfigureMockMvc`、`@TestPropertySource`、`@ActiveProfiles("h2")` 全部放在基类上，三个测试类只继承并写各自的断言。

理由：上下文缓存按合并后的配置键控，注解只有一份就不会因为某个类多写一个 `@MockBean` 而分裂出第二个上下文；同时避免三个类各自复制一遍关开关的属性。

### D3 测试属性关方糖、留库

`@TestPropertySource` 设 `cu.notify.fang-tang.enabled=false`、`cu.request.message.fang-tang=false`，保留 `cu.request.persistent.database=true`。

关前者是因为仓库在 Gitee 公开可读，`cu.notify.fang-tang.key` 目前是占位串且异常被吞——测试会绿，但每次跑都在真的出网；一旦有人把真 key 提交进来，跑测试就变成了给别人推送消息。后者只写内存 H2，无外部依赖，留着不影响隔离性。

已核实生效路径：`FangTangMessageClient` 带 `@ConditionalOnProperty(prefix = "cu.notify.fang-tang", name = "enabled", havingValue = "true")`，关掉后该 bean 不注册，`RequestLogFangTangMessageListener` 因其 `@ConditionalOnProperty` 与 `@ConditionalOnBean` 双重条件同时不注册。`RequestLogDatabasePersistentListener` 的条件是 `cu.request.persistent.database=true`，保持不变故仍注册。

代价：测试不再覆盖"方糖推送真的发得出去"。那本就该由 starter 侧负责，见 Non-Goals。

### D4 断言按当前实际行为，缺陷原样固化

`proposal.md - Impact` 列的 2 处缺陷，测试断言其**现状输出**，并在断言行上方一行注释注明该行为有已知缺陷、修在哪。

理由：测试的职责是记录契约而非表达愿望。按现状断言的效果是——将来真去修的时候这些测试会变红，而红色正好是"你在改对外契约"的提醒信号。若反过来按"应该的样子"写，测试从第一天起就是红的或被 `@Disabled`，等于没有测试。

`specs/basic/request-response-contract/spec.md` 里的需求措辞刻意停在"由各字段错误以分号连接构成"这一层，不写入多删一个字符的细节，这样修复缺陷时不需要改动 spec。

### D5 动态值只断格式

`deSerializerDate` 返回的是 `new Date()` / `LocalDateTime.now()`，断言用正则匹配格式，不断具体值，否则测试天然 flaky。

## Risks / Trade-offs

- **[全量 context 能否在本机起来]** → 已由任务 1.3 实测通过（H2 建池 + `spring.sql.init` 执行 `docs/sql/basic-support.sql`，`GET /filter/hello` 返回 200）。D1 的切片退路未触发，仍保留在此作为回归失败时的备选。
- **[H2 可装配的前提是 `exclude = MybatisPlusConfig.class`]** → 前提已由 f98ee5c 提交落地。若将来被回退，MyBatis-Plus 分页插件会退回 starter 里的默认 `DbType`，本模块的全量上下文可能直接起不来；届时先停下重新确认前提，而不是去改生产代码。
- **[`@Async` 使副作用断言变成竞态]** → v1 不断言请求日志落库。要断就得轮询或引入 Awaitility，属于另一个 change。
- **[流水号回显依赖 slf4j 绑定提供 MDC]** → 本模块有 `logback-spring.xml`，MDC 可用。若日后换成不提供 MDC 的绑定，`traceId` 断言会假通过，因此断言写成"非空字符串"而不是"字段存在"。
- **[全量上下文是本仓库最慢的测试]** → 三个类共享一个缓存上下文，只付一次启动成本；相对 slice 仍是秒级到十秒级的差别。
- **[测试为何固定 `h2`]** → 为的是不依赖本机外部 MySQL，而不是规避缺陷：mysql profile 原先引用的 `com.cu.framework.boot.basic.p6spy.KP6SpyDriver` 在 starter 中并不存在，已由 fd64842 换成原生 `com.p6spy.engine.spy.P6SpyDriver`。
