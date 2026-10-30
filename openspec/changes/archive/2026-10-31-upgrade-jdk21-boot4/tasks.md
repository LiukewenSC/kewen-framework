# Tasks

## 1. 依赖基线与 magic-api 摘除

- [x] 1.1 根 `pom.xml` 切基线：`maven.compiler.release=21`；Boot BOM 4.1.2、Spring Cloud 2025.1.4、SCA 2025.1.0.0；MyBatis-Plus 3.5.18.1、JSQLParser 4.9；MySQL 坐标改 `com.mysql:mysql-connector-j`；第三方版本按 design D1/D5 更新；新增 Shibboleth 仓库（OpenSAML 5 不在 Central/阿里云）。验证：`mvn -B validate` 通过，`mvn -B -pl basic dependency:tree -Dincludes=org.springframework.boot:spring-boot` 显示 4.1.2。
- [x] 1.2 25 个子 `pom.xml`：删除各自的 `maven.compiler.source/target=8`（security sample 的 17 一并删除），继承 release 21；`javax.servlet-api`/`validation-api` 换 `jakarta.*`；`mybatis-plus-boot-starter` → `mybatis-plus-spring-boot4-starter`；`mybatis-spring-boot-starter` 2.3.1 → 4.1.0；`mysql:mysql-connector-java` → `com.mysql:mysql-connector-j`；`spring-boot-maven-plugin` 2.5.12/2.7.18 → 4.1.2。验证：`mvn -B validate` 通过，抽查 basic / login-security / auth-rbac-security-boot-sample 的 `dependency:tree` 无 javax 坐标、无旧 mysql 坐标、无 2.x 插件。
- [x] 1.3 移除 `boot/magic-spring-boot-starter`：从 `boot/pom.xml` modules 与根 dependencyManagement 删除，删除目录；在 `docs/decisions/已废弃实现与决策记录.md` 记录移除理由（magic-api 2.2.2 无 Boot 4 支持、实验模块）。验证：`grep -rn "magic" --include=pom.xml .` 无结果，`openspec` 之外的生产代码无 `org.ssssssss` 引用，决策记录含该条目。
- [x] 1.4 更新 `README.md` / `README.en.md`：JDK 21、Boot 4.1.x、Security 7.x、MP 3.5.18 的徽章与版本表；删除 magic-api 模块行。验证：`grep -nE "JDK|Spring Boot|magic" README.md README.en.md` 与目标一致。
- [x] 1.5 框架版本从 `0.0.1` 升到 `0.2.1`：根 pom 的 `version` 与 `framework-version`、24 个子 pom 的 parent 版本、README/sample README 的依赖与 exec jar 示例。验证：`grep -rn "0\.0\.1"`（排除 IP 与 archive）无结果，`mvn -B clean install` 通过并产出 0.2.1 产物。

## 2. Jakarta / Jackson 3 / 自动配置清单

