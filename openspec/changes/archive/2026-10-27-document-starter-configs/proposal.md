# Proposal: document-starter-configs

## Why

框架的 7 个 starter 都已具备 `spring.factories` 自动装配入口，但"配置面"几乎没有可交付的说明：`login-security-spring-boot-starter` 有一份详细 README，`auth-*` 三个 starter 的 README 只有 3~5 行，`basic` / `storage` / `tenant` 三个 starter 完全没有 README；仓库里仅有的两份示例配置（根目录 `application.yml` 与 `docs/application.yml`）均已过期——包含已移除的 SAML 字段、`cu.message.*` 旧前缀、`cu.storage.qiniu.*` 错误层级和 `maxSessions-prevents-login` 错误大小写，照抄无法启动。

结果是使用方无法从仓库回答三个问题：这个 starter 有哪些配置键、默认值是什么、哪些键配了其实不生效。前两个问题只能靠读代码，第三个问题（`current-user-url`、`storage.type`、OAuth2 的 `authorization-grant-type` / `user-info-authentication-method`、`saml.login-page-url`，以及只建 Bean 未挂链的 remember-me）没有任何地方说明。

## What Changes

**文档（本次主体）**

- 为 6 个缺少说明的 starter 新建/扩写 `README.md`：`basic`、`storage`、`tenant` 新建；`auth-core`、`auth-rbac`、`auth-rbac-security` 从现有 3~5 行扩写。每份 README 固定四段：自动装配清单（配置类 / 生效条件 / 产出）、配置表（key / 默认值 / 必填 / 行为）、启动前置条件与已知空转项、指向全量示例对应小节。
- `login-security-spring-boot-starter/README.md` 只补缺口：顶层 `cu.security.*`、`cu.auth.permit-url`、7 个装配类的清单；三个协议已有的详细章节不动。
- 重写 `docs/application.yml` 为全仓库唯一的全量参考示例：按 starter 分节，覆盖全部配置键；有默认值的写实，无默认值的留空并标注必填；每个键一行注释，写清作用、默认/必填，以及"未接线 / 已废弃"的事实。
- 删除根目录 `application.yml`，同步修正 `README.md` 目录树；修正 `README.md` 配置指南中的 `cu.storage.qiniu.*`、`cu.message.*` 等错误键名，并改为指向各 starter README 与 `docs/application.yml`。
- 示例定位为"参考手册"而不是可启动基线：`cu.security.login.oauth2.clients` 与 `saml.registrations` 的必填性、留空会 NPE 的后果在注释中写明；可启动配置继续由 `sample/*/application.yml` 承担。

**配置元数据（让 IDE 里的说明也准确）**

- 为 `cu.auth.permit-url` 新增 `additional-spring-configuration-metadata.json`（`login-security-spring-boot-starter`），补上目前缺失的 IDE 补全。
- 将 `cu.auth.cache-auth` 的 metadata 从 `auth-core-spring-boot-starter` 迁到实际消费它的 `auth-rbac-spring-boot-starter`。
- 补齐/修正属性类 javadoc：`FangTangProperties.enabled`（当前无描述）、`StorageProperties.type`、`SecurityProperties.currentUserUrl`、`Oauth2OidcProperties.authorizationGrantType` / `userInfoAuthenticationMethod`（后四个标注"当前未读取"）。
- 新增 `scripts/check-config-docs.sh`：从各模块生成的 `spring-configuration-metadata.json` 提取键集合，断言全部出现在 `docs/application.yml`，给后续新增配置项一个防漂移闸门。

**范围外**

- 不改任何自动装配行为：login starter 的 `clients` / `registrations` 仍保持"必须至少一条"；如要让 starter 零配置可启动，另开 change。
- 不迁移 `spring.factories` 到 Boot 3 的 `AutoConfiguration.imports`；本次只统一 7 个 `spring.factories` 的格式（空格、缩进、行尾续行）。
- 不一并修复 remember-me 未挂链、`current-user-url` 无消费方等实现缺口；本次只在文档里如实标注。
- 不改 `sample/*` 的配置与 README；`magic-spring-boot-starter` 没有 `spring.factories`、模块内是 demo 启动类，不是自动装配 starter，不在本次范围。

## Capabilities

无。纯文档与配置元数据变更，不改变任何运行时行为；`.openspec.yaml` 设 `skip_specs: true`。

## Impact

- **文档**：7 个 starter README、`docs/application.yml`（重写）、`README.md`（目录树 + 配置指南）
- **删除**：根目录 `application.yml`（非打包产物，无运行时依赖）
- **配置元数据**：`login-security-spring-boot-starter` 新增 additional metadata；`auth-rbac-spring-boot-starter` 新增、`auth-core-spring-boot-starter` 移除 `cu.auth.cache-auth` 条目
- **属性类 javadoc**：`FangTangProperties`、`StorageProperties`、`SecurityProperties`、`Oauth2OidcProperties`（仅注释，不改字段与默认值）
- **脚本**：新增 `scripts/check-config-docs.sh`，读取各模块 `target/classes/META-INF/spring-configuration-metadata.json`
- **验收**：`mvn -q -DskipTests compile` 通过；脚本在现有键集上通过；`docs/application.yml` 可被 YAML 解析器读取
