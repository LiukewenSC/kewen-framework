# auth-rbac

`auth-rbac` 是权限框架的默认 RBAC 实现：用户、部门、角色、菜单 API、菜单路由与权限数据的持久层、组合服务和管理端点都在这里。它是库模块，不负责自动装配；实际接入用 [auth-rbac-spring-boot-starter](../auth-rbac-spring-boot-starter/README.md)。

## 包结构

| 包 | 职责 |
| --- | --- |
| `composite` | 用户权限与菜单权限的组合服务接口：`SysUserComposite`、`SysAuthMenuComposite`、`AuthMenuStore` |
| `composite.impl` | 默认实现：`SysUserCompositeImpl`、`DefaultSysAuthMenuComposite`、`MemoryExpiredAuthMenuStore` |
| `composite.mapper` | 用户-角色-部门联合查询 `SysUserUnionCompositeMapper` |
| `composite.model` | 权限实体与权限体：`User`、`Role`、`Dept`、`ParentDept`、`DeptRole`、`SimpleAuthObject`、`UserAuthInfo` |
| `mp` | MyBatis-Plus 的实体、Mapper、Service：`sys_user*`、`sys_role`、`sys_dept`、`sys_auth_data`、`sys_auth_menu`、`sys_menu_api`、`sys_menu_route` |
| `controller` | RBAC 管理端点：用户、角色、部门、菜单 API、菜单路由、当前用户路由与改密 |
| `extension` | `RbacCurrentUserService`、`RbacMenuApiService` 等对 core 接缝的实现 |
| `model` | 请求/响应模型、菜单类型常量、`UserAuthObject` |
| `RbacAnnotationAuthHandler` | 菜单权限的默认处理器，只实现 `AuthMenuHandler` |

## 默认 Bean

| Bean | 实现 | 说明 |
| --- | --- | --- |
| `SysUserComposite` | `SysUserCompositeImpl` | 按用户名装载用户、凭证与权限体；`listAncestorDepts` 展开严格上级链 |
| `SysAuthMenuComposite` | `DefaultSysAuthMenuComposite` | 菜单鉴权、菜单树、路由树、菜单权限编辑 |
| `AuthMenuStore` | `MemoryExpiredAuthMenuStore` | `cu.auth.cache-auth=true` 时的内存缓存，10 分钟过期 |
| `AuthMenuHandler` | `RbacAnnotationAuthHandler` | 菜单鉴权；使用方声明同类型 Bean 时让位 |
| `AuthDataAdaptor` | `auth-core` 的 `AuthDataAdaptor` | 由 `auth-rbac-spring-boot-starter` 用 `AuthDataHandler` 组装 |
| `RbacCurrentUserService` | `auth-rbac` 自带 | 当前用户、改密等扩展服务 |
| `RbacMenuApiService` | `auth-rbac` 自带 | 菜单 API 的持久化实现 |

`auth-rbac` 不提供 `AuthCurrentUserContextHolder`，也不提供 `PasswordEncoder`。只用本模块时这两个 Bean 需要使用方自己声明；接 Spring Security 登录态时由 [auth-rbac-security-spring-boot-starter](../auth-rbac-security-spring-boot-starter/README.md) 提供默认实现。

## 管理端点

| 路径 | 用途 |
| --- | --- |
| `/rbac/user/*` | 用户列表、分页、新增、修改、删除 |
| `/rbac/role/*` | 角色列表、分页、新增、修改、删除 |
| `/rbac/dept/*` | 部门列表、分页、新增、修改、删除 |
| `/menu/api/tree` | API 菜单树（含权限） |
| `/menu/api/update` | 更新菜单名称、备注、权限类型、父节点与权限；请求路径不可改 |
| `/menu/route/tree` | 前端路由树 |
| `/currentUser/routeMenus` | 当前用户可见的路由树 |
| `/currentUser/updatePassword` | 修改当前用户密码 |

`GET /currentUser` 由 `auth-rbac-security-spring-boot-starter` 的 `RbacCurrentUserEndpoint` 提供，不在本模块。

## 数据表

依赖 [relation/auth-rbac/auth-rbac.sql](../../relation/auth-rbac/auth-rbac.sql) 的 10 张表；表关系、权限串编码与部门继承语义见 [auth/docs/rbac-and-extension.md](../docs/rbac-and-extension.md)。

## 替换点

- 接自己的用户中心：实现 `SysUserComposite` 并声明为 Bean。
- 换菜单实现或缓存：实现 `SysAuthMenuComposite` 或 `AuthMenuStore`。
- 换菜单鉴权：实现 `AuthMenuHandler`；`RbacAnnotationAuthHandler` 会在使用方已提供时让位。
- 换数据权限存储：实现 `AuthDataHandler`；默认 `JdbcAuthDataHandler` 会在使用方已提供时让位。
- 自定义权限维度（岗位等）：见 [auth/docs/rbac-and-extension.md](../docs/rbac-and-extension.md#新增一个权限维度以岗位为例)。

## 相关文档

- [auth 总览](../README.md)
- [RBAC 模型与扩展](../docs/rbac-and-extension.md)
- [权限注解与权限编辑](../docs/annotations.md)
- [auth-rbac-spring-boot-starter](../auth-rbac-spring-boot-starter/README.md)
- [auth-rbac-security-spring-boot-starter](../auth-rbac-security-spring-boot-starter/README.md)
- [auth-rbac 会议室示例](../../sample/auth-rbac-boot-sample/README.md)
