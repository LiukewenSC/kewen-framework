# JDK 21 + Spring Boot 4.1 升级变更说明

| 项目 | 内容 |
| --- | --- |
| 变更名称 | `upgrade-jdk21-boot4` |
| 分支 | `upgrade/jdk21-boot4` |
| 日期 | 2026-10-31 |
| 影响级别 | **BREAKING**（Java 基线、Jakarta 命名空间、Spring Security 配置 API） |
| 状态 | 代码与单测已完成；MySQL profile、SSO 端到端 2 项集成待人工验证；代码生成器模板有 1 项遗留 |

> 过程文档（proposal / design / tasks）在 `openspec/changes/upgrade-jdk21-boot4/`；本文面向使用方与后续维护者，说明实际改了什么、为什么这样改、升级时要注意什么。

## 一、摘要

框架从 Java 8 + Spring Boot 2.7 整体迁移到 Java 21 + Spring Boot 4.1。这不是一次普通依赖升级，而是跨越了 Spring Boot 3 的 Jakarta 命名空间迁移和 Spring Security 7 的配置 API 重写。

选择一次落到 Boot 4.1 而不是先到 3.5，是因为 Boot 3.5 的 OSS 维护已于 2026-06 结束；直接到 4.1 可以把下一次大版本迁移推到 2028 年前后，避免连续做两次大迁移。

## 二、版本对照

| 组件 | 变更前 | 变更后 |
| --- | --- | --- |
| Java / 编译目标 | 8 | **21**（`maven.compiler.release=21`） |
| Spring Boot | 2.7.18 | **4.1.2** |
| Spring Framework | 5.3.x | 7.0.10 |
| Spring Security | 5.7.11 | **7.1.2** |
| Spring Session | 2.7.x | 4.1.1 |
| Tomcat | 9.0.x | 11.0.27 |
| Jackson | 2.13.x | **3.1.7** |
| Hibernate Validator | 6.2.x | 9.1.5 |
| OpenSAML | 3.x | 5.2.3 |
| MyBatis-Plus | 3.5.8 | **3.5.18.1**（`mybatis-plus-spring-boot4-starter`） |
| MyBatis / MyBatis-Spring-Boot | 3.5.16 / 2.3.1 | 3.5.19 / 4.1.0 |
| JSQLParser | 4.7（手工覆盖） | 4.9（由 MyBatis-Plus 管理） |
| MySQL 驱动 | `mysql:mysql-connector-java:8.0.33` | `com.mysql:mysql-connector-j:9.7.0` |
| H2 | 2.1.214 | 2.4.240 |
| Spring Cloud / Alibaba | 2021.0.9 / 2021.0.5.0 | 2025.1.4 / 2025.1.0.0 |
| fastjson / fastjson2 | 2.0.9 / 2.0.25 | 2.0.66 / 2.0.66 |
| Hutool / Qiniu / Guava | 5.8.18 / 7.12.1 / 32.1.3-jre | 5.8.48 / 7.19.0 / 33.7.2-jre |
| commons-io / collections4 | 2.11.0 / 4.3 | 2.22.0 / 4.5.0 |
| Velocity（代码生成） | 2.0 | 2.3 |
| springfox / swagger-bootstrap-ui | 2.9.2 / 1.9.6 | **移除** |
| magic-api 模块 | 2.1.0 | **移除** |

## 三、具体改动

### 3.1 构建与依赖

