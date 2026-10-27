# RBAC 模型与扩展

本文说明 `auth-rbac` 的默认权限模型：十张表的关系、权限串怎么编解码、部门继承权限的语义，以及要替换默认实现或新增权限维度时改哪里。只使用注解、不关心 RBAC 的读者可以先看 [annotations.md](annotations.md)。

## 十张表

建表脚本在 [relation/auth-rbac/auth-rbac.sql](../../relation/auth-rbac/auth-rbac.sql)，共 10 张表和一个查看视图 `view_menu_api_auth`。

| 表 | 作用 | 关键关系 |
| --- | --- | --- |
| `sys_user` | 用户 | 主键 `id` |
| `sys_user_credential` | 用户凭证 | `user_id` 一对一关联 `sys_user` |
| `sys_user_dept` | 用户-部门 | `user_id` + `dept_id` 多对多 |
| `sys_user_role` | 用户-角色 | `user_id` + `role_id` 多对多 |
| `sys_role` | 角色 | 主键 `id` |
| `sys_dept` | 部门 | `parent_id` 自关联，形成部门树 |
| `sys_auth_data` | 数据权限 | `business_function` + `operate` + `data_id` -> `authority` |
| `sys_menu_api` | API 菜单 | `path` 是鉴权身份，`parent_id` 形成菜单树 |
| `sys_auth_menu` | 菜单权限 | `api_id` -> `authority` |
| `sys_menu_route` | 前端路由 | 路由树，可通过 `api_id` 关联 API 菜单 |

权限数据不写死在代码里：菜单能访问谁由 `sys_auth_menu` 决定，某条业务数据能编辑/预约由 `sys_auth_data` 决定。

## 权限串

权限串是用户权限与数据权限的匹配单位，格式由 `AuthConstant` 约定：

- 主分隔符：`__`
- 次级分隔符：`_`

| 权限实体 | 权限串示例 | 说明 |
| --- | --- | --- |
| `User` | `USER__1` | 用户本人 |
| `Role` | `ROLE__4` | 角色 |
| `Dept` | `DEPT__2` | 部门成员 |
| `ParentDept` | `RELATIONDEPT__2` | 部门 2 的**严格子孙部门**成员 |
| `DeptRole` | `DEPT_ROLE__2_4` | 部门 2 与角色 4 的组合 |

描述串使用同样的结构，值换成名称，例如 `DEPT_ROLE__研发部_管理员`。

编解码规则：

- `IFlagAuthEntity.getAuth()` 按 `标记__各位置ID` 拼接权限串，按 `标记__各位置名称` 拼接描述。
- `AbstractAuthObject.parseBaseAuth(...)` 取 `__` 前的标记，从启动时扫描到的 `IFlagAuthEntity` 实现里找到类，实例化后按 `_` 拆分填充。
- 实体类必须保留无参构造，否则项目启动或解析权限串时抛异常。
- 标记本身必须只占一个位置（不含 `_`）；例如 `RELATIONDEPT` 合法，旧的 `DEPT_RELATION` 会被拆成两个位置，导致权限串错位。
- `AbstractIdNameFlagAuthEntity` 内置 `Integer` / `Long` / `String` 三种 ID 转换；其他主键类型需要自行扩展转换器。

## 配置权限与用户权限

`auth-core` 只定义 `IAuthObject` 与 `AbstractAuthObject`。`auth-rbac` 有两个方向不同的实现：

| 类 | 用途 | 特点 |
| --- | --- | --- |
| `SimpleAuthObject` | 配置权限时传入的结构体 | 支持 `USER` / `ROLE` / `DEPT` / `RELATIONDEPT` / `DEPT_ROLE`；可反解析权限串 |
| `UserAuthInfo` | 用户登录后装载出的权限体 | 包含用户、角色、部门及上级链；`parseBaseAuth` 直接抛异常，用户权限不做逆向 |

`UserAuthInfo.listBaseAuth()` 在用户缺失时返回空集合，而不是抛异常；`RbacSecurityUserDetailsService` 在权限体缺失时给出空权限集合，认证流程继续。没有凭证的用户是合法状态，不应该让登录失败。

## 部门继承：RELATIONDEPT

两个标记正交，不要混用：

- `DEPT__B`：部门 B 自己的成员。
- `RELATIONDEPT__B`：部门 B 的严格子孙部门成员；B 自己的成员**不会**因为这条授权获得权限。

所以“把权限配在部门 B 上，B 的成员与 B 的子部门成员结果不同”是预期行为。

展开时机与存储：

