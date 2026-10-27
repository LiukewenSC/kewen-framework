# Tasks

## 1. 前置安全与迁移基线

- [x] 1.1 请项目所有者轮换 `README_Auth.md` 中出现的 RDS 凭据；在得到确认前不删除该文件。验证：所有者明确确认已轮换（在 change 执行备注中记录日期），后续 6.2 删除文件时凭据不再出现在仓库。
- [x] 1.2 记录迁移前引用基线：对 `README_Auth.md`、`README-SSO.md`、`docs/sql`、`docs/说明文档`、`auth/relation` 做一次 `grep -rn` 清单，覆盖根 README、模块/示例 README、sample poms、scripts、application.yml。验证：清单贴在 change 执行备注中，包含所有命中位置，后续逐项对照。

## 2. SQL 归位与构建/脚本同步

- [x] 2.1 创建 `relation/auth-rbac/`、`relation/basic-support/`、`relation/login-security/`，用 `git mv` 移动三个框架脚本：`docs/sql/auth-rbac.sql`、`docs/sql/basic-support.sql`、`boot/login-security-spring-boot-starter/relation/sql/spring-session-mysql.sql`。验证：`find relation -name '*.sql' | sort` 恰好三行，`git status --short` 显示 rename。
- [x] 2.2 新建 `relation/README.md`：脚本清单、每个脚本服务的模块、MySQL/H2 适用范围、执行顺序、示例 DDL 不在本目录的边界声明、`scripts/check-sql-compat.sh` 用法。验证：文档里的三个相对链接都存在；明确写出 `relation/login-security/spring-session-mysql.sql` 仅 MySQL，且示例建表在 `sample/*/sql/`。
- [x] 2.3 删除 `docs/sql/`、`auth/relation/properties/application-sample.yml`，以及空的 `auth/relation/`、`boot/login-security-spring-boot-starter/relation/` 目录。验证：`test ! -e docs/sql`、`test ! -e auth/relation`、`find boot/login-security-spring-boot-starter -maxdepth 1 -name relation` 为空。
- [x] 2.4 更新 `scripts/check-sql-compat.sh`：扫描 `relation/auth-rbac/*.sql`、`relation/basic-support/*.sql`、`sample/*/sql/*.sql`；明确排除 `relation/login-security/`；更新头注释。验证：`bash scripts/check-sql-compat.sh`（必要时 `H2_JAR=...`）通过，输出文件清单包含 2 个 relation 框架脚本和 4 个 sample 脚本，不含 spring-session。
- [x] 2.5 更新 `sample/pom.xml`：把 `../../docs/sql` 资源替换为 `../../relation/auth-rbac`、`../../relation/basic-support`、`../../relation/login-security` 三个 `<resource>`，均 `targetPath=sql`、`includes=*.sql`；更新 `sample/auth-rbac-boot-sample/pom.xml` 为 `../../relation/auth-rbac` + 本模块 `sql/`；更新 `sample/auth-core-boot-sample/pom.xml` 中提及 `docs/sql` 的注释。验证：`mvn -q -DskipTests -pl sample/auth-rbac-boot-sample,sample/basic-boot-sample,sample/storage-boot-sample -am package` 成功，且 `sample/auth-rbac-boot-sample/target/classes/sql/` 含 `auth-rbac.sql`、`schema.sql`、`seed-data.sql`，`sample/basic-boot-sample/target/classes/sql/` 含 `basic-support.sql`，`sample/auth-core-boot-sample/target/classes/sql/` 不含 `auth-rbac.sql`。
- [x] 2.6 更新 `docs/application.yml`、`sample/*/src/main/resources/application.yml` 中所有 `docs/sql/...` 路径注释为 `relation/...`；同步更新 `sample/auth-rbac-boot-sample/src/main/resources/application.yml` 的“建表脚本由 sample/pom.xml（docs/sql）”注释。验证：`grep -rn "docs/sql" --include='*.yml' --include='*.yaml' .` 返回空。
- [x] 2.7 将 `docs/说明文档/已废弃实现与决策记录.md` 移动到 `docs/decisions/`，在文末追加一条 2026-10-27 的迁移记录：框架 DDL 从 `docs/sql/` 移到 `relation/{模块}/`，示例 DDL 留在 `sample/*/sql/`，原因与 `check-sql-compat.sh` 的排除规则。验证：`docs/decisions/已废弃实现与决策记录.md` 存在且历史段落未改；`grep -n "relation/"` 能看到新记录。
- [x] 2.8 扫描并修复所有仍在引用旧 SQL 位置的活链接（根 README、模块 README、示例 README、scripts 注释、poms 注释）；`docs/decisions/` 中的历史记录保留原文。验证：`grep -rn "docs/sql" --exclude-dir=.git --exclude-dir=.qoder --exclude-dir=openspec .` 只剩 `docs/decisions/` 的历史段落；所有 `relation/` 链接目标存在。

