# Design

## Context

动机见 `proposal.md - Why`。实现前需要知道的现状与约束：

- `auth-rbac-security-spring-boot-starter` 的 `RbacSecurityConfig` 用三个 `@ConditionalOnMissingBean` 提供默认实现：`SecurityUserDetailsService`、`AuthCurrentUserContextHolder`、`RbacCurrentUserEndpoint`。使用方一旦声明同类型 Bean，默认实现就让位——这正是当前脚手架三处复制件造成的问题。
- 登录链要求的 Bean 都已存在：`SecurityConfig` 强制注入 `SecurityUserDetailsService`，`SecurityBeanConfig` 在无自定义编码器时注册 `DelegatingPasswordEncoder`；RBAC 链的 `JdbcAuthDataHandler`、`MybatisDataRangeInterceptor`、`MenuAccessInterceptor` 由 starter 自动装配。
- 菜单注册器在启动后异步补缺失行，且把非根行一律标成「基于父权限」；种子必须显式提供需要「基于自身权限」的行，否则会继承父菜单的授权。
- `sample/demo-database-profiles` 约束：H2 只出现在 profile 段、MySQL profile 不执行脚本、框架表结构单一来源、schema 与 data 分文件、会话表由会话存储创建、示例 SQL 进入 `scripts/check-sql-compat.sh`。
- 该检查器把 `relation/*` 与 `sample/*/sql/*.sql` 全部灌进同一个 H2 库并跑两遍。任何与 `auth-rbac-boot-sample` 重叠的固定主键插入都会在第二份种子处失败。
- 种子里的 bcrypt 串已实测匹配 `123456`（`BCryptPasswordEncoder.matches`）。
- H2 console 默认会被安全链拦下（`anyRequest().authenticated()` 且 frame options 为 DENY），本示例不开启。

## Goals / Non-Goals

**Goals:**

- 让登录人成为 RBAC 判权的唯一身份来源，且整条链路只使用 starter 的默认实现。
- 默认 profile 零外部依赖启动，页面与脚本能对四个演示账号复现同一张判权矩阵。
- 示例 SQL 与其它示例脚本在同一数据库里可以顺序执行、重复执行。

**Non-Goals:**

- 不搬会议室业务（保留在 `auth-rbac-boot-sample`）。
- 不实现 JUnit 端到端测试；本轮以页面与 `verify.sh` 作为验证证据，框架单测不重复。
- 不修改任何框架模块代码或行为；若实测发现 starter 缺陷，只记录，不在本变更修。
- 不把 SSO 联调、MySQL 实际部署、H2 控制台纳入默认验证。

## Decisions

### 1. 删除三处覆盖件，而不是改写它们

删除 `SampleInMemorySecurityUserDetailsService`、`AuthBootSampleCurrentUserContextHolder`、`SampleCurrentUserController`，并从示例配置里移除 `NoOpPasswordEncoder` 与对应的 holder Bean。

- 备选：保留 `X-Demo-User` 上下文，在登录后手工把请求头设成登录名。否决——判权身份会重新变成请求可写的，和本示例要证明的契约相反。
- 备选：保留内存用户，把它们改成 `ROLE__1`。否决——RBAC 表在登录链上仍然不参与，`/currentUser` 的 id/部门/权限全部是假的。

### 2. 密码编码与种子保持 `{bcrypt}`

使用 starter 默认的 `DelegatingPasswordEncoder`；种子里的四个演示账号密码沿用 `{bcrypt}` 前缀，明文统一 `123456`。登录页提示与 README 写明。

- 备选：种子写 `{noop}`。否决——会把「生产用强编码」的既有文档口径带偏，且 NoOp 需要自定义编码器，回到覆盖默认实现的老路。

### 3. 当前用户统一走 starter 的 `/currentUser`

页面以 `cu.security.current-user-url` 指向的端点为登录态唯一来源；删除自建 `/api/me`。页面把 `authorities` 从对象数组映射成权限串展示。

- 备选：保留 `/api/me` 继续给页面用。否决——starter 的第三个默认 Bean 将没有任何消费方，示例证明不了它。
- 影响：`login.js` / `index.js` 里所有 `/api/me` 改为 `/currentUser`，登录态判据仍只看响应体的 `success`。

### 4. 演示面只保留判权四类声明，不搬会议室

保留并适配 `auth-rbac-boot-sample` 的 `/test/*` 与 `/deptInherit/rooms`：菜单正反例、数据范围、行级数据操作、部门继承各至少一个端点。业务表改名 `security_sample_business`，不再使用 rbac 示例的 `testauth_annotation_business`，避免两个示例的 schema 互相 DROP。

- 备选：整份搬会议室控制器、实体与两张业务表。否决——范围翻倍、与另一个示例的种子漂移，demo 的核心不是业务 CRUD。
- 备选：保留原业务表名。否决——检查器把两个示例的 schema 按目录顺序执行，后一个 schema 的 `DROP TABLE` 会清掉前一个示例的业务数据，改名后互不干扰。
- 适配点：原 `TestAuthAnnotationController` 在 `size()==1` / 非空上做了断言，非 admin 身份会 500；迁移时去掉断言，让「允许但 0 行」成为正常结果。数据范围端点统一返回 `Result` 包装。

### 5. 依赖保持最小，不引 `basic-spring-boot-starter`

只补 `spring-boot-starter-validation`。RBAC 链需要的 MyBatis-Plus、`basic-support`、`basic` 已由 starter 传递；示例自带 `MybatisPlusInterceptor`（H2 方言）。

- 备选：按 starter README 的「5 分钟接入」加 `basic-spring-boot-starter`。否决——它带来请求日志过滤器、自己的分页插件、另一套异常 advice 和 p6spy，都会与登录过滤器链或示例自带分页插件发生交互；示例应展示最小可运行面，README 再把 basic starter 标为生产可选。
- MySQL profile 沿用 login 示例的姿态：驱动自行补，脚本手工按序执行。

### 6. SQL 放在模块 `sql/` 目录，子 POM 重写 resources

`sql/schema.sql` 与 `sql/seed-data.sql` 放在模块根 `sql/`，子 POM 用 `<resources>` 同时挂载 `src/main/resources`、`../../relation/auth-rbac`（targetPath `sql`）与自身 `sql/`。

- 备选：放在 `src/main/resources/sql/`。否决——`scripts/check-sql-compat.sh` 只扫 `sample/*/sql/*.sql`，放源码目录会逃出检查范围，违反 `sample/demo-database-profiles` 的要求。
- 备选：把 rbac 示例的种子目录挂进来复用。否决——两个示例从此共享数据，改一边影响另一边，且违背「示例专属种子」的文件归属。

### 7. 种子写成幂等形态

所有 `sys_*` 行用 `INSERT ... SELECT ... WHERE NOT EXISTS` 按自然键守卫：部门与角色用固定 ID，用户按 `username` 解析、关联行用子查询取 `id`，授权行按 `(api_id, authority)` 去重，业务行按主键去重。schema 对示例业务表使用 `DROP TABLE IF EXISTS` + `CREATE TABLE`。

- 备选：改检查器，让每个示例使用独立数据库。否决——改动共享工具、影响其它示例的既有验证结论，收益不抵风险。
- 备选：给本示例换一套完全独立的账号名、路径与 ID。否决——与 rbac 示例的路径/账号不再可比，README 和排障都更绕；幂等守卫已经能解决共存。

### 8. 菜单种子显式钉住权限类型

种子提供 `/test`、`/test/checkMenu`（`auth_type=2`，继承 `/test` 的 `ROLE__1`）、`/test/checkMenuNone`（`auth_type=1`，无授权）、`/deptInherit`、`/deptInherit/rooms`（`auth_type=1`，挂 `RELATIONDEPT__2`）。ID 沿用 rbac 示例的固定值，便于两库共存。

- 原因：菜单注册器把非根行默认标成 `auth_type=2`。`/deptInherit/rooms` 若交给它生成，会继承父菜单的 `ROLE__1`，部门继承演示直接失效。

### 9. SSO 默认关闭，独立 profile 开启

默认 yml 不含任何 `clients` / `registrations`；新增 `application-sso.yml`，包含 OAuth2 手动端点、OIDC 与 SAML 三条本地 IDP 配置，`client-secret` 使用占位值并在注释里指向 login 示例的同名客户端。启动命令 `--spring.profiles.active=h2,sso`。