- 用户每次装载权限时，`UserAuthInfo.fillUserAuth` 为每个部门展开严格上级链，并把部门与角色两两组合成 `DEPT_ROLE`。
- `SysUserCompositeImpl.listAncestorDepts` 从 `parent_id` 向上走，**不包含自身**；用 `visited` 集合处理自引用与成环，保证有限步终止。
- 展开出的继承权限只存在内存，不写回用户的授权数据；部门层级调整后下一次装载即生效，不需要重刷任何用户。
- `ParentDept.level` 只表示距本部门的层级，不参与权限判定，不要依赖它做授权。

当前实现与 `auth/dept-inherited-access` 规格一致：`RELATIONDEPT__B` 只覆盖严格子孙部门。

## 权限粒度

- **数据创建、删除**：由菜单权限控制。
- **数据编辑**：由 `@AuthDataOperation` 的操作类型（如 `edit_info`）控制。
- **数据使用**：由另一个操作类型（如 `appointment`）控制。
- **主权限管理**：创建后交于其他人管理的最大权限；这类接口通常单独用菜单权限控制，避免把“主权限”交给普通数据操作。

推荐把菜单权限留给高级接口（创建、删除、修改主权限），其余数据操作接口可以放给所有人再由数据权限细判，避免两种权限混在一起。

## 扩展点

| 扩展点 | 默认实现 | 替换方式 |
| --- | --- | --- |
| `AuthCurrentUserContextHolder` | 无；`auth-rbac-security` 提供基于 Spring Security 的实现 | 声明自己的 Bean |
| `AuthMenuHandler` | `RbacAnnotationAuthHandler`（只做菜单） | 声明 `AuthMenuHandler` Bean，默认实现让位 |
| `AuthDataHandler` | `JdbcAuthDataHandler` | 声明 `AuthDataHandler` Bean，默认实现让位 |
| `MenuApiService` | `RbacMenuApiService` | 声明自己的 `MenuApiService` Bean |
| `SysUserComposite` | `SysUserCompositeImpl` | 声明自己的 `SysUserComposite` Bean |
| `SysAuthMenuComposite` | `DefaultSysAuthMenuComposite` | 声明自己的 `SysAuthMenuComposite` Bean |
| `AuthMenuStore` | `MemoryExpiredAuthMenuStore`（10 分钟过期） | 声明 `AuthMenuStore` Bean，例如 Redis 实现 |

`RbacAnnotationAuthHandler` 只实现 `AuthMenuHandler`，不处理数据权限；数据权限的默认实现是 `auth-core` 的 `JdbcAuthDataHandler`。

### 新增一个权限维度（以岗位为例）

需要同时改“配置侧”和“用户侧”，否则权限串能解析，但用户身上没有装载来源。

1. 新建权限实体，继承 `AbstractIdNameFlagAuthEntity<Long>`，`flag()` 返回单段标记（如 `POSITION`），保留无参构造。
2. 配置侧：新建 `PositionAuthObject extends SimpleAuthObject`，重写 `setAnotherBaseAuth` / `addAnotherBashAuth`，把 `Position` 收进自己的集合，并在 `listBaseAuth()` 中输出。
3. 用户侧：在 `UserAuthInfo` 的子类或自定义装载逻辑里加入岗位权限；如果沿用默认查询，需要扩展 `SysUserComposite` 或替换 `getUserAuthInfo` 的映射，把岗位数据一起装载。
4. 编辑权限的请求体使用新的配置侧权限对象；`AuthDataAdaptor` 接收的是 `IAuthObject`，不需要改框架签名。
5. 在 `sys_auth_data` / `sys_auth_menu` 中配置新标记的权限串，例如 `POSITION__7`。

如果只加实体、不装载，权限串永远匹配不到用户；如果只改装载、不加实体，`parseBaseAuth` 会因为标记未注册而抛 `AuthEntityException`。

### 替换用户装载

默认实现 `SysUserCompositeImpl` 从 `sys_user` / `sys_user_credential` / `sys_user_dept` / `sys_user_role` / `sys_dept` 装载，并在 `fillUserAuth` 中展开上级链。要接自己的用户中心：

- 实现 `SysUserComposite`，返回 `UserAuthObject`（含 `SysUser`、`SysUserCredential`、`UserAuthInfo`）。
- 声明为 Bean；`auth-rbac-spring-boot-starter` 的扫描会使用它。
- 保持 `UserAuthInfo.listBaseAuth()` 的语义：用户缺失时返回空集合，不抛异常。

## 相关文档

- [auth 总览](../README.md)
- [权限注解与权限编辑](annotations.md)
- [auth-rbac 模块](../auth-rbac/README.md)
- [auth-rbac-spring-boot-starter](../auth-rbac-spring-boot-starter/README.md)
- [auth-rbac-security-spring-boot-starter](../auth-rbac-security-spring-boot-starter/README.md)
- [建表脚本](../../relation/auth-rbac/auth-rbac.sql)
