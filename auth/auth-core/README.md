# auth-core

`auth-core` 是权限框架的接缝层：注解、权限模型接口、数据范围 SQL 注入、数据操作切面与菜单拦截器都在这里。它不包含用户、角色、部门模型，也不负责自动装配；实际接入用 [auth-core-spring-boot-starter](../auth-core-spring-boot-starter/README.md)，默认 RBAC 实现见 [auth-rbac](../auth-rbac/README.md)。

## 包结构

| 包 | 内容 |
| --- | --- |
| `com.cu.framework.auth.core` | 四个注解：`@AuthMenu`、`@AuthMenuDoc`、`@AuthDataRange`、`@AuthDataOperation`；`AuthDataAdaptor` |
| `data` | 权限表映射 `AuthDataTable`、数据权限处理器 `AuthDataHandler`、JDBC 默认实现、持久化 |
| `data.edit` | `DataIdCarrier`、`@AuthDataOperation` 的切面 `DataCheckAspect` |
| `data.range` | 数据范围声明缓存、启动校验、MyBatis 拦截器、SQL 注入器、`MatchMethod` |
| `entity` | 权限模型接口与基础实现：`IAuthObject`、`AbstractAuthObject`、`IAuthEntity`、`IFlagAuthEntity`、`FlatteningAuthority`、`AbstractIdNameFlagAuthEntity`、`AuthConstant`、`CurrentUser` |
| `menu` | `AuthMenuHandler`、菜单拦截器、菜单 API 生成器与持久化接口 |
| `exception` | `AuthException`（权限拒绝）、`AuthServiceException`（用法错误）、`AuthEntityException`（权限串解析错误） |

## 核心模型

- `IAuthObject`：权限集合体接口，`listBaseAuth()` 交出扁平权限串，`setProperties(...)` 把权限串写回结构体。
- `AbstractAuthObject`：启动时扫描 `IFlagAuthEntity` 实现并建立“标记 -> 实体类”注册表，负责权限串的反解析。
- `FlatteningAuthority`：一条扁平权限（`auth` + `description`），`equals` / `hashCode` 只基于 `auth`。
- `IFlagAuthEntity` / `AbstractIdNameFlagAuthEntity`：带标记的权限实体，负责按 `__` / `_` 编解码；子类必须保留无参构造。
- `AuthConstant`：`AUTH_SPLIT="__"`、`AUTH_SUB_SPLIT="_"`。
- `CurrentUser`：当前用户名的最小模型，只含 `username`。

`SimpleAuthObject`、`UserAuthInfo`、`User`、`Role`、`Dept`、`ParentDept`、`DeptRole` 属于默认 RBAC 实现，不在 core 里；它们的语义见 [auth-rbac](../auth-rbac/README.md) 与 [RBAC 模型与扩展](../docs/rbac-and-extension.md)。

## 空权限的对外结果

- `AuthCurrentUserContextHolder.getAuths()` 在上下文无用户或无权限对象时返回空集合，不抛异常。
- `JdbcAuthDataHandler.hasDataOperateAuths` 在权限集合为空时返回 `false`，不会因为空集合放行。
- `@AuthDataRange` 在权限集合为空时注入恒假条件 `1 = 0`，查询返回零行，而不是省略过滤条件。

默认 RBAC 的用户权限体装载细节（用户缺失、无凭证、`RELATIONDEPT` 展开）见 [RBAC 模型与扩展](../docs/rbac-and-extension.md)。

## 最小接入

`auth-core` 是库模块；使用方通常引入 `auth-core-spring-boot-starter`。starter 会装配：

- `AuthCurrentUserContextHolder`：必须由使用方实现，交出当前用户名与权限集合。
- `AuthMenuHandler`：必须由使用方实现，判断菜单路径是否有权限；即使不用菜单鉴权，`MenuAccessInterceptor` 也依赖它。
- `MenuApiService`：可选；不提供时启动只输出 WARN，不生成 `@AuthMenu` 菜单。
- `AuthDataHandler`：可选；默认 `JdbcAuthDataHandler`，表名与列名用 `cu.auth.auth-data-table.*` 配置。

数据权限需要一张权限表（默认 `sys_auth_data`）；菜单鉴权需要自己的菜单存储与 `AuthMenuHandler`。完整可运行示例见 [sample/auth-core-boot-sample](../../sample/auth-core-boot-sample/README.md)。

## 相关文档

- [auth 总览](../README.md)
- [权限注解与权限编辑](../docs/annotations.md)
- [RBAC 模型与扩展](../docs/rbac-and-extension.md)
- [auth-core-spring-boot-starter](../auth-core-spring-boot-starter/README.md)
- [auth-core 验证示例](../../sample/auth-core-boot-sample/README.md)
