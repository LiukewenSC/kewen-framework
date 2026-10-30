# Design

## Context

现状与动机见 `proposal.md - Why`。本节只列影响实现方式的事实，全部来自对本机依赖的实测（Boot 4.1.2 / Security 7.1.2 / Session 4.1.1 / Framework 7.0.10 的 jar 与 BOM），不是推断。

当前栈：Java 8、Boot 2.7.18、Security 5.7.11、Session 2.7、MP 3.5.8、JSQLParser 4.7（被 dependencyManagement 覆盖，MP 3.5.8 自身依赖 5.0）、magic-api 2.1.0、25 个 pom、440 个 Java 文件。

目标栈：Java 21、Boot 4.1.2、Security 7.1.2、Session 4.1.1、Framework 7.0.10、Tomcat 11、Jackson 3.1.7、Hibernate Validator 9.1.5、MP 3.5.18.1（boot4 starter）、JSQLParser 4.9。

已核实的关键破坏点：

| 区域 | 现状 | 目标下的实际 API |
|---|---|---|
| Spring Security DSL | `authorizeRequests()`、无参 `csrf()`/`logout()`/`exceptionHandling()`/`sessionManagement()`/`saml2Login()` | 无参方法全部删除，只剩 `Customizer` 形式；`authorizeRequests()` 删除，只剩 `authorizeHttpRequests`；`antMatchers` 删除，只剩 `requestMatchers` |
| 安全配置基类 | `WebSecurityConfigurerAdapter` | 已删除（jar 内 0 个 class），必须改 `SecurityFilterChain` Bean |
| 自定义匹配器 | `AntPathRequestMatcher` | 已删除；用 `PathPatternRequestMatcher.pathPattern(HttpMethod, String)` |
| SAML provider | `OpenSamlAuthenticationProvider` | 只剩 `OpenSaml5AuthenticationProvider`（OpenSaml4 已删除）；`ResponseToken` 类型与转换器随之为 OpenSaml5 |
| SAML 注册 | `assertingPartyDetails(...)` | 改为 `assertingPartyMetadata(...)`，参数类型 `AssertingPartyMetadata.Builder<?>` |
| SAML 登出 DSL | `.logoutRequest().logoutUrl(...).and()` | `logoutRequest(Customizer)` / `logoutResponse(Customizer)`，无无参形式 |
| SAML 其余 | `saml2Login().authenticationManager/relyingPartyRegistrationRepository/loginProcessingUrl`、`Saml2MetadataFilter` 构造器 | 均保留，签名不变 |
| Jackson | `com.fasterxml.jackson.databind.*`、`MappingJackson2HttpMessageConverter` | Boot 4 默认 Jackson 3：`tools.jackson.databind.*`、`tools.jackson.databind.json.JsonMapper`、`JacksonJsonHttpMessageConverter`；注解包仍是 `com.fasterxml.jackson.annotation` |
| Jackson 定制器 | `Jackson2ObjectMapperBuilderCustomizer`（`org.springframework.boot.autoconfigure.jackson`） | Jackson 3 用 `org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer` |
| Framework 7 | `MediaType.APPLICATION_JSON_UTF8_VALUE` | 已删除，只能用 `APPLICATION_JSON_VALUE` |
| HTTP 客户端 | Apache HttpClient 4 + `HttpComponentsClientHttpRequestFactory(HttpClient)` | 构造器只接受 HttpClient 5；需显式加 `org.apache.httpcomponents.client5:httpclient5` |
| Servlet / Validation | `javax.servlet` / `javax.validation` | `jakarta.*`；Boot 4 BOM 不再管理 `javax.servlet-api` / `validation-api` 版本 |
| 自动配置 | `spring.factories` 的 `EnableAutoConfiguration` | 路径 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`；`@AutoConfiguration` 及 `autoconfigure.condition.*` 包名不变 |
| Boot 4 test | `@AutoConfigureMockMvc` 在 `org.springframework.boot.test.autoconfigure.web.servlet` | 移到 `org.springframework.boot.webmvc.test.autoconfigure`，依赖改为 `spring-boot-starter-webmvc-test`（starter-test 不再包含） |
| Spring Session | `SessionRepository` 4 方法、`HttpSessionIdResolver` 3 方法、`SpringSessionRememberMeServices`、`JdbcIndexedSessionRepository` | 全部保留，签名不变；`spring.session.jdbc.*` 迁到 `spring-boot-session-jdbc` 模块，属性名不变 |
| Boot 4 生命周期扩展点 | `SpringApplicationRunListener` / `SpringBootExceptionReporter` | 包名与构造约定不变，sample 的 `spring.factories` 继续有效 |
| YAML 工具 | `YamlProcessor` / `YamlPropertiesFactoryBean` | Framework 7 仍保留 |
| MP | `mybatis-plus-boot-starter` 3.5.8 | `mybatis-plus-spring-boot4-starter` 3.5.18.1（pom 目标 Boot 4.1.0），自带 `mybatis-plus-jsqlparser-4.9` |

## Goals / Non-Goals

**Goals:**

- 用一次迁移把运行基线落到 Java 21 + Boot 4.1，下一次大版本迁移推到 2028 年前后。
- 保持所有既有行为契约不变：鉴权、数据权限、登录交付、异常分类、存储、请求日志。
- 让框架重新可被 Jakarta / Boot 4 项目引用，下游只需换依赖版本、无需改 starter 坐标。
- 消除当前 JSQLParser 4.7 与 MP 自带 5.0 的版本错配。

**Non-Goals:**

- 不新增业务能力，不改任何 spec 中的行为。
- 不迁移到 Boot 4 的 AOT / Native Image。
- 不把 JUnit 4 测试统一改写成 JUnit 5/6。
- 不为 magic-api 保留兼容分支。
- 不引入虚拟线程、结构化并发等 Java 21 新特性（只升级基线，不改变运行模型）。

## Decisions

### D1：运行基线选 Java 21，而不是 17 或 25

- **选择**：`maven.compiler.release=21`，所有模块与 sample 统一。
- **理由**：Temurin 17 的免费更新到 2027-10，只剩约 1 年；21 到 2029-12，且第三方库（Lombok、字节码工具）适配最广；25 虽然是新 LTS（到 2031-09），但作为对外发布的框架库，21 的下游兼容性更好。
- **替代**：17——会把下一次 JDK 迁移提前到一年内；25——可作为后续运行时选项，但不作为编译基线。

### D2：直接上 Boot 4.1.2，而不是先到 3.5

- **选择**：Boot 4.1.2 + Security 7.1.2 + Framework 7.0.10。
- **理由**：3.5 的 OSS 维护已于 2026-06 结束，落在 3.5 等于一落地就站在 EOL 线上，1–2 年内还要再跨一次大版本；4.1 是当前 OSS 主线，下一大版本预计 2028。
- **替代**：Boot 3.5——迁移成本低 3–5 天，但要连续做两次大迁移（3.5→4），且 magic-api 能保留。用户已确认 magic-api 可摘除，故不选。

### D3：Jackson 3 直接迁移，不用 `spring-boot-jackson2` 兼容模块

- **选择**：`ObjectMapper`/`JsonNode`/日期序列化器/HTTP converter 全部换 `tools.jackson.*`；注解包保持 `com.fasterxml.jackson.annotation` 不动。
- **理由**：Boot 4 的 `spring-boot-starter-web` 默认拉 `spring-boot-starter-jackson`（Jackson 3），兼容模块会同时挂两套 JSON 栈，且把同一份迁移推迟到以后；实际改动只有 16 个文件。
- **替代**：加 `spring-boot-jackson2` 并把 `Jackson2ObjectMapperBuilderCustomizer` 换成 `org.springframework.boot.jackson2.autoconfigure` 版本——作为降级预案保留，不作为主路径。

### D4：SAML 直接落 OpenSaml5

- **选择**：`OpenSaml5AuthenticationProvider` + `assertingPartyMetadata` + lambda 化登出 DSL。
- **理由**：Security 7.1 已删除 OpenSaml4，没有别的选择；OpenSaml5 与 OpenSaml4 都使用 `net.shibboleth.shared.*`，`IdpMetadataParser` 的包名迁移只做一次。
- **注意**：`SecurityUserSamlAuthenticationConverter` 的泛型从 `ResponseToken` 换成 OpenSaml5 的对应内部类；`createDefaultResponseAuthenticationConverter` 仍可用。

### D5：MyBatis-Plus 3.5.18.1 + JSQLParser 4.9

- **选择**：`mybatis-plus-spring-boot4-starter:3.5.18.1`，JSQLParser 由 `mybatis-plus-jsqlparser-4.9` 带入，不再手写 4.7 覆盖。
- **理由**：该 starter 的 pom 明确以 Boot 4.1.0 为目标；4.9 保住 `ParenthesedExpressionList` / `SelectUtils` 等现有用法；顺带修掉 4.7 vs 5.0 的错配。
- **实现修正（2026-10-31）**：MP 3.5.17 起把 `IService` / `ServiceImpl` / `Model` 从 `extension.*` 移到 `spring.*`；`mybatis-plus` 聚合包不再包含 `mybatis-plus-extension` 与可选的 `mybatis-plus-jsqlparser-4.9`。因此代码 import 按新包名调整，并在使用分页插件的模块显式引入 `mybatis-plus-extension` 与 `mybatis-plus-jsqlparser-4.9`。
- **替代**：继续用 3.5.8 的 boot3 starter——Boot 4 下自动配置模块已变，不保证可用。

### D6：magic-api 模块整体摘除

- **选择**：从 `boot/pom.xml` 的 modules、根 `pom.xml` 的 dependencyManagement 中移除，删除 `boot/magic-spring-boot-starter/` 目录，并在 `docs/decisions/已废弃实现与决策记录.md` 记录移除理由。
- **理由**：magic-api 2.2.2 只声明兼容 Boot 3.4，无 Boot 4 支持；模块本身是实验性示例，不承载框架核心能力。
- **替代**：单独留一个 Boot 3.5 子工程——引入第二套 BOM 与维护成本，不值得。

### D7：OpenSAML 5 依赖 Shibboleth 仓库

- **选择**：根 `pom.xml` 增加 `https://build.shibboleth.net/maven/releases` 仓库，id 为 `shibboleth`。
- **理由**：OpenSAML 4/5 只发布在 Shibboleth 仓库，Maven Central 与阿里云镜像都没有 5.2.3（实测 404）；现有 settings.xml 的 mirror 只镜像 `central`，不会拦截该仓库。
- **替代**：把 OpenSAML 5 手工 install 进本地仓库——不可复现，不采用。

