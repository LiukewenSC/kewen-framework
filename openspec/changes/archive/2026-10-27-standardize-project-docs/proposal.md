# Proposal

## Why

项目准备开源，但同一件事在根 README、README_Auth、模块 README、示例 README 和 docs/ 里被写了多份，且反复过期：登录协议文档与代码的枚举、会话存储方式不一致，权限说明有新旧两套配置键，SQL 散在四个位置。读者无法判断哪份是权威版本，维护者每次改动都要同步多处。现在需要先定下一套文档与资源的归属规则，再按规则重写。

## What Changes

- 确立一条文档规则：每个 Maven 模块只有 `README.md` 一个入口，一屏内回答“是什么 / 何时用 / 怎么跑起来 / 下一步读哪里”；进阶细节放同模块 `docs/`；根 `README.md` 只做门面与导航；根 `docs/` 只放跨模块资料。
- 重写根 README 为开源门面：项目定位、模块地图、两条快速开始（H2 示例零配置 / 接入自有项目）、文档导航。删除 `README_Auth.md`，把仍有价值的权限细节拆入 `auth/README.md` 与 `auth/docs/`。
- 补齐缺失的模块文档：`basic`、`basic-support`、`auth-rbac`、`magic`、`code-generator`、`scripts`，以及 `basic` / `login` / `storage` / `tenant` 四个示例；已有模块 README 增加“5 分钟接入”段。
- 把框架产品 DDL 统一移到根目录 `relation/{模块}/`：`relation/auth-rbac/auth-rbac.sql`、`relation/basic-support/basic-support.sql`、`relation/login-security/spring-session-mysql.sql`。**示例专用 DDL 保持在 `sample/*/sql/`**，不随框架发布。删除 `docs/sql/`。
- 重写单点登录文档为“1 篇通俗总览 + 3 篇协议细节”：`docs/sso-overview.md`、`docs/oauth2.md`、`docs/oidc.md`、`docs/saml2.md`。删除 `README-SSO.md`、`docs/说明文档/oidc接入踩坑记录.md`、`docs/说明文档/SAML2_SP_Metadata配置指南.md`，内容并入新文档。
- 保留 `docs/说明文档/已废弃实现与决策记录.md`，移到 `docs/decisions/`；只追加一条本次迁移说明，不回改历史结论。
- 统一文案风格：中文正文、ASCII 图、固定配置表列、`[未接线]` / `[已废弃]` / `[实验性]` 标记、不写“主要变化点/更新日志”、示例密钥只用占位符。
- 删除过期的 `auth/relation/properties/application-sample.yml`（仍使用 `cu.security.login.login-url` 旧键，配置参考以 `docs/application.yml` 为准）。
- **BREAKING（仅文档链接与仓库路径）**：`README_Auth.md`、`README-SSO.md`、`docs/sql/*`、`docs/说明文档/*` 的旧路径不再存在。无 Java API、配置键语义或运行时行为变化。

## Capabilities

### New Capabilities

无。本次是文档与仓库布局改造，不改变任何运行时行为；`.openspec.yaml` 已设置 `skip_specs: true`，不新增 capability spec。

### Modified Capabilities

无。现有 `auth/*`、`identity/*`、`basic/*`、`observability/*`、`storage/*`、`sample/*` 的规格不变。

## Impact

- 文档：根 README、README_Auth、8 个模块/starter README、2 个示例 README、3 篇 `docs/说明文档`，以及新增的模块 `docs/` 与示例 README。
- 构建：`sample/pom.xml`、`sample/auth-rbac-boot-sample/pom.xml` 的 `<resources>` 挂载路径；`scripts/check-sql-compat.sh` 的扫描路径。
- 配置注释：`docs/application.yml` 与各 sample `application.yml` 中的 SQL 路径说明。
- 不涉及：Java 代码、依赖版本、运行时行为、`.qoder/repowiki`、`openspec/`、LICENSE、`.gitee` 模板。
- 安全前置：`README_Auth.md` 含真实形态的 RDS 主机/账号/口令，删除前需由项目所有者轮换凭据；本 change 只负责从文档中移除。