## 3. Auth 文档

- [x] 3.1 新建 `auth/README.md`：模块地图（auth-core / auth-rbac / 三个 starter）、5 分钟接入的两条路（只引 security starter 的 RBAC 默认链路；自实现 core 接缝）、核心概念（菜单 / 数据范围 / 数据操作 / 权限编辑）、文档索引。验证：到 `auth/docs/annotations.md`、`auth/docs/rbac-and-extension.md`、三个 starter README、两个 auth 示例 README 的相对链接都存在；不复制根 README 的完整注解示例。
- [x] 3.2 新建 `auth/docs/annotations.md`：`@AuthMenu` / `@AuthMenuDoc`、`@AuthDataRange`、`@AuthDataOperation`、`AuthDataAdaptor`、`DataIdCarrier`、`AuthDataHandler`、`AuthMenuHandler`；每条写参数、声明位置、启动期校验、已知限制。验证：逐条对照 `auth/auth-core` 源码与 `openspec/specs/auth/*`；明确 `@AuthDataAuthEdit` 与 `AnnotationAuthHandler` 已不在当前代码中，不写成可用；至少覆盖路径变量恒拒绝、同路径不同 HTTP 方法共用授权、`@AuthDataRange` 只作用于 Mapper 方法、批量操作 100 条上限。
- [x] 3.3 新建 `auth/docs/rbac-and-extension.md`：RBAC 十张表与关系、权限串编码（`ROLE_` / `USER_` / `DEPT_` / `RELATIONDEPT_`）、部门继承语义、权限粒度、扩展岗位等新维度的步骤、`AuthDataHandler` / `AuthMenuHandler` / `AuthDataAdaptor` 扩展点。验证：对照 `auth/auth-rbac` 源码与 `auth/auth-core/README.md`；`RELATIONDEPT` 当前实现按严格上级链（不含自身）展开，文档写当前实现并与 `auth/dept-inherited-access` 规格一致。
- [x] 3.4 新建 `auth/auth-rbac/README.md`：定位、composites / mappers / controllers / extension 的职责、默认实现与替换点、依赖的十张表、与 auth-core / security starter 的关系。验证：对照 `auth/auth-rbac/src/main/java` 的包结构；链接到 `relation/auth-rbac/auth-rbac.sql` 与 `auth/docs/rbac-and-extension.md`。
- [x] 3.5 重写 `auth/auth-core/README.md`：保留权限体模型说明（`IAuthObject`、`SimpleAuthObject`、`UserAuthInfo`、空权限行为、`RELATIONDEPT`），增加“这是什么 / 何时用 / 最小接入 / 相关文档”，链接到 `auth/docs/annotations.md` 与 `sample/auth-core-boot-sample`。验证：模型语句与 `auth/auth-core` 源码一致；`RELATIONDEPT` 的当前/目标语义按 3.3 的口径处理；不出现 starter 专属的“自动装配”段落。
- [x] 3.6 重写三个 auth starter 的 README（`auth-core-spring-boot-starter`、`auth-rbac-spring-boot-starter`、`auth-rbac-security-spring-boot-starter`）：按 D3 七段骨架，加入 5 分钟接入、必需 Bean/表、空转项，更新 SQL 链接到 `relation/`。验证：每篇都有“5 分钟接入”段并给出可复制的 Maven 坐标、最小配置和一条验证命令；前两者 SQL 链接指向 `relation/auth-rbac/auth-rbac.sql`；`auth-rbac-security` 说明三个默认实现都在使用方已提供同类 Bean 时让位（`@ConditionalOnMissingBean`）。
- [x] 3.7 迁移 `README_Auth.md` 中仍有价值且与代码一致的扩展章节（`RbacAnnotationAuthHandler`（仅菜单）、`SysAuthMenuComposite`、`AuthDataHandler` / `AuthMenuHandler`、自定义岗位维度、权限粒度）到 `auth/docs/rbac-and-extension.md` 或 `auth/docs/annotations.md`。验证：逐条对照源码，不迁移已删除的 `AnnotationAuthHandler` / `AbstractAuthDatraAnnotationAuthHandler` / `@AuthDataAuthEdit`，也不迁移过时外链、旧配置键、RDS 凭据；迁移完成后 `README_Auth.md` 进入待删除状态（6.2 删除）。

## 4. 其余模块与示例文档

- [x] 4.1 新建 `basic/README.md`：定位（库模块，不是 starter）、包结构与能力（异常、过滤器、日志追踪、通用模型、工具类）、被哪些 starter 使用、最小依赖用法、与 `basic/request-response-contract` 规格的关系。验证：对照 `basic/src/main/java` 包结构；链接到 `basic-support/README.md` 与 `boot/basic-spring-boot-starter/README.md`。
- [x] 4.2 新建 `basic-support/README.md`：定位（持久化 / 消息 / MP 工具）、`BasicSupportEndpoint`、请求日志落库、方糖通知、DataSource 硬前提、Snowflake / PageUtil 等工具；说明它由 basic starter 装配。验证：对照 `basic-support/src/main/java`；链接到 `relation/basic-support/basic-support.sql` 与 `boot/basic-spring-boot-starter/README.md`。
- [x] 4.3 重写 `boot/basic-spring-boot-starter/README.md`：按 D3 骨架；5 分钟接入用 H2 + DataSource 路径，说明 p6spy / MyBatis-Plus 传递依赖、请求日志落库需要 `sys_request_log`、方糖双开关。验证：命令/配置与 `sample/basic-boot-sample` 一致；SQL 链接指向 `relation/basic-support/basic-support.sql`。
- [x] 4.4 重写 `boot/storage-spring-boot-starter/README.md` 与 `boot/tenant-spring-boot-starter/README.md`：按 D3 骨架；storage 写清七牛四键、三张表、端点表、`cu.storage.type` 未接线；tenant 写清 `tenantId` 头、Feign 条件、无表。验证：storage 的 SQL 链接指向 `relation/basic-support/basic-support.sql`；两个 README 都有可复制的 5 分钟配置与一条验证方式。
- [x] 4.5 新建 `boot/magic-spring-boot-starter/README.md`：标注“示例 / 实验性”；说明 `SampleMagicApiApp`、`HelloController` 的固定返回、`application-magic.sample` 仅本地演示（含固定 secret-key），不作为生产 starter。验证：对照源码与 `application-magic.sample`；文档不宣称生产可用。
- [x] 4.6 新建四个示例 README：`sample/basic-boot-sample/README.md`、`sample/login-boot-sample/README.md`、`sample/storage-boot-sample/README.md`、`sample/tenant-boot-sample/README.md`；统一按“演示什么 / 零配置启动 / 打开什么 / 验证清单 / 不覆盖什么”组织。验证：每篇的端口、profile、账号、路径都与对应 `application.yml` / Controller 一致；login 示例写明 `admin/admin123` 与 `demo/demo123`，不写旧的 `admin/123456`。
- [x] 4.7 统一 `sample/auth-core-boot-sample/README.md` 与 `sample/auth-rbac-boot-sample/README.md` 的风格并更新路径：保留 auth-core 的场景矩阵与 `verify.sh` 说明、auth-rbac 的会议室案例与部门继承案例；SQL 引用改为 `relation/auth-rbac/auth-rbac.sql` + 本模块 `sql/`。验证：两篇的启动命令可在本机 H2 跑通；`bash sample/auth-core-boot-sample/verify.sh` 仍可执行；所有 SQL 链接存在。
- [x] 4.8 新建 `other/code-generator/README.md`：生成器入口、配置项、模板目录、运行方式、当前状态（含 `.vm.bak` 说明）。验证：对照 `CodeGenerator.java` / `Properties.java` 与 `templates/`；给出的命令与类名一致。
- [x] 4.9 新建 `scripts/README.md`：`check-sql-compat.sh` / `CheckCompat.java`、`check-config-docs.sh` / `CheckConfigDocs.java` 的用途、前置、环境变量（`H2_JAR`、`SNAKEYAML_JAR`、`JAVA_BIN`）、扫描范围与已知盲区（H2 不等同 MySQL；MySQL-only 的 session 脚本被排除）。验证：文档中的路径和环境变量与脚本一致；每个脚本至少能运行到参数校验或完成一次实际检查。