### D8：Spring Cloud / Alibaba BOM 保留并对齐到 2025.1.x

- **选择**：`spring-cloud-dependencies:2025.1.4`、`spring-cloud-alibaba-dependencies:2025.1.0.0`。
- **理由**：代码零使用，但框架 parent 可能被下游用来管理 `feign-core` 等版本；直接删除会破坏下游的版本来源。
- **替代**：删除两个 BOM 并显式管理 `feign-core`——更干净但属于对外契约变更，不在本次范围。

### D9：迁移顺序按"编译器驱动"

1. 根 pom / 全部子 pom 的 BOM、插件、坐标（先不动代码）→ 收集全部编译错误。
2. `javax.*` → `jakarta.*`；`spring.factories` → `AutoConfiguration.imports`。
3. Security 6/7 DSL 重写（SecurityConfig、JsonLoginAuthenticationFilterConfigurer、SessionConfig 的 customizer）。
4. SAML：OpenSaml5 + assertingPartyMetadata + 登出 lambda。
5. Jackson 3 与 HttpClient 5。
6. 测试适配（`@AutoConfigureMockMvc`、`spring-boot-starter-webmvc-test`）与文档。

先做无行为变化的机械替换，再做有行为变化的 Security/SAML，最后跑端到端。

### D10：测试框架维持现状

