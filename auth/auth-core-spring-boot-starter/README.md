# auth-core-spring-boot-starter

子类必须实现 `AuthCurrentUserContextHolder`，这跟上下文有关，而此模块还没有上下文信息。

## 自动装配

| 配置类 | 生效条件 | 产出 |
| --- | --- | --- |
| `AuthCoreConfig` | 无 | 数据权限：`AuthRangeCache`、`JdbcAuthDataHandler`（使用方已有 `AuthDataHandler` 时让位）、`AuthDataTable`、`SqlAuthInject`、`MybatisDataRangeInterceptor`、`AuthRangeStartupValidator`、`JdbcAuthDataPersistent`、`DataCheckAspect`；菜单：`MenuAccessInterceptor`、`MenuApiGeneratorProcessor`、`InitMenuApiCommandLineRunner`；拒绝通道：`AuthExceptionHandler`（仅 Web 且使用方未定义时） |
| `AuthWebConfig` | 无 | 把 `MenuAccessInterceptor` 注册到 Spring MVC |

## 配置项

| key | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- |
| `cu.auth.auth-data-table.table-name` | `sys_auth_data` | 否 | 权限数据表表名 |
| `cu.auth.auth-data-table.id-column` | `id` | 否 | 主键列名 |
| `cu.auth.auth-data-table.business-function-column` | `business_function` | 否 | 业务功能列名 |
| `cu.auth.auth-data-table.data-id-column` | `data_id` | 否 | 业务 ID 列名 |
| `cu.auth.auth-data-table.operate-column` | `operate` | 否 | 操作类型列名 |
| `cu.auth.auth-data-table.authority-column` | `authority` | 否 | 权限字符串列名 |
| `cu.auth.auth-data-table.description-column` | `description` | 否 | 权限描述列名 |

## 启动前置条件与已知空转项

- 需要 `JdbcTemplate`（DataSource）；`JdbcAuthDataPersistent` 直接依赖它。
- 数据范围能力需要 MyBatis：`MybatisDataRangeInterceptor` 挂在 `SqlSessionFactory` 上；`AuthRangeStartupValidator` 用 `ObjectProvider` 取不到 `SqlSessionFactory` 时会跳过校验，而不是启动失败。
- 必须提供 `AuthCurrentUserContextHolder` 实现，否则数据校验切面与数据范围拦截器无法取当前用户。
- 表名与七个列名必须与实际表一致；默认值对应 [docs/sql/auth-rbac.sql](../../docs/sql/auth-rbac.sql) 的 `sys_auth_data`。使用自有表时逐项覆盖。
- `AuthExceptionHandler` 只在 Web 应用且使用方未定义同类处理器时注册；非 Web 场景没有该拒绝通道。
- 无自有“空转”键。

## 完整示例

见 [docs/application.yml](../../docs/application.yml) 的 `auth-core-spring-boot-starter` 分节。