- 备选：沿用 login 示例的默认 SSO 配置。否决——OIDC 会在启动期访问 `http://localhost:9010/.well-known/openid-configuration`，IDP 不在线时应用起不来，违背「默认零外部依赖」。
- 启用后身份映射不需要新代码：`SsoSecurityUserConverter` / `SecurityUserSamlAuthenticationConverter` 都调用同一个 `SecurityUserDetailsService`，也就是 RBAC 装载器。

### 10. 验证台沿用 auth-core 示例的交互形态

页面 = 身份切换 + 真值表 + 一键测试 + 汇总；期望值内置在页面（按用户名分组），实际值来自真实请求。身份切换 = `POST /logout` 后 `POST /login`，不引入测试后门。矩阵只读（`dataEdit` 端点本身无副作用）。

- 备选：不做页面，只留 curl。否决——人的直觉验证成本高，且「期望/实际/结果」正是上一轮 auth-core 示例被认可的形态。
- 备选：加 MockMvc 集成测试。暂缓——Spring Session 的 Cookie 在 MockMvc 里需要手工搬运，`verify.sh` 已覆盖同一批断言；后续要进 CI 再补。

### 11. 判权矩阵

| # | 调用 | admin | admin2 | subordinate | outsider | 判定的层 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | `GET /currentUser` | 200 | 200 | 200 | 200 | 登录态 |
| 2 | `GET /test/checkMenu` | 200 | 403 | 403 | 403 | 菜单（父授权继承） |
| 3 | `GET /test/checkMenuNone` | 403 | 403 | 403 | 403 | 菜单（无授权） |
| 4 | `GET /test/dataRange` | 200 / total=1 | 200 / total=0 | 200 / total=0 | 200 / total=0 | 数据范围 |
| 5 | `POST /test/dataEdit {"id":1}` | 200 | 403 | 403 | 403 | 行级数据操作 |
| 6 | `GET /deptInherit/rooms` | 403 | 403 | 200 | 403 | 部门继承 |

账号与归属：`admin` 根部门(1)+角色 1；`admin2` 总部(2)与外部合作部(4)、无角色；`subordinate` 研发部(3)、无角色；`outsider` 外部合作部(4)、无角色。部门层级 1 -> 2 -> 3，4 是独立根。

## Risks / Trade-offs

- **`mvn spring-boot:run` 类路径扫描问题**（rbac 示例已记录，表现为 `权限标记错误: ROLE`）→ README 明确用 `package` + `java -jar` 验证；若实测 run 正常也保留该建议。
- **幂等种子写错守卫** → 必须先跑 `scripts/check-sql-compat.sh`（两遍）再启动应用；检查器通过前不算完成。
- **真值表三处漂移**（页面 / `verify.sh` / README）→ 规格要求同步；tasks 里把三者放在同一任务核对。
- **无凭证用户的 `enabled` 拆箱**：RBAC 装载器对无凭证用户不设置 `enabled`，`SecurityUser.isEnabled()` 会 NPE。种子里的演示账号都有凭证；README 不把无凭证用户列为可登录账号。该缺陷记录在案，不在本变更修。
- **`/currentUser` 的 `loginTime` 为空**（装载器不写该字段）→ 页面不展示登录时间，避免把空值当成异常。
- **SSO profile 需要本地 IDP** → 不进入 `verify.sh`；文档标为手工联调，并指明使用 login 示例的 IDP 配置。
- **会话表依赖 DataSource** → h2 与 mysql 两个 profile 都提供数据源；`initialize-schema` 分别 embedded / never。
- **`dataEdit` 只返回成功而不改数据** → 矩阵把它当作「行级操作权限判定」的证据，不声称覆盖业务副作用；副作用类断言留给 rbac 示例与框架测试。

## Migration Plan

新增示例，无数据迁移。回滚方式为整体回退本变更；不触碰既有数据库与其它示例。默认端口 8083，首次启动使用内存 H2，重启即回到种子状态。

## Open Questions

无。SSO 客户端密钥是否复用 login 示例的本地 IDP 值，属于可选配置的填充细节，实现时用占位值并在注释中指向 login 示例。
