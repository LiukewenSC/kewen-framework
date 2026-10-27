# auth-rbac-spring-boot-starter

## 这是什么

默认 RBAC 实现的自动装配：把 `SysUserComposite`、`SysAuthMenuComposite`、RBAC 控制器、菜单 API 服务以及默认的 `AuthMenuHandler` 装配起来。它不绑定 Spring Security 登录态，当前用户由使用方提供。

## 何时使用

- 你接受默认的用户-角色-部门-菜单表结构，并有自己的登录态或身份来源。
- 你只需要 RBAC 权限，不想引入 `login-security-spring-boot-starter`。
- 不适用：需要账密 / SSO 登录态时，用 `auth-rbac-security-spring-boot-starter`，它会把本 starter 与登录 starter 一起带上。

## 5 分钟接入

**依赖**：`auth-rbac-spring-boot-starter` 会传递 `auth-core-spring-boot-starter`；再加 `basic-spring-boot-starter` 与数据库驱动。

```xml
<dependencies>
    <dependency>
        <groupId>com.cu.framework.boot</groupId>
        <artifactId>basic-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.cu.framework.auth</groupId>
        <artifactId>auth-rbac-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
</dependencies>
```

**建表**：执行 [relation/auth-rbac/auth-rbac.sql](../../relation/auth-rbac/auth-rbac.sql)，共 10 张表 + `view_menu_api_auth` 视图。

**最小 Bean**：本 starter 不提供 `PasswordEncoder` 与 `AuthCurrentUserContextHolder`，需要自己声明；`AuthMenuHandler` 由 RBAC 默认提供。

```java
@Configuration
public class AuthBeans {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthCurrentUserContextHolder authCurrentUserContextHolder(SysUserComposite sysUserComposite) {
        return new AuthCurrentUserContextHolder() {
            @Override
            public CurrentUser getUser() {
                // 真实项目从登录态取；示例用请求头
                return new CurrentUser().setUsername("admin");
            }

            @Override
            public Collection<FlatteningAuthority> getAuths() {
                UserAuthObject authObject =
                        sysUserComposite.getUserAuthObjectByUsername(getUser().getUsername());
                return authObject == null || authObject.getAuthInfo() == null
                        ? Collections.emptyList()
                        : authObject.getAuthInfo().listBaseAuth();
            }
        };
    }
}
```

**最小配置**：默认表名与列名已经对应脚本，只需要数据源。

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

**验证**：启动后 `GET /rbac/user/page` 返回分页用户；`GET /menu/api/tree` 返回菜单树。用户权限来自 `sys_user*` / `sys_role` / `sys_dept` 与权限表，具体数据需要自己准备；也可以直接用 [sample/auth-rbac-boot-sample](../../sample/auth-rbac-boot-sample/README.md) 的 H2 种子数据。

## 自动装配

| 配置类 | 生效条件 | 产出 |
| --- | --- | --- |
| `AuthRbacScanConfig` | 无 | `@MapperScan("com.cu.framework.auth.rbac.**.mapper")`；扫描 `mp.service.impl`、`composite`、`controller` |
| `AuthRbacConfig` | `@AutoConfigureAfter(AuthCoreConfig, AuthRbacScanConfig)` | `RbacAnnotationAuthHandler`（使用方已有 `AuthMenuHandler` 时让位）、`AuthDataAdaptor`、`RbacCurrentUserService`、`RbacMenuApiService`、`AuthMenuStore`（未定义时用 `MemoryExpiredAuthMenuStore`） |

## 配置项

| key | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- |
| `cu.auth.cache-auth` | `false` | 否 | 是否缓存菜单与权限数据；false 时每次直接查库。true 时使用 `AuthMenuStore`，默认内存实现 10 分钟过期 |

完整键表见 [docs/application.yml](../../docs/application.yml)。

## 前置条件与已知空转项

- 需要 MyBatis-Plus 与 DataSource；建表脚本见 [relation/auth-rbac/auth-rbac.sql](../../relation/auth-rbac/auth-rbac.sql)。
- 需要 `PasswordEncoder` 与 `AuthCurrentUserContextHolder`；单独使用本 starter 时由使用方提供，或改用 [auth-rbac-security-spring-boot-starter](../auth-rbac-security-spring-boot-starter/README.md)（它提供两者）。
- `cu.auth.cache-auth=true` 时菜单与权限写入 `AuthMenuStore`，默认 `MemoryExpiredAuthMenuStore`；替换为 Redis 等实现时声明自己的 `AuthMenuStore` Bean。
- 默认 `AuthMenuHandler` 是 `RbacAnnotationAuthHandler`，只做菜单；数据权限默认由 `auth-core` 的 `JdbcAuthDataHandler` 处理。
- 当前没有 `@AuthDataAuthEdit`；数据权限编辑用 `AuthDataAdaptor`，见 [auth/docs/annotations.md](../docs/annotations.md)。
- 无其他“空转”键。

## 相关文档

- [auth 总览](../README.md)
- [RBAC 模型与扩展](../docs/rbac-and-extension.md)
- [权限注解与权限编辑](../docs/annotations.md)
- [auth-rbac](../auth-rbac/README.md)
- [auth-rbac-security-spring-boot-starter](../auth-rbac-security-spring-boot-starter/README.md)
- [auth-rbac 会议室示例](../../sample/auth-rbac-boot-sample/README.md)
- [全量配置参考](../../docs/application.yml)
