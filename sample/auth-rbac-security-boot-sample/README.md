# auth-rbac-security-boot-sample

## 这是什么

`auth-rbac-security-spring-boot-starter` 的融合示例：**登录身份来自 RBAC 用户表，权限在登录时装载进会话**，菜单、数据范围、行级数据操作与部门继承四类判定全部按登录人计算。starter 的 README 里「当前没有单独使用本 starter 的示例」指的就是这个缺口。

依赖链由 starter 传递：

```
auth-rbac-security-spring-boot-starter
  +-- auth-rbac-spring-boot-starter       (RBAC 表、菜单/数据权限实现)
  +-- login-security-spring-boot-starter  (JSON 账密登录、会话、SSO)
```

本示例刻意**不声明** `SecurityUserDetailsService`、`AuthCurrentUserContextHolder` 或 `PasswordEncoder`：这三个 Bean 一旦由示例提供，starter 的默认实现就会按 `@ConditionalOnMissingBean` 让位，登录身份便不再参与判权。

## 零配置启动

```bash
cd sample/auth-rbac-security-boot-sample
mvn -q -DskipTests package
java -jar target/auth-rbac-security-boot-sample-0.2.1-exec.jar
```

打开 <http://localhost:8083/login.html>。

- 默认 `h2` profile：内存库，启动时自动执行框架建表脚本、示例业务表与种子；不需要 MySQL，也不需要本地 IDP。
- 端口 **8083**（8080/8081/8082 已被其它示例占用）。
- 请用打包后的 exec jar 启动。与 `auth-rbac-boot-sample` 一样，`mvn spring-boot:run` 在当前工程可能遇到权限实体的类路径扫描问题；`java -jar` 是验证过的方式。
- H2 console 默认没有开启：安全链会拦下 `/h2-console`，开启还需要额外放行并关闭 frame options，示例不把这段配置塞进来。

## 演示账号

密码统一 `123456`，存的是 `{bcrypt}` 编码。

| 账号 | 部门 | 角色 | 用途 |
| --- | --- | --- | --- |
| `admin` | 根部门(1) | 超级管理员(角色 1，权限串 `ROLE__1`) | 菜单、数据操作全部放行；数据范围看到 1 行 |
| `admin2` | 总部(2) + 外部合作部(4) | 无 | 菜单与数据操作全部拒绝；数据范围允许但 0 行 |
| `subordinate` | 研发部(3，总部的子部门) | 无 | 只有部门继承端点放行 |
| `outsider` | 外部合作部(4，独立根) | 无 | 没有任何放行 |

部门层级：`1 根部门 -> 2 总部 -> 3 研发部`，`4 外部合作部` 是另一棵根。

## 判权矩阵

期望值来自 `sql/seed-data.sql`；`verify.sh` 与验证台页面内置同一张表。

| # | 调用 | admin | admin2 | subordinate | outsider | 判定的层 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | `GET /currentUser` | 200 | 200 | 200 | 200 | 登录态 |
| 2 | `GET /test/checkMenu` | 200 | 403 | 403 | 403 | 菜单（继承父授权） |
| 3 | `GET /test/checkMenuNone` | 403 | 403 | 403 | 403 | 菜单（无授权） |
| 4 | `GET /test/dataRange` | 200 / 1 行 | 200 / 0 行 | 200 / 0 行 | 200 / 0 行 | 数据范围 |
| 5 | `POST /test/dataEdit` | 200 | 403 | 403 | 403 | 行级数据操作 |
| 6 | `GET /deptInherit/rooms` | 403 | 403 | 200 | 403 | 部门继承 |

两点容易误读：

- 权限拒绝以 **HTTP 403 + 响应体 `code=403` + `errorType=AUTHORIZATION`** 交付；页面与脚本只看响应体，因此 HTTP 状态变化不影响验证台。
- 未登录访问 `/currentUser` 为 **HTTP 401 + `errorType=AUTHENTICATION`**，账号密码错误同样是 401，提示为“用户名或密码错误”。
- 第 4 行的 `200 / 0 行` 是「允许访问但看不到数据」，与第 2/3/5/6 行的拒绝不是一回事。

## 权限从哪来

| 数据 | 作用 |
| --- | --- |
| `sys_user` / `sys_user_credential` | 登录账号与 `{bcrypt}` 密码，`enabled=1` |
| `sys_user_role` / `sys_role` | `admin` -> 角色 1 -> 权限串 `ROLE__1` |
| `sys_user_dept` / `sys_dept` | 用户部门与上级链；`subordinate` 的上级链含总部，所以带出 `RELATIONDEPT__2` |
| `sys_auth_data` | `testrange` 槽位把业务 ID 1 授权给 `ROLE__1`；`testedit` 同理 |
| `sys_menu_api` / `sys_auth_menu` | 请求路径与授权行；`/test/checkMenu` 的 `auth_type=2` 继承父菜单 `/test` 的 `ROLE__1`；`/deptInherit/rooms` 显式 `auth_type=1` 并挂 `RELATIONDEPT__2` |

登录成功后，权限是**会话快照**：改库后需要重新登录才生效。

## 接口与页面

| 地址 | 用途 |
| --- | --- |
| `/login.html` | 登录页：账密表单 + 按配置动态渲染的单点登录入口 |
| `/` 或 `/index.html` | 验证台：当前身份、期望/实际/结果、一键测试、身份切换 |
| `GET /api/login/options` | 未认证可访问的登录方式清单 |
| `POST /login` | JSON 账密登录（`Content-Type` 必须是精确的 `application/json`） |
| `GET /currentUser` | starter 的当前用户端点，页面判定登录态的唯一来源 |
| `GET /test/dataRange` | 数据范围列表（`testrange` 槽位） |
| `GET /test/pageDataRange` | 数据范围分页版本（列表与计数各一条声明） |
| `POST /test/dataEdit` | 行级数据操作（`testedit` 槽位）；方法体不写数据 |
| `GET /test/checkMenu` | 菜单正例：继承父菜单授权 |
| `GET /test/checkMenuNone` | 菜单反例：没有任何授权行 |
| `GET /deptInherit/rooms` | 部门继承：只有总部的严格子孙放行 |
| `POST /logout` | 退出登录 |

验证台只发起只读请求和一条无副作用的数据操作调用；身份切换走真实的 `POST /logout` 与 `POST /login`。

## 验证

```bash
# 应用启动后
bash verify.sh
# 换地址：BASE=http://host:port bash verify.sh
```

脚本对四个账号逐条断言上表的 6 行用例，另加「admin 权限集合含 `ROLE__1` / 其它账号不含」的对照，共 32 条断言；任一失败以非零状态退出。

页面侧打开 <http://localhost:8083/>，切换身份即可看到同一张表的期望、实际与结论；「测试当前身份」会重跑当前账号的全部用例。

也可以直接看接口：

```bash
curl -s -X POST http://localhost:8083/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456"}' -c target/admin.cookie
curl -s -b target/admin.cookie http://localhost:8083/currentUser
curl -s -b target/admin.cookie 'http://localhost:8083/test/pageDataRange?page=1&size=10'
```

`Content-Type` 必须是精确的 `application/json`，带 charset 参数会被当成表单解析。

## 可选：单点登录

默认配置**不包含任何 SSO 客户端**，所以没有本地 IDP 也能启动；登录页显示「当前没有配置单点登录身份源」。

需要联调时使用独立配置档：

```bash
java -jar target/auth-rbac-security-boot-sample-0.2.1-exec.jar --spring.profiles.active=h2,sso
```