- [x] 2.1 44 个 `javax.servlet` / `javax.validation` 文件改 `jakarta.*`（basic 的 filter/logger/utils、auth-core 的 menu/data、login-security 的 filter/session/SAML controller、storage 的请求模型、samples）。验证：`grep -rl "javax\.servlet\|javax\.validation" --include=*.java .` 无结果。
- [x] 2.2 非 login-security 模块的 Jackson 2 → 3：`basic`、`basic-support`、`auth/*` 中的 `ObjectMapper` / `JsonNode` / `datatype.jsr310` / `JsonSerialize` 改 `tools.jackson.*`；`com.fasterxml.jackson.annotation` 注解包保持不动。验证：`grep -rn "com.fasterxml.jackson.databind\|datatype.jsr310" basic basic-support auth` 无结果。
- [x] 2.3 `RequestResponseFormatConfig` 的 `Jackson2ObjectMapperBuilderCustomizer` 改 `org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer`。验证：`mvn -pl boot/basic-spring-boot-starter -DskipTests compile` 通过。
- [x] 2.4 7 个 starter 的 `spring.factories`（仅 EnableAutoConfiguration 部分）迁到 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`，配置类改 `@AutoConfiguration`；`sample/basic-boot-sample` 的 `SpringApplicationRunListener` / `SpringBootExceptionReporter` 继续留在 `spring.factories`。验证：7 个 imports 文件存在，`grep -rn "EnableAutoConfiguration" --include=spring.factories .` 无结果。
- [x] 2.5 在 `docs/decisions/已废弃实现与决策记录.md` 记录 Jackson 3 直迁（不使用 `spring-boot-jackson2` 兼容模块）的理由。验证：条目存在且与 design D3 一致。
- [x] 2.6 运行非安全模块测试：`mvn -pl basic,basic-support,auth/auth-core,auth/auth-rbac,boot/basic-spring-boot-starter,boot/storage-spring-boot-starter,boot/tenant-spring-boot-starter test` 通过。

## 3. Spring Security 7 + SAML + HttpClient 5（login-security 模块）

- [x] 3.1 重写 `SecurityConfig`：删 `WebSecurityConfigurerAdapter` 与 `configure(AuthenticationManagerBuilder)`；改 `SecurityFilterChain` + `WebSecurityCustomizer`；`authorizeRequests().antMatchers(...)` → `authorizeHttpRequests(auth -> auth.requestMatchers(...).permitAll().anyRequest().authenticated())`；`csrf` / `logout` / `exceptionHandling` / `sessionManagement` 全部改 lambda；静态资源模式从 `/**/**.html` 改为 PathPattern 合法的 `/**/*.html`。验证：`mvn -pl boot/login-security-spring-boot-starter -DskipTests compile` 通过。
- [x] 3.2 `JsonLoginAuthenticationFilterConfigurer.createLoginProcessingUrlMatcher` 改 `PathPatternRequestMatcher.pathPattern(HttpMethod.POST, loginProcessingUrl)`；`JsonLoginFilter` 删除 `MediaType.APPLICATION_JSON_UTF8_VALUE`（Framework 7 已删除），只保留 `APPLICATION_JSON_VALUE` 并补 Content-Type 为空的处理。验证：模块编译通过，登录过滤器单测通过。
- [x] 3.3 `SessionConfig` 的 `HttpSecurityCustomizer` 改 lambda（`sessionManagement(sm -> sm.maximumSessions(...).maxSessionsPreventsLogin(...))`）；确认 `SpringSessionRememberMeServices`、`HeaderCookieHttpSessionIdResolver`、`JdbcIndexedSessionRepository` 在 Session 4.1.1 下编译。验证：模块编译通过。
- [x] 3.4 SAML 迁 `OpenSaml5AuthenticationProvider`：`SecurityUserSamlAuthenticationConverter` 的 `ResponseToken` 与默认转换器改 OpenSaml5；`SamlConfig` 的 `assertingPartyDetails` → `assertingPartyMetadata`；`saml2Logout` 改 `logoutRequest(req -> req.logoutUrl(...))` / `logoutResponse(...)`；`Saml2WebSsoAuthenticationFilter` 使用 `...service.web.authentication` 包。验证：模块编译 + SAML 相关单测通过。
- [x] 3.5 `IdpMetadataParser` 迁 OpenSAML 5：`net.shibboleth.utilities.java.support.xml.BasicParserPool` → `net.shibboleth.shared.xml.impl.BasicParserPool`；确认 `UnmarshallerFactory` / `XMLObjectProviderRegistrySupport` 用法仍有效。验证：`IdpMetadataParser` 能解析 `sample/*/src/main/resources/saml/idp-metadata.xml`（单测或 sample 启动）。
- [x] 3.6 `RestOperationBuilder` 迁 HttpClient 5 + Jackson 3：`org.apache.http.*` → `org.apache.hc.client5.*` / `org.apache.hc.core5.*`；`HttpComponentsClientHttpRequestFactory` 使用 HC5 client；`MappingJackson2HttpMessageConverter` → `JacksonJsonHttpMessageConverter`；模块 pom 增加 `org.apache.httpcomponents.client5:httpclient5`；同时把 login-security 内其余 Jackson 2 import 改 Jackson 3。验证：模块编译 + `RoutingOAuth2UserServiceTest` 等 OAuth2 单测通过。
- [x] 3.7 文档同步：`boot/login-security-spring-boot-starter/docs/saml2.md` 删除“OpenSaml4 需要 JDK11+ 所以用 OpenSamlAuthenticationProvider”的旧结论，改为 OpenSaml5 / JDK 21；`docs/sso-overview.md` 更新 Spring Security 7 描述；决策记录补 RestOperationBuilder 的 HttpClient 5 说明。验证：`grep -n "5\.7\|OpenSamlAuthenticationProvider\|JDK 8"` 在相关文档中无过时表述。
- [x] 3.8 运行安全模块测试：`mvn -pl boot/login-security-spring-boot-starter,auth/auth-rbac-security-spring-boot-starter test` 通过。

## 4. 测试与 sample 配置适配

- [x] 4.1 `@AutoConfigureMockMvc` 改 `org.springframework.boot.webmvc.test.autoconfigure`（2 个测试基类），对应 sample pom 增加 `spring-boot-starter-webmvc-test`。验证：`mvn -pl sample/basic-boot-sample,sample/auth-rbac-boot-sample test` 通过。
- [x] 4.2 3 个 sample 的 `application.yml` 删除 `spring.session.store-type`；两个 starter README 同步删除该片段。验证：`grep -rn "store-type" --include=*.yml --include=*.md .`（排除 `.qoder` 与 `openspec/changes/archive`）无结果。
- [x] 4.3 适配 Boot 4 的 JUnit Jupiter 6：跑全量测试，按报错修 import 或断言 API。验证：全量 `mvn test` 通过。
- [x] 4.4 验证 3 个 sample 的 `spring-boot-maven-plugin` repackage：`mvn -pl sample/basic-boot-sample,sample/auth-rbac-boot-sample,sample/auth-rbac-security-boot-sample package -DskipTests`；确认 `layout=ZIP` 产物可启动；若 4.1.2 不支持 ZIP，则改默认 JAR layout 并同步 pom。验证：产物存在且 `java -jar` 能启动到端口监听。

## 5. 集成验证

- [x] 5.1 跑 `bash scripts/check-sql-compat.sh`，修复 H2 2.4.240 不接受或不可重复执行的 DDL/seed（如有）。验证：脚本通过。
- [x] 5.2 逐个启动 H2 profile 的 sample（basic / tenant / storage / auth-core / auth-rbac / login / auth-rbac-security），确认启动无异常、H2 控制台与关键端点可访问。验证：每个 sample 的启动日志 + 一次 HTTP 请求。
- [ ] 5.3 MySQL profile 冒烟（未验证：需要本地 MySQL 凭据与库）：`auth-rbac-boot-sample` 与 `basic-boot-sample` 连本地 MySQL，验证 P6Spy、分页与初始化脚本。验证：关键接口返回预期。
- [ ] 5.4 用本机 `kw-idp-simple`（未验证：需要启动 IDP 并走浏览器授权流程）（9010）回归 SSO：OAuth2、OIDC、SAML 各完成一次登录，确认回调后主体与权限来自本地账号。验证：三条链路均成功，日志无异常。
- [x] 5.5 跑 `bash scripts/check-config-docs.sh`，确认 `docs/application.yml` 覆盖所有配置键。验证：脚本通过。
- [x] 5.6 跑 `openspec validate upgrade-jdk21-boot4 --strict` 确认变更可归档。验证：validate 通过。
