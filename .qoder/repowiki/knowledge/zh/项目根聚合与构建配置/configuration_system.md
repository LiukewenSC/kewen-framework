## 1. 采用的方案

项目基于 Spring Boot 原生的 `@ConfigurationProperties` + YAML 配置文件，没有引入第三方配置中心（如 Nacos/Apollo）。所有 starter 模块通过 Lombok + `@ConfigurationProperties` 声明可配置属性，并在 `META-INF/additional-spring-configuration-metadata.json` 中补充元数据，由 Maven 编译生成 `spring-configuration-metadata.json`，供 IDE 提示与后续文档校验。

## 2. 关键文件

- 参考手册：`docs/application.yml` —— 聚合了所有 starter 的配置键、默认值与标记约定（`[必填]` / `[未接线]` / `[已废弃]`），并说明该文件是参考手册而非可直接启动的配置。
- 属性类（全部使用 `@ConfigurationProperties("cu.*")`）：
  - `boot/basic-spring-boot-starter/.../properties/FangTangProperties.java` → `cu.notify.fang-tang`
  - `boot/storage-spring-boot-starter/.../StorageProperties.java` → `cu.storage`
  - `auth/auth-core-spring-boot-starter/.../AuthDataTableProperties.java` → `cu.auth.auth-data-table`
  - `boot/login-security-spring-boot-starter/.../properties/SecurityProperties.java` → `cu.security`
  - `boot/login-security-spring-boot-starter/.../properties/SessionProperties.java` → `cu.security.session`
  - `boot/login-security-spring-boot-starter/.../properties/RememberMeProperties.java` → `cu.security.remember-me`
  - `boot/login-security-spring-boot-starter/.../properties/SsoLoginProperties.java` → `cu.security.login.sso`
  - `boot/login-security-spring-boot-starter/.../oauth2/Oauth2OidcProperties.java` → `cu.security.login.oauth2`
  - `boot/login-security-spring-boot-starter/.../saml/properties/SamlProperties.java` → `cu.security.login.saml`
  - `boot/login-security-spring-boot-starter/.../password/properties/SecurityLoginProperties.java` → `cu.security.login.password`
- 元数据补充：各 starter 的 `src/main/resources/META-INF/additional-spring-configuration-metadata.json`（basic、login-security、tenant、auth-rbac 四个 starter 均有）。
- 文档校验脚本：`scripts/check-config-docs.sh` + `scripts/CheckConfigDocs.java`。
- 示例应用配置：`sample/*/src/main/resources/application.yml`（H2 profile 与 MySQL profile 通过 `spring.profiles.active` 切换）。

## 3. 架构与约定

### 3.1 属性命名空间
所有框架暴露的属性统一以 `cu.` 为根前缀，按 starter 再细分二级前缀：
- `cu.request.*`、`cu.notify.*` — basic-spring-boot-starter
- `cu.storage.*` — storage-spring-boot-starter
- `cu.tenant.*` — tenant-spring-boot-starter
- `cu.auth.*` — auth-core / auth-rbac / login-security 三个 starter 共用（例如 `cu.auth.auth-data-table.table-name`、`cu.auth.cache-auth`、`cu.auth.permit-url`）
- `cu.security.*` 及其子路径 `cu.security.session`、`cu.security.remember-me`、`cu.security.login.password|sso|oauth2|saml` — login-security-spring-boot-starter

### 3.2 属性定义模式
每个可配置项都对应一个 Lombok `@Data` 的 Java Bean，用 `@ConfigurationProperties("cu.xxx")` 绑定。字段有默认值的直接在 Java 中赋初值（如 `FangTangProperties.domain = "https://sctapi.ftqq.com"`、`SecurityProperties.currentUserUrl = "/currentUser"`）；无默认值的留空，由 `docs/application.yml` 中的注释标注其语义和是否必填。

### 3.3 Profile 与环境切换
示例应用通过 `spring.profiles.active: h2` 或 `mysql` 切换数据源，并使用 YAML 多文档段 `---` 配合 `spring.config.activate.on-profile` 组织不同环境配置。`docs/application.yml` 明确区分“本地 H2 内存库”和“本地 MySQL”，后者不执行建表脚本，需手动执行 `relation/basic-support/basic-support.sql`。

### 3.4 元数据与文档同步机制
Maven 编译后，Lombok + `@ConfigurationProperties` 会生成 `spring-configuration-metadata.json`。部分手工维护的属性（如 `additional-spring-configuration-metadata.json` 中的 `cu.request.persistent.database`、`cu.request.message.fang-tang`）需要额外补充元数据。`scripts/check-config-docs.sh` 在仓库根执行 `mvn -q -DskipTests compile` 后，调用 `CheckConfigDocs.java` 遍历所有模块生成的 `target/classes/META-INF/spring-configuration-metadata.json`，对比 `docs/application.yml` 中是否存在对应的扁平化 YAML 路径，缺失则退出码非 0。

注意：脚本明确说明 List 元素字段（`clients[]`、`registrations[]`）不在 metadata 中，不参与校验，需要人工核对——这是约束的已知边界。

## 4. 约定与约束

- **属性命名空间**：所有框架属性必须以 `cu.` 开头，并按 starter 划分二级前缀（`cu.storage`、`cu.security`、`cu.auth`、`cu.tenant`、`cu.notify`、`cu.request`），见各 `@ConfigurationProperties` 注解及 `docs/application.yml` 的分节结构。
- **属性定义位置**：每个可配置属性类必须放在各自 starter 的 `properties` 包下，使用 Lombok `@Data` + `@ConfigurationProperties`，默认值写在 Java 字段上（见 FangTangProperties、SecurityProperties、SsoLoginProperties 等实现）。
- **文档即契约**：`docs/application.yml` 是唯一的官方配置参考手册，其中对每个键标注 `[必填]`、`[未接线]`、`[已废弃]`。新增属性必须在 `docs/application.yml` 中登记，并由 `scripts/check-config-docs.sh` 强制校验——脚本会扫描所有模块编译产物中的 `spring-configuration-metadata.json`，如果存在未在 YAML 中出现的路径则失败。
- **List 型属性例外**：`check-config-docs.sh` 注释明确声明 List 元素字段（如 `cu.security.login.oauth2.clients[]`、`cu.security.login.saml.registrations[]`）不会出现在 metadata 的 properties 列表中，因此不受自动校验覆盖，需要人工核对文档。
- **Profile 管理**：示例应用统一使用 `spring.profiles.active` + YAML 多文档段切换 H2/MySQL 环境，数据库连接池使用 HikariCP，P6Spy 驱动要求 `jdbc:p6spy:mysql` 前缀。
- **Secret 处理**：当前未发现专用 secrets 管理器；密钥（如七牛 access-key/secret-key、方糖 key、OAuth2 client-secret）直接以明文形式配置在 application.yml 中，属于明文配置策略。
- **Starter 装配**：starter 通过 `META-INF/spring.factories` 与 `additional-spring-configuration-metadata.json` 注册自动装配与 IDE 元数据，`docs/application.yml` 的头部注释明确说明“可启动的完整示例见 sample/*/src/main/resources/application.yml”。