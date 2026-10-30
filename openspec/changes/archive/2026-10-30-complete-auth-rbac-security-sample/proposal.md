# Proposal: complete-auth-rbac-security-sample

## Why

`auth-rbac-security-spring-boot-starter` 的三个能力（RBAC 用户装载、登录态权限上下文、`/currentUser`）目前没有任何组合验证载体：`auth-rbac-boot-sample` 用请求头声明身份、不接 Spring Security；`login-boot-sample` 用内存用户、不接 RBAC。starter 自己的 README 也写着「当前没有单独使用本 starter 的示例」。结果是这条最关键的通路——**登录身份 → RBAC 用户与权限装载 → 会话中的认证主体 → 三个权限注解判权**——只在各 module 的单测里被拆开验证过，没有任何一次端到端跑通。

`sample/auth-rbac-security-boot-sample` 已有一个从 login 示例搬来的脚手架，但它现在的接线会让这套融合**静默失效**：内存用户服务、`X-Demo-User` 上下文、`NoOpPasswordEncoder` 三处复制件分别通过 `@ConditionalOnMissingBean` 顶掉了 starter 的 `RbacSecurityUserDetailsService`、`RbacSecurityAuthCurrentUserContextHolder` 和默认的 `DelegatingPasswordEncoder`。表现是能编译、能启动、登录也能成功，但登录人根本不参与判权，RBAC 种子里的 `{bcrypt}` 密码也匹配不上。这个示例必须先纠正接线，才谈得上「做完」。

## What Changes

- **删除三处覆盖 starter 的复制件**：进程内用户服务、`X-Demo-User` 身份上下文、`NoOpPasswordEncoder`。登录用户一律由 `RbacSecurityUserDetailsService` 从 `sys_user` / `sys_user_credential` / 角色 / 部门装载，权限为登录时刻的 RBAC 快照。
- **补齐数据与配置**：h2 profile 下自动执行框架表结构、示例业务表与幂等种子；新增 `spring.sql.init`、会话表初始化、端口 8083 与 `cu.auth` / `cu.security` 配置。框架表结构只使用 `relation/auth-rbac/auth-rbac.sql` 这一份，示例自带 SQL 只含业务表与种子。
- **保留并适配 RBAC 判权演示面**：`@AuthMenu`、`@AuthDataRange`、`@AuthDataOperation`、`RELATIONDEPT` 部门继承四类声明各有一到两个端点，全部通过登录态判权，不引入请求头身份。
- **前端改成验证台**：登录页保留账密与动态 SSO 入口，当前用户判定改用 starter 的 `/currentUser`；首页改为只读验证台，支持演示账号切换、逐行展示「期望 / 实际 / 结果」并一键重跑当前身份的全部用例。
- **新增 `verify.sh`**：与页面同一张身份 × 端点真值表，用 curl 逐条断言，供无浏览器环境复现 README 结论。
- **SSO 默认关闭、按需开启**：默认配置不含任何 SSO 客户端，未装本地 IDP 也能零依赖启动；单独提供 `application-sso.yml`，启用后 OAuth2 / OIDC / SAML2 身份仍由同一个 `SecurityUserDetailsService` 落到本地 RBAC 账号。
- **文档收口**：新增示例 README；更新 starter README 的「暂无示例」、`auth/README.md` 的接入路径 A，以及根 README 中英文的示例清单。

**不做**：不搬会议室业务（保留在 `auth-rbac-boot-sample`，融合示例聚焦判权链路）；不启用 H2 console（安全链需要额外放行与 frame options 调整）；不改任何框架模块的代码与行为；不新增 JUnit 集成测试（本轮以页面 + `verify.sh` 作为端到端证据）。

## Capabilities

### New Capabilities

- `sample/rbac-security-verification-demo`: 约束融合示例的验证义务——登录身份是唯一的权限来源、当前用户必须来自 starter 端点、身份 × 端点矩阵在页面与脚本中同源可执行、默认启动零外部依赖、复用框架表结构并以幂等种子进入全仓 SQL 兼容检查。

### Modified Capabilities

无。本变更只新增示例，不修改任何既有规格的行为要求；示例对 `sample/demo-database-profiles` 与 `identity/sample-login-page` 的遵守属于既有约束的适用范围，不构成需求变更。

## Impact

**代码足迹**

- 删除 `sample/auth-rbac-security-boot-sample` 内 3 个复制件：`SampleInMemorySecurityUserDetailsService`、`AuthBootSampleCurrentUserContextHolder`、`SampleCurrentUserController`。
- 重写该示例的 `application.yml`，新增 `application-sso.yml`、`sql/schema.sql`、`sql/seed-data.sql`、验证台页面脚本、`verify.sh`、`README.md`；端口从 8080 改为 8083，避免与 login / basic / storage / tenant 示例冲突。
- 新增 RBAC 判权演示端点的实现（菜单正反例、数据范围、行级数据操作、部门继承），不新增会议室模块。
- 不修改 `auth/`、`boot/` 下任何模块；不改 `relation/` 下任何脚本。

**依赖**

- 示例模块新增 `spring-boot-starter-validation`（分页参数校验）。不引入 `basic-spring-boot-starter`，避免其请求日志过滤器、分页插件与登录过滤器链、示例自带分页插件相互作用。
- `mysql` profile 仍沿用「自行补 MySQL 驱动」的既有姿态，文档写明。

**数据与检查**

- 示例新增的 `sql/*.sql` 自动进入 `scripts/check-sql-compat.sh` 的 `sample/*/sql/*.sql` 范围。由于该脚本把所有示例脚本灌进同一个 H2 库并跑两遍，新种子必须用 `INSERT ... SELECT ... WHERE NOT EXISTS` 写成幂等形态，避免与 `auth-rbac-boot-sample` 的 `sys_*` 种子主键冲突。

**行为可见性**

- 默认账号为 `admin` / `admin2` / `subordinate` / `outsider`，密码统一 `123456`；这些账号的判权结果只由种子里的角色、部门与授权行决定。页面与 `verify.sh` 的真值表必须与种子保持同步，改种子即改真值表。