- 配置在 [application-sso.yml](src/main/resources/application-sso.yml)，需要本地 IDP（默认 `http://localhost:9010`）在线。
- OIDC 条目会在启动期拉取 `{issuer-uri}/.well-known/openid-configuration`，IDP 不在线时整个应用起不来；只联调 OAuth2 / SAML 时把 `demo-oidc` 注释掉。
- `client-id` / `client-secret` 是占位值（**不要把真实凭据提交进仓库**）。填入与 [login-boot-sample](../login-boot-sample/README.md) 相同的本地 IDP 客户端，或在 IDP 后台登记本示例的回调地址。
- 本示例默认端口 8083，而 login 示例登记的回调是 8080；直接复用那套客户端时，用 `--server.port=8080` 启动，或把 `http://localhost:8083/login/oauth2/code/{registrationId}` 登记到 IDP。
- 三类协议认证成功后都由同一个 `SecurityUserDetailsService` 按用户名落到本地 RBAC 账号：**SSO 只证明是谁，权限仍来自本系统**。本地已实测 OAuth2 完整往返（IDP 登录 -> 授权 -> 回调 -> `/currentUser` 为本地 `admin` 且权限为 RBAC 装载结果）。
- `verify.sh` 不覆盖 SSO；它需要外部 IDP 参与。

## 数据与 profile

### h2（默认）

启动时自动执行：

1. `classpath:sql/auth-rbac.sql`（框架 10 表 + 视图，来自 `relation/auth-rbac/`）
2. `classpath:sql/schema.sql`（示例业务表 `security_sample_business`）
3. `classpath:sql/seed-data.sql`（幂等种子）

会话表由 `spring.session.jdbc.initialize-schema=embedded` 创建。内存库重启即回到种子状态。

### mysql

`mysql` profile **不执行任何脚本**。首次使用按顺序手工执行：

1. [relation/auth-rbac/auth-rbac.sql](../../relation/auth-rbac/auth-rbac.sql)
2. [sql/schema.sql](sql/schema.sql)
3. [sql/seed-data.sql](sql/seed-data.sql)
4. 会话表：[relation/login-security/spring-session-mysql.sql](../../relation/login-security/spring-session-mysql.sql)

并把 `spring.session.jdbc.initialize-schema` 保持为 `never`。本示例的 pom 不含 MySQL 驱动，需要在本地自行补 `mysql:mysql-connector-java`。

### 种子为什么是幂等的

`scripts/check-sql-compat.sh` 会把 `relation/*` 与 `sample/*/sql/*.sql` 全部灌进同一个 H2 库并跑两遍。新示例的种子全部用 `INSERT ... SELECT ... WHERE NOT EXISTS` 守卫，按自然键跳过已存在的行；与 `auth-rbac-boot-sample` 的种子在同一库里顺序执行也不会撞主键或唯一键。

## 覆盖边界

| 能力 | 在哪 |
| --- | --- |
| 会议室业务（编辑 / 预约 / 主权限管理） | [auth-rbac-boot-sample](../auth-rbac-boot-sample/README.md) |
| 单点登录的完整联调 | 需要本地 IDP；本示例只提供可选配置与已验证的 OAuth2 往返 |
| H2 console | 不开启；需要时自行放行 `/h2-console/**` 并处理 frame options |
| 副作用证据 / 启动期声明校验 | 框架单测与 auth-core 示例 |
| 无凭证账号登录 | 不作为演示账号：`RbacSecurityUserDetailsService` 对无凭证用户不设置 `enabled`，`SecurityUser.isEnabled()` 拆箱会 NPE。已记录为框架待修项，不在本示例修 |
| `/currentUser` 的 `loginTime` | 装载器不写该字段，值为 null；页面不展示 |

## 相关文档

- [auth-rbac-security-spring-boot-starter](../../auth/auth-rbac-security-spring-boot-starter/README.md)
- [auth 总览](../../auth/README.md)
- [权限注解与权限编辑](../../auth/docs/annotations.md)
- [RBAC 模型与扩展](../../auth/docs/rbac-and-extension.md)
- [login-boot-sample](../login-boot-sample/README.md)
- [auth-rbac-boot-sample](../auth-rbac-boot-sample/README.md)
- [项目根 README](../../README.md)
