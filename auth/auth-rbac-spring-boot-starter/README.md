# auth-rbac-spring-boot-starter

单独引入本 starter 时，必须额外实现 `AuthCurrentUserContextHolder`，否则 Auth 模块拿不到当前用户；同时必须提供 `PasswordEncoder`，否则启动报错（改密也需要它）。引入 `auth-rbac-security-spring-boot-starter` 时，这两个默认实现由它提供。

## 自动装配

| 配置类 | 生效条件 | 产出 |
| --- | --- | --- |
| `AuthRbacScanConfig` | 无 | `@MapperScan("com.cu.framework.auth.rbac.**.mapper")` + 扫描 `mp.service.impl`、`composite`、`controller` |
| `AuthRbacConfig` | `@AutoConfigureAfter(AuthCoreConfig, AuthRbacScanConfig)` | `RbacAnnotationAuthHandler`（使用方已有 `AuthMenuHandler` 时让位）、`AuthDataAdaptor`、`RbacCurrentUserService`、`RbacMenuApiService`、`AuthMenuStore`（未定义时用 `MemoryExpiredAuthMenuStore`） |

## 配置项

| key | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- |
| `cu.auth.cache-auth` | `false` | 否 | 是否缓存菜单与权限数据；false 时每次直接查库，由 `DefaultSysAuthMenuComposite` 读取 |

## 启动前置条件与已知空转项

- 需要 MyBatis-Plus 与 DataSource；建表脚本见 [docs/sql/auth-rbac.sql](../../docs/sql/auth-rbac.sql)（共 10 张表，含 `sys_auth_data`、`sys_auth_menu`、`sys_menu_api`、`sys_menu_route`、`sys_user*`、`sys_role`、`sys_dept`）。
- 需要 `PasswordEncoder` 与 `AuthCurrentUserContextHolder`；单独使用本 starter 时由使用方提供，或改用 `auth-rbac-security-spring-boot-starter`。
- `cu.auth.cache-auth=true` 时菜单与权限写入 `AuthMenuStore`，默认实现是内存版 `MemoryExpiredAuthMenuStore`；替换为 Redis 等实现时自行声明 `AuthMenuStore` Bean。
- 无其他“空转”键。

## 完整示例

见 [docs/application.yml](../../docs/application.yml) 的 `auth-rbac-spring-boot-starter` 分节。