- JUnit 4（42 个类）和 JUnit 5（9 个类）按模块分开，不混用；Boot 4 的 BOM 仍管理 JUnit 4.13.2，surefire 仍能按模块选 provider。
- `spring-boot-starter-test` 在 Boot 4 提供 JUnit Jupiter 6（包名不变），现有 JUnit 5 测试源码预计无需改动；风险由测试运行兜底。

### 实现修正（2026-10-31，实施时发现）

- **必须开启 `-parameters`**：Spring Framework 6.1+ 移除了 `LocalVariableTableParameterNameDiscoverer`，`@RequestParam` / `@PathVariable` 不写名字时依赖编译期参数名。根 pom 增加 `maven.compiler.parameters=true`，否则 `/jackson/paramSerializer` 这类接口会在运行期抛 `IllegalArgumentException`。
- **Security 7 的 `apply()` 不再设置 builder**：`http.apply(configurer)` 只注册不调用 `setBuilder`，自定义 `AbstractAuthenticationFilterConfigurer` 的 `init` 会因 `getBuilder()` 为空抛 `securityBuilder cannot be null`；必须改用 `http.with(configurer, customizer)`。
- **Boot 4 的 `ApplicationTaskExecutorAsyncConfigurer`**：Boot 4 只要容器里存在 `Executor` 就会注册一个包装用的 `AsyncConfigurer`，与项目自己的 `AsyncConfig` 同时存在时 Spring 抛 "Only one AsyncConfigurer may exist"。落点：`AsyncConfig` 声明 `applicationTaskExecutor` Bean，并用 `@AutoConfiguration(before = TaskExecutionAutoConfiguration.class)` 让 Boot 跳过自己的执行器与包装层。
- **MyBatis-Plus 3.5.17+ 包迁移**：`IService` / `ServiceImpl` / `Model` 从 `extension.*` 移到 `spring.*`，`mybatis-plus` 聚合包不再带 `mybatis-plus-extension` 与可选的 `mybatis-plus-jsqlparser-4.9`；使用分页插件的模块需显式引入这两个依赖。
- **OpenSAML 5 仓库**：见 D7。

