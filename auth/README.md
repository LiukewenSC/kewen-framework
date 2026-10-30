# auth

`auth` 是权限能力的模块组：`auth-core` 提供注解与接缝，`auth-rbac` 提供用户-角色-部门的默认实现，三个 starter 负责自动装配。只做业务权限、不想引入登录时，可以只用 core 自建权限模型；需要开箱即用的 RBAC 时，直接用 rbac 系列。

## 模块地图

| 模块 | 角色 | 什么时候用 |
| --- | --- | --- |
| [auth-core](auth-core/README.md) | 注解、权限模型接口、数据范围注入、数据操作切面、菜单拦截器 | 自建权限模型，或只想要注解能力 |
| [auth-rbac](auth-rbac/README.md) | 用户 / 部门 / 角色 / 菜单的默认 RBAC 实现，含管理端点与 MyBatis-Plus 持久层 | 接受默认的 RBAC 表结构 |
| [auth-core-spring-boot-starter](auth-core-spring-boot-starter/README.md) | core 的自动装配 | 用 core 自建模型时的入口 |
| [auth-rbac-spring-boot-starter](auth-rbac-spring-boot-starter/README.md) | RBAC 的自动装配 | 只接 RBAC，不接 Spring Security 登录态 |
| [auth-rbac-security-spring-boot-starter](auth-rbac-security-spring-boot-starter/README.md) | 把 RBAC 接到 Spring Security 登录态，提供默认 `SecurityUserDetailsService` 与当前用户端点 | 需要账密 / SSO 登录 + RBAC 权限 |

依赖方向：三个 starter 都依赖 `auth-core`；`auth-rbac-spring-boot-starter` 传递 `auth-core-spring-boot-starter`，`auth-rbac-security-spring-boot-starter` 传递 `auth-rbac-spring-boot-starter` 与 `login-security-spring-boot-starter`。

## 核心概念

| 概念 | 解决什么 | 入口 |
| --- | --- | --- |
| 菜单权限 | 这个接口 / 这组接口能不能访问 | `@AuthMenu` + `@AuthMenuDoc` |
| 数据范围 | 列表里哪些行可见 | `@AuthDataRange`，声明在 Mapper 查询方法上 |
| 数据操作 | 这一条 / 这一批数据能不能改 | `@AuthDataOperation`，请求载体实现 `DataIdCarrier` |
| 权限编辑 | 把权限写回权限表 | `AuthDataAdaptor.editDataAuths(...)` |
| 权限串 | 用户权限与数据权限的匹配单位 | `ROLE__1`、`DEPT__2`、`RELATIONDEPT__2`、`DEPT_ROLE__2_4` 等 |
| RBAC | 用户-部门-角色-菜单的默认权限来源 | `auth-rbac` |

四个注解的完整语义、声明位置、启动期校验与已知限制见 [annotations.md](docs/annotations.md)。RBAC 表结构、权限串编码、部门继承与扩展点见 [rbac-and-extension.md](docs/rbac-and-extension.md)。

## 5 分钟接入

### 路径 A：RBAC + Spring Security 登录态（推荐）

适合需要账密登录或单点登录、并接受默认 RBAC 表结构的项目。

**依赖**：`auth-rbac-security-spring-boot-starter` 会传递 `auth-rbac-spring-boot-starter` 与 `login-security-spring-boot-starter`；`basic-spring-boot-starter` 提供 Web、统一响应、异常处理与 MyBatis-Plus。

```xml
<parent>
    <groupId>com.cu.framework</groupId>
    <artifactId>cu-parent</artifactId>
    <version>0.2.1</version>
</parent>

<dependencies>
    <dependency>
        <groupId>com.cu.framework.boot</groupId>
        <artifactId>basic-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.cu.framework.auth</groupId>
        <artifactId>auth-rbac-security-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
</dependencies>
```

**建表**：先执行 [relation/auth-rbac/auth-rbac.sql](../relation/auth-rbac/auth-rbac.sql)，再准备 `sys_user` / `sys_user_credential` / `sys_user_dept` / `sys_user_role` / `sys_role` / `sys_dept` 数据。使用 MySQL 持久化会话时，还要执行 [relation/login-security/spring-session-mysql.sql](../relation/login-security/spring-session-mysql.sql)。

**最小配置**：数据源、登录参数与当前用户地址都有默认值，只需要改数据源。

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password

cu:
  auth:
    auth-data-table:
      table-name: sys_auth_data
  security:
    current-user-url: /currentUser
```

**验证**：启动后 `POST /login`（JSON）登录，再 `GET /currentUser` 应返回当前用户。密码编码器由登录 starter 提供，库里的密码需要带 `{bcrypt}` 等编码前缀；没有前缀时自行声明匹配的 `PasswordEncoder`。

想先跑起来看效果，登录态 + RBAC 直接用 [sample/auth-rbac-security-boot-sample](../sample/auth-rbac-security-boot-sample/README.md)：默认 H2、端口 8083，四个演示账号能直接对照判权矩阵；只看会议室权限案例用 [sample/auth-rbac-boot-sample](../sample/auth-rbac-boot-sample/README.md)（端口 8081）。

### 路径 B：只用 core，自建权限模型

适合已经有自己的用户/角色模型，只想要注解与权限判定能力的项目。

**依赖**：

```xml
<dependencies>
    <dependency>
        <groupId>com.cu.framework.boot</groupId>
        <artifactId>basic-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.cu.framework.auth</groupId>
        <artifactId>auth-core-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

**必须实现**：

- `AuthCurrentUserContextHolder`：交出当前用户名与权限集合。
- `AuthMenuHandler`：判断当前权限集合是否有某个菜单路径的访问权限。即使不使用菜单鉴权，`MenuAccessInterceptor` 也依赖这个 Bean。

**按需实现**：

- `MenuApiService`：把 `@AuthMenu` 注册的接口写进你自己的菜单表；不提供时启动只输出 WARN，不生成菜单。
- `AuthDataHandler`：默认是 `JdbcAuthDataHandler`（表名与列名用 `cu.auth.auth-data-table.*` 配置）；要换成缓存或其他存储时声明自己的实现。

数据权限需要一张权限表（默认 `sys_auth_data`）；菜单鉴权需要你自己的菜单存储与 `AuthMenuHandler`。完整可运行示例见 [sample/auth-core-boot-sample](../sample/auth-core-boot-sample/README.md)。

## 文档索引

- [注解与权限编辑](docs/annotations.md)
- [RBAC 模型与扩展](docs/rbac-and-extension.md)
- [auth-core-spring-boot-starter](auth-core-spring-boot-starter/README.md)
- [auth-rbac-spring-boot-starter](auth-rbac-spring-boot-starter/README.md)
- [auth-rbac-security-spring-boot-starter](auth-rbac-security-spring-boot-starter/README.md)
- [auth-core](auth-core/README.md)
- [auth-rbac](auth-rbac/README.md)
- [auth-core 验证示例](../sample/auth-core-boot-sample/README.md)
- [auth-rbac 会议室示例](../sample/auth-rbac-boot-sample/README.md)
- [框架 SQL](../relation/README.md)

## 相关文档

- [项目根 README](../README.md)
- [登录与单点登录](../boot/login-security-spring-boot-starter/README.md)
- [统一响应与异常](../basic/README.md)