- 根 [pom.xml](file:///C:/Users/kewen/Projects/kewen-framework/pom.xml)：
  - `maven.compiler.release=21`，并开启 `maven.compiler.parameters=true`（Spring 6.1+ 必需）。
  - 导入 Boot 4.1.2 / Cloud 2025.1.4 / SCA 2025.1.0.0 BOM。
  - 新增 Shibboleth 仓库：OpenSAML 4/5 只发布在 `https://build.shibboleth.net/maven/releases`，Central 与国内镜像都没有。
  - 移除 springfox、swagger-bootstrap-ui、magic-api starter、旧 `mysql:mysql-connector-java` 坐标。
  - 新增 `mybatis-plus-extension`、`mybatis-plus-jsqlparser-4.9` 的管理（MP 3.5.17+ 拆包后需要显式引入）。
- 其余 23 个子 pom：
  - 删除各自的 `maven.compiler.source/target`，统一继承 release 21。
  - `javax.servlet-api` / `validation-api` → `jakarta.servlet-api` / `jakarta.validation-api`。
  - `mybatis-plus-boot-starter` → `mybatis-plus-spring-boot4-starter`。
  - `mysql:mysql-connector-java` → `com.mysql:mysql-connector-j`。
  - `spring-boot-maven-plugin` 2.5.12/2.7.18 → 4.1.2；`mybatis-spring-boot-starter` 2.3.1 → 4.1.0；velocity 2.0 → 2.3。

### 3.2 Jakarta 与自动配置

- 44 个 Java 文件的 `javax.servlet` / `javax.validation` 改为 `jakarta.*`。
- 7 个 starter 的自动配置从 `META-INF/spring.factories` 迁移到 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`，配置类改用 `@AutoConfiguration`。
- `sample/basic-boot-sample` 的 `SpringApplicationRunListener` / `SpringBootExceptionReporter` 仍留在 `spring.factories`（这两个扩展点没有变）。

### 3.3 Spring Security 7

- [SecurityConfig.java](file:///C:/Users/kewen/Projects/kewen-framework/boot/login-security-spring-boot-starter/src/main/java/com/cu/framework/authentication/config/SecurityConfig.java)：
  - `WebSecurityConfigurerAdapter` 已删除，改为 `SecurityFilterChain` Bean + `WebSecurityCustomizer`。
  - `authorizeRequests().antMatchers(...)` → `authorizeHttpRequests(auth -> auth.requestMatchers(...)...)`。
  - `csrf` / `logout` / `exceptionHandling` / `sessionManagement` 全部改 lambda DSL。
  - 认证提供者改为 `DaoAuthenticationProvider` Bean，由 Spring Security 统一装配 `AuthenticationManager`。
  - 静态资源模式从 `/**/**.html` 改为 PathPattern 合法的 `/**/*.html`。
- [PasswordSecurityConfig.java](file:///C:/Users/kewen/Projects/kewen-framework/boot/login-security-spring-boot-starter/src/main/java/com/cu/framework/authentication/password/config/PasswordSecurityConfig.java)：`http.apply(...)` → `http.with(configurer, customizer)`（Security 7 的 `apply` 不再设置 builder）。
- `JsonLoginAuthenticationFilterConfigurer`：`AntPathRequestMatcher` → `PathPatternRequestMatcher.pathPattern(HttpMethod.POST, ...)`。
- `JsonLoginFilter`：删除 `MediaType.APPLICATION_JSON_UTF8_VALUE`（Framework 7 已移除），改为按 `APPLICATION_JSON_VALUE` 前缀判断并处理空 Content-Type。
- `SessionConfig`：`sessionManagement()` 改 lambda。

### 3.4 SAML / OAuth2

- [SamlConfig.java](file:///C:/Users/kewen/Projects/kewen-framework/boot/login-security-spring-boot-starter/src/main/java/com/cu/framework/authentication/saml/SamlConfig.java)：
  - `OpenSamlAuthenticationProvider` → `OpenSaml5AuthenticationProvider`；`OpenSamlMetadataResolver` → `OpenSaml5MetadataResolver`。
  - `Saml2WebSsoAuthenticationFilter` 改到 `...service.web.authentication` 包。
  - `RelyingPartyRegistration.Builder.assertingPartyDetails(...)` → `assertingPartyMetadata(...)`。
  - `saml2Login()` / `saml2Logout(...)` 全部改 lambda；`logoutRequest` / `logoutResponse` 不再有无参形式。
- [SecurityUserSamlAuthenticationConverter.java](file:///C:/Users/kewen/Projects/kewen-framework/boot/login-security-spring-boot-starter/src/main/java/com/cu/framework/authentication/saml/SecurityUserSamlAuthenticationConverter.java)：`ResponseToken` 与默认转换器改为 OpenSaml5。
- [IdpMetadataParser.java](file:///C:/Users/kewen/Projects/kewen-framework/boot/login-security-spring-boot-starter/src/main/java/com/cu/framework/authentication/saml/IdpMetadataParser.java)：`net.shibboleth.utilities.java.support.xml.BasicParserPool` → `net.shibboleth.shared.xml.impl.BasicParserPool`。
- [RestOperationBuilder.java](file:///C:/Users/kewen/Projects/kewen-framework/boot/login-security-spring-boot-starter/src/main/java/com/cu/framework/authentication/oauth2/RestOperationBuilder.java)：
  - Apache HttpClient 4 → 5：`HttpClientBuilder` + `PoolingHttpClientConnectionManagerBuilder`。
  - 消息转换器 `MappingJackson2HttpMessageConverter` → `JacksonJsonHttpMessageConverter`。
  - login-security pom 显式增加 `httpclient5`。
- `Oauth2Config`：`DefaultAuthorizationCodeTokenResponseClient` 在 Security 7 已删除，改为 `RestClientAuthorizationCodeTokenResponseClient`。

### 3.5 Jackson 3

- 16 个文件中的 `com.fasterxml.jackson.databind.*` / `datatype.jsr310.*` 改为 `tools.jackson.*`；`com.fasterxml.jackson.annotation` 注解包保持不变。
- [RequestResponseFormatConfig.java](file:///C:/Users/kewen/Projects/kewen-framework/boot/basic-spring-boot-starter/src/main/java/com/cu/framework/boot/basic/config/RequestResponseFormatConfig.java)：
  - `Jackson2ObjectMapperBuilderCustomizer` → `JsonMapperBuilderCustomizer`。
  - `serializerByType` / `deserializerByType` 改为 `SimpleModule` 注册；日期格式用 `defaultDateFormat`。
  - Jackson 3 已把 Java 8 时间类型并入 databind（`tools.jackson.databind.ext.javatime.*`），不再需要单独的 jsr310 模块。

### 3.6 其它代码修正

- [AsyncConfig.java](file:///C:/Users/kewen/Projects/kewen-framework/boot/basic-spring-boot-starter/src/main/java/com/cu/framework/boot/basic/config/AsyncConfig.java)：Boot 4 会额外注册一个包装用的 `AsyncConfigurer`，与项目自己的实现冲突。改为提供 `applicationTaskExecutor` Bean，并用 `@AutoConfiguration(before = TaskExecutionAutoConfiguration.class)` 让 Boot 跳过自己的执行器与包装层。
- `FileResponseBodyAdvance`：`sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl` → 标准 `java.lang.reflect.ParameterizedType`（JDK 17+ 模块系统不允许访问 `sun.*`）。
- `SpringBootLifeCycleListener`：`ConfigurableBootstrapContext` 改到 `org.springframework.boot.bootstrap`；`started` / `running` 改为 `started(context, Duration)` / `ready(context, Duration)`。

### 3.7 测试与 sample

- `@AutoConfigureMockMvc` 改到 `org.springframework.boot.webmvc.test.autoconfigure`，两个 sample 增加 `spring-boot-starter-webmvc-test`。
- 3 个 sample 的 `application.yml` 删除 `spring.session.store-type`（Boot 3 起已移除）。
- Boot 4 的 `spring-boot-starter-test` 带 JUnit Jupiter 6；现有 JUnit 5 测试包名不变，JUnit 4 测试按模块保留。
- `mvn clean test` 已开启并使用 `-parameters`。

### 3.8 移除项

- `boot/magic-spring-boot-starter` 模块整体移除：magic-api 2.2.2 只声明兼容 Boot 3.4，无 Boot 4 支持，且该模块是实验性示例。理由已写入 [docs/decisions/已废弃实现与决策记录.md](file:///C:/Users/kewen/Projects/kewen-framework/docs/decisions/已废弃实现与决策记录.md) 第八节。
- springfox 2.9.2 / swagger-bootstrap-ui 1.9.6 从依赖管理移除；Boot 4 下它们无法工作。

## 四、为什么这样选

| 决策 | 理由 | 放弃的替代方案 |
| --- | --- | --- |
| Java 21 | Temurin 17 免费更新到 2027-10，21 到 2029-12；21 的第三方库兼容性最稳 | Java 17：一年内又要迁 JDK；Java 25：作为框架库，下游兼容性不如 21 |
| Boot 4.1.2 | 3.5 的 OSS 维护已于 2026-06 结束；4.1 是当前主线，下一大版本预计 2028 | Boot 3.5：落地即 EOL，1–2 年内还要再跨一次大版本 |
| Jackson 3 直迁 | `spring-boot-starter-web` 默认带 Jackson 3；兼容模块会让两套 JSON 栈并存，且把同一份迁移推迟到以后 | `spring-boot-jackson2` 兼容模块：暂缓迁移，但增加双栈复杂度 |
| MP 3.5.18.1 | boot4 starter 明确以 Boot 4.1.0 为目标；自带 JSQLParser 4.9，顺带修掉旧版 4.7 与 5.0 的错配 | 留在 3.5.8 的 boot3 starter：Boot 4 下自动配置模块已变，不保证可用 |
| 保留 Cloud / Alibaba BOM | 代码零使用，但下游可能依赖 parent 管理 `feign-core` 等版本 | 删除 BOM：更干净，但属于对外契约变更 |
| 移除 magic-api | 无 Boot 4 支持；实验模块，不承载核心能力 | 单独留 Boot 3.5 子工程：引入第二套 BOM 与维护成本 |
| OpenSAML 5 | Security 7.1 只保留 OpenSAML 5；OpenSAML 4 已移除 | 无（Security 7 下的唯一选择） |
| `-parameters` | Spring 6.1+ 移除了 LocalVariableTable 参数名发现，`@RequestParam` 不写名字时依赖编译期参数名 | 给每个参数手写名字：改动面更大且容易漏 |

## 五、验证结果

| 验证项 | 结果 |
| --- | --- |
| `mvn -B clean test`（JDK 21） | 24 个模块全部 SUCCESS |
| SQL 兼容（H2 2.4.240） | `scripts/check-sql-compat.sh` 两遍均通过 |
| 配置文档 | `scripts/check-config-docs.sh` 通过 |
| OpenSpec | `openspec validate --strict` 通过 |
| 7 个 H2 sample | 全部启动成功；basic / tenant / auth-core / login 关键端点 200，security 未登录 401（符合预期），storage / auth-rbac 服务正常 |
| exec jar | `auth-rbac-boot-sample` 的 `layout=ZIP` 产物可 `java -jar` 启动 |
| SAML metadata | login sample 启动时成功解析 IdP metadata |
| MySQL profile | **未验证**（需要本地 MySQL 凭据与库） |
| SSO 端到端 | **未验证**（需要启动 idp-simple 并走浏览器授权） |

## 六、下游使用方升级指南

这是 BREAKING 变更，不能作为补丁版本直接替换。使用方需要：

1. **JDK 21**：项目 SDK、CI、运行镜像全部切到 21。
2. **Jakarta**：自定义 `Filter`、`Servlet`、校验注解从 `javax.*` 改 `jakarta.*`。
3. **编译加 `-parameters`**：Spring 6.1+ 必需；Maven 设置 `<maven.compiler.parameters>true</maven.compiler.parameters>`，IDE 同步开启。
4. **依赖坐标**：
   - MySQL 驱动换 `com.mysql:mysql-connector-j`。
   - MyBatis-Plus 换 `mybatis-plus-spring-boot4-starter`，并显式引入 `mybatis-plus-extension`、`mybatis-plus-jsqlparser-4.9`（如用分页）。
   - 不再从本框架获得 springfox 与 magic-api 的版本管理；需要接口文档请自行引入 springdoc，需要 magic-api 请自行管理。
5. **Spring Security 自定义**：`WebSecurityConfigurerAdapter` 已删除；自定义 `AbstractAuthenticationFilterConfigurer` 必须用 `http.with(...)`，不能用 `http.apply(...)`。
6. **SAML**：如果使用 SAML，需要在构建环境能访问 Shibboleth 仓库。
7. **本地 H2 文件库**：H2 2.4 的 write format 是 3；旧文件（format 2）不能直接打开，需要重建或迁移。

## 七、注意事项与遗留

### 7.1 必须保留的配置

- 根 pom 的 `maven.compiler.parameters=true` **不要删**。删掉后 `@RequestParam` / `@PathVariable` 不写名字的接口会在运行期 500。
- 根 pom 的 Shibboleth 仓库 **不要删**。删掉后 SAML 依赖无法解析。
- `AsyncConfig` 的 `@AutoConfiguration(before = TaskExecutionAutoConfiguration.class)` 与 `applicationTaskExecutor` Bean **不要还原成**只实现 `AsyncConfigurer` 的写法，否则 Boot 4 下会启动失败。

### 7.2 已知遗留

- **代码生成器模板仍输出 springfox 注解**：`other/code-generator/src/main/resources/templates/controller.java.vm` 与 `entity.java.vm` 仍引用 `io.swagger.annotations.*`。本仓库不再管理 springfox，生成出来的项目需要自行加 `io.swagger:swagger-annotations`，或把模板改成 springdoc 的 `io.swagger.v3.oas.annotations`。这是本次变更的已知遗留。
- `PropertiesUrlSecurityCustomizer` 与 `MenuApiGeneratorProcessor` 仍调用已废弃的 `RequestMappingInfo.getPatternsCondition()`，编译有警告；保留了 Ant 匹配兼容分支，后续可单独清理。
- `.idea/` 里仍有 magic 模块的旧索引（`compiler.xml` / `encodings.xml` / `workspace.xml`）。按惯例不手改 `.idea`，IDEA 重新导入 Maven 工程后会自行清理。
- `TokenSessionRequestFilter` 仍是 `@Deprecated`，本次只做了 Jakarta 适配，没有删除。
- JUnit 4 与 JUnit 5 仍按模块分开，没有统一。

### 7.3 环境与数据

- 本机已安装 Temurin JDK 21 到 `C:\Java\jdk-21`；全局 `JAVA_HOME` 仍是 JDK 17，IDEA 里需要把项目 SDK 切到 21。
- `sample/auth-core-boot-sample/data/auth_core_sample.mv.db` 是 H2 2.1 格式，已备份为 `.h2v2.bak`；sample 已用新库启动。其他使用 H2 文件库的工程也要做同样处理。
- 当前分支 `upgrade/jdk21-boot4`，尚未提交、未合并。

## 八、回滚

- 代码：按阶段 revert 本分支提交即可；本变更没有修改仓库内的数据库 schema。
- 本地 H2 数据：`auth_core_sample.mv.db.h2v2.bak` 可直接改回原名。
- 下游：由于是 BREAKING 变更，回滚需要连同 JDK / Jakarta 改动一起回滚，不建议只回退依赖版本。

## 九、相关文档

- 过程与决策：[proposal.md](file:///C:/Users/kewen/Projects/kewen-framework/openspec/changes/upgrade-jdk21-boot4/proposal.md)、[design.md](file:///C:/Users/kewen/Projects/kewen-framework/openspec/changes/upgrade-jdk21-boot4/design.md)、[tasks.md](file:///C:/Users/kewen/Projects/kewen-framework/openspec/changes/upgrade-jdk21-boot4/tasks.md)
- 配置参考：[docs/application.yml](file:///C:/Users/kewen/Projects/kewen-framework/docs/application.yml)
- 决策记录：[docs/decisions/已废弃实现与决策记录.md](file:///C:/Users/kewen/Projects/kewen-framework/docs/decisions/已废弃实现与决策记录.md)
- SAML / SSO：[saml2.md](file:///C:/Users/kewen/Projects/kewen-framework/boot/login-security-spring-boot-starter/docs/saml2.md)、[sso-overview.md](file:///C:/Users/kewen/Projects/kewen-framework/boot/login-security-spring-boot-starter/docs/sso-overview.md)