## Risks / Trade-offs

- **Security 7 DSL 与 SAML 重写是唯一的高风险区** → 单独分支、按文件小步提交；先编译、再单测、最后跑 `auth-rbac-security-boot-sample` 与 `kw-idp-simple`（9010）的 SSO 端到端。
- **Boot 4 的模块拆分可能还有未发现的包名变化** → 以编译器和 `mvn test` 为准；design 中的 API 表来自 4.1.2 实际 jar，实施时如遇差异以 jar 为准并回写。
- **H2 2.4.240 的 DDL 兼容性** → 先跑 `scripts/check-sql-compat.sh`，再启动各 sample；已知盲区（H2 ≠ MySQL）不变。
- **MySQL / P6Spy / 七牛无法在本机 H2 覆盖** → 保留 MySQL profile 的手工回归清单。
- **Jackson 3 序列化默认值差异** → 依赖现有 `basic/request-response-contract` 与 sample 的 JSON 契约测试。
- **magic-api 移除会改变 README 中的模块清单** → 同步 README 与 `boot/pom.xml`，并在决策记录中留理由。
- **下游使用方被迫升级 Jakarta** → 在 README 版本表与 release note 中明确 BREAKING。
- **`spring-boot-maven-plugin` 4.1.2 对 `layout=ZIP` 的支持** → 3 个 sample 使用 ZIP 重打包，实施时先验证 `repackage` 产物可启动；若不支持则改回默认 JAR layout。
- **Spring Session JDBC 的 `initialize-schema` 在 Boot 4 模块拆分后是否仍生效** → 启动 sample 验证 `SPRING_SESSION` 表初始化。

## Migration Plan

1. 新建分支 `upgrade/jdk21-boot4`，保留当前 master 作为回滚点。
2. 按 D8 的六个阶段提交；每个阶段结束必须 `mvn -DskipTests compile` 通过。
3. 第二阶段结束后跑一次 `mvn test`，确认机械替换没有引入行为回归。
4. 第五阶段结束后跑 H2 sample，第六阶段跑 MySQL profile 与 SSO 端到端。
5. 回滚策略：按阶段 revert；数据库 schema 不变，无数据迁移；magic-api 目录在最后一个提交删除，可单独 revert 恢复。

## Open Questions

- `spring-boot-maven-plugin` 4.1.2 的 `layout=ZIP` 是否仍可用——实施时以 `repackage` + 启动验证为准。
- H2 2.4.240 是否需要调整 `relation/*` 或 `sample/*/sql` 中的 DDL——以 `check-sql-compat.sh` 与 sample 启动结果为准。
- Boot 4 是否还有未覆盖的自动配置包名变化（如 `@AutoConfigureMockMvc` 之外的测试注解）——以编译器与测试运行为准。