## 5. 登录协议文档

- [x] 5.1 重写 `boot/login-security-spring-boot-starter/README.md`：按 D3 骨架，保留准确的自动装配表；增加“三协议总览”和“未认证请求看到什么”；配置表只保留模块级摘要，细节链接到 `docs/*`；删除末尾“主要变化点”。验证：每一条 `[未接线]` / `[已废弃]` 与 `docs/application.yml` 一致；所有 docs 链接存在。
- [x] 5.2 新建 `boot/login-security-spring-boot-starter/docs/sso-overview.md`：身份源与本地账号分工、三协议 ASCII 时序、端点总表、成功 / 失败交付（JSON vs 302 + `loginState`）、会话里存什么、明确不支持的项（remember-me、OIDC logout、无代码证据的 IdP-initiated SAML）。验证：对照 security 包源码与 `sample/login-boot-sample/application.yml`；时序图使用 ASCII；`result-url` 行为的四种端点组合与现有 README 表一致。
- [x] 5.3 新建 `docs/oauth2.md`：授权码流程、token、scope、state、框架配置映射、端点、失败模式、安全注意；保留 OIDC 踩坑记录中属于 OAuth2 的部分。验证：`client-authentication-method` 写成 `client_secret_basic|client_secret_post|none`；`authorization-grant-type`、`user-info-authentication-method` 标 `[未接线]`；配置键与 `docs/application.yml` 一致。
- [x] 5.4 新建 `docs/oidc.md`：OIDC 身份层、`openid` scope、`id_token`、discovery、JWKS、`nonce`、claim 映射、启动期网络依赖、`user-name-attribute-name` 与 `sub` 回退、排错清单。验证：覆盖 OIDC 踩坑记录中的 JWKS 参数、`text/html` converter、`BadJwtException`、`invalid_nonce` 四类问题；明确 `ignore-ssl` 只影响 userinfo。
- [x] 5.5 新建 `docs/saml2.md`：断言、AuthnRequest/Response、metadata、签名、Audience、SLO、SP metadata 端点、`WantAuthnRequestsSigned` 必填、`OpenSamlAuthenticationProvider` 的 JDK 8 约束；把旧 metadata 指南改写成“本框架如何暴露 metadata”。验证：明确 SP 发起是唯一支持模式（除非代码证明）；端点路径与 `SamlProperties` 一致；`login-page-url` 标 `[已废弃]`。
- [x] 5.6 删除 `README-SSO.md`、`docs/说明文档/oidc接入踩坑记录.md`、`docs/说明文档/SAML2_SP_Metadata配置指南.md`；删除空的 `docs/说明文档/`。验证：`grep -rn "README-SSO\|oidc接入踩坑\|SAML2_SP_Metadata" --exclude-dir=.git --exclude-dir=.qoder --exclude-dir=openspec .` 返回空；`test ! -e docs/说明文档`。

## 6. 根 README 与 README_Auth 收尾

- [x] 6.1 重写根 `README.md`：定位、真实能力清单（带状态标记）、模块地图（每个模块一句 + 链接）、两条快速开始（H2 示例路径以 `sample/auth-rbac-boot-sample` 为主，登录 / SSO 指向 `sample/login-boot-sample`；自有 MySQL 项目路径）、文档导航、贡献与许可。验证：所有模块 / 示例 / 协议文档链接存在；不再包含完整注解详解、配置大全或 FAQ 长文；快速开始的端口与账号与示例实际配置一致（auth-rbac 默认身份 admin，可用 `X-Demo-User` 切换；login 示例 `admin/admin123`）。
- [x] 6.2 在 1.1 已确认凭据轮换、6.1 已移除所有引用后，删除 `README_Auth.md`。验证：`test ! -e README_Auth.md`；`grep -rn "README_Auth" --exclude-dir=.git --exclude-dir=.qoder --exclude-dir=openspec .` 返回空；`grep -rn "rds.aliyuncs.com\|open_framework" --exclude-dir=.git --exclude-dir=.qoder --exclude-dir=openspec .` 返回空（不打印任何命中值）。
- [x] 6.3 按根 README 的“两条快速开始”逐条执行：H2 路径从仓库根 `mvn clean install -DskipTests` 后启动 `sample/auth-rbac-boot-sample`；MySQL 路径按依赖、建库脚本顺序、最小配置走到启动。验证：两条路径都能按文档启动成功；发现不一致立即回写文档。

