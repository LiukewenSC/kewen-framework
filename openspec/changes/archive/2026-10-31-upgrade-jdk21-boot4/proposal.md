# Proposal

## Why

框架仍停留在 Spring Boot 2.7.18 / Spring Security 5.7.11 / Java 8：2.7 的 OSS 维护已于 2023-11 结束，Spring Framework 5.3 的 OSS 维护停在 2024-08，Spring 侧已约 2 年没有安全补丁；JDK 17 的免费更新也只剩到 2027-10。

继续停留在 3.x 同样不划算：Spring Boot 3.5 的 OSS 维护已于 2026-06 结束，而 Spring Boot 的每个 minor 只维护约 13 个月。要避免反复跨大版本迁移，应直接落到当前 OSS 主线 Spring Boot 4.1（下一大版本预计 2028 年），并选一个能长期收到补丁的 JDK LTS。

## What Changes

- **BREAKING** 运行与编译基线从 Java 8 提升到 **Java 21**（`maven.compiler.release=21`），所有框架模块与 sample 统一。
- **BREAKING** Spring Boot 2.7.18 → **4.1.2**（Spring Framework 7.0.10 / Spring Security 7.1.2 / Spring Session 4.1.1 / Tomcat 11 / Jackson 3 / Hibernate Validator 9）。
- **BREAKING** 全部 `javax.servlet`、`javax.validation` 迁移到 `jakarta.*`；显式声明 `javax.servlet-api` / `validation-api` 的模块改换 Jakarta 坐标。
- **BREAKING** 7 个 starter 的自动配置从 `META-INF/spring.factories` 迁移到 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`，配置类改用 `@AutoConfiguration`。
- **BREAKING** Spring Security 配置重写：`WebSecurityConfigurerAdapter` 已删除，改用 `SecurityFilterChain` + 全 lambda DSL（`authorizeHttpRequests`、`requestMatchers`）；自定义登录匹配器改用 `PathPatternRequestMatcher`。
- **BREAKING** SAML：`OpenSaml4AuthenticationProvider` 已删除，换 `OpenSaml5AuthenticationProvider`；`RelyingPartyRegistration.Builder.assertingPartyDetails(...)` 改为 `assertingPartyMetadata(...)`；`saml2Logout` 的 `logoutRequest` / `logoutResponse` 改为 lambda 形式。
- **BREAKING** Jackson 2 迁移到 Jackson 3：`ObjectMapper` / `JsonNode` / 日期序列化器 / `MappingJackson2HttpMessageConverter` 改用 `tools.jackson.*` 与 `JacksonJsonHttpMessageConverter`（注解包 `com.fasterxml.jackson.annotation` 保持不变）。
- OAuth2 userinfo 的 HTTP 客户端从 Apache HttpClient 4 迁移到 **HttpClient 5**，新增 `httpclient5` 依赖。
- 持久层：MyBatis-Plus 3.5.8 → **3.5.18.1** + `mybatis-plus-spring-boot4-starter`；JSQLParser 对齐到 **4.9**（修掉当前 4.7 与 MP 自带 5.0 的错配）；MySQL 驱动换 `com.mysql:mysql-connector-j`。
- 移除 `boot/magic-spring-boot-starter` 模块：magic-api 2.2.2 只声明兼容 Spring Boot 3.4，属实验模块，不再维护 Boot 4 适配。
- 测试适配：`@AutoConfigureMockMvc` 移到 `org.springframework.boot.webmvc.test.autoconfigure`，相关 sample 增加 `spring-boot-starter-webmvc-test`；JUnit 4/5 分模块的现状保持不变。
- 同步 README、starter README、SAML/SSO 文档与版本表。

## Capabilities

### New Capabilities

无。纯平台升级，不新增业务能力。

### Modified Capabilities

无。所有既有 spec 描述的行为（鉴权、数据权限、登录交付、异常契约、存储等）保持不变；本次只更换运行平台与依赖，属 `skip_specs: true` 的迁移类变更。

## Impact

- **代码**：25 个 `pom.xml`、44 个含 `javax.*` 的 Java 文件、7 个 starter 的自动配置清单、3 个 sample `application.yml`、约 10 处文档。
- **对外契约**：starter 的 groupId/artifactId 不变，但下游使用方必须升级到 JDK 21 + Jakarta；自定义 `javax.servlet.Filter`、`javax.validation` 注解需同步迁移。
- **移除**：`boot/magic-spring-boot-starter` 从 reactor 与依赖管理中摘除，目录删除并在决策记录中保留移除理由。
- **验证**：模块单测 → H2 sample 启动 → MySQL profile → `kw-idp-simple`（9010）SSO 端到端回归。