## 7. 集成验证

- [x] 7.1 全仓链接检查：提取所有 Markdown 相对链接并验证目标文件存在，覆盖根 README、模块 README、`docs/`、`relation/README.md`、示例 README。验证：检查命令返回 0 个 broken link；若一次性命令值得复用，再评估是否落成 `scripts/check-doc-links.sh`（不强制）。
- [x] 7.2 运行 `bash scripts/check-sql-compat.sh`（必要时设置 `H2_JAR`）。验证：退出码 0；输出包含 `relation/auth-rbac/auth-rbac.sql`、`relation/basic-support/basic-support.sql` 与 4 个 sample 脚本；不包含 `spring-session-mysql.sql`。
- [x] 7.3 运行 `mvn -q -DskipTests compile` 后执行 `bash scripts/check-config-docs.sh`（必要时设置 `SNAKEYAML_JAR` / `JAVA_BIN`）。验证：退出码 0，`docs/application.yml` 仍覆盖全部配置键。
- [x] 7.4 运行 `mvn -q -DskipTests package`。验证：全模块打包成功；`sample/auth-rbac-boot-sample/target/classes/sql/` 与 `sample/basic-boot-sample/target/classes/sql/` 的内容符合 2.5 的预期。
- [x] 7.5 启动 `sample/auth-core-boot-sample` 与 `sample/auth-rbac-boot-sample`（H2 默认 profile）。验证：两个应用启动无 SQL 初始化错误；按 README 各调用一个接口 / 场景，结果与文档一致；auth-core 的 `verify.sh` 通过。
- [x] 7.6 启动 `sample/basic-boot-sample`、`sample/login-boot-sample`、`sample/storage-boot-sample`、`sample/tenant-boot-sample`（H2 默认 profile）。验证：四个应用都能启动；basic 的过滤器 / Jackson / 校验示例、login 的 `/login.html` 与 `admin/admin123`、storage 的上传端点（无七牛密钥时按文档说明跳过）、tenant 的 `tenantId` 头行为与 README 一致。
- [x] 7.7 风格与旧路径终检：`grep` 检查“主要变化点”“更新日志”、emoji 标题、Unicode 框线图、`README-SSO`、`docs/sql`、`docs/说明文档`、`README_Auth` 等旧引用；检查配置表列头统一。验证：除 `docs/decisions/` 历史记录外无命中；抽查每篇文档的“这是什么”首段与“相关文档”尾段。
- [x] 7.8 最终 `git status` 与 diff 审查：确认改动只在文档、SQL 资产、sample poms、scripts、`docs/application.yml` 注释，以及 `CodeGenerator.java`、`RoutingOAuth2UserService.java` 的注释更新；没有 Java 行为代码、依赖版本、`.qoder/repowiki`、`openspec/specs` 变更；没有新增明文密钥。验证：`git status --short` 与 `git diff --stat` 符合上述范围；提交前列出所有新增 / 删除 / 重命名文件供用户复核。

## 执行备注

- 2026-10-27：项目所有者确认 `README_Auth.md` 中的 RDS 凭据已轮换；该文件已删除。
- 2026-10-27：MariaDB 11.4 本机实测 MySQL 路径：创建 `kw_test` → 执行框架/会话/示例四个脚本 → exec jar 启动 `mysql` profile；`/meetingRoom/page` 与 `/deptInherit/rooms` 结果符合预期。
- 2026-10-27：H2 路径实测通过；`sample/auth-core-boot-sample` 的 `verify.sh` 通过 70/70；`basic`、`login`、`storage`、`tenant` 示例均启动成功。
- 2026-10-27：示例启动方式定为：`sample/pom.xml` 统一声明 `spring-boot-maven-plugin`，`basic` / `login` / `storage` / `tenant` 用 `mvn spring-boot:run`；`auth-core` / `auth-rbac` 用打包后的 exec jar（`auth-rbac` 在 Maven 插件下有类路径扫描差异）。
- 2026-10-27：`CodeGenerator.java` 仅清理真实 RDS/IP 注释示例，替换为 localhost 示例；`RoutingOAuth2UserService.java` 更新决策记录路径注释；均无行为变化。
