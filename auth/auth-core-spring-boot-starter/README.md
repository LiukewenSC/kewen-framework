# auth-core-spring-boot-starter

## 这是什么

`auth-core` 的自动装配：把数据权限、菜单权限、权限拒绝响应通道装配起来。它不认识用户、角色、部门，只认使用方提供的当前用户上下文与菜单处理器。

## 何时使用

- 你已经有自己的用户/角色模型，只想用注解完成菜单与数据权限。
- 你需要自定义 `AuthDataHandler` 或 `MenuApiService`，不想引入整套 RBAC。
- 不适用：想开箱即用用户-角色-部门模型时，用 `auth-rbac-spring-boot-starter`；要接 Spring Security 登录态时，用 `auth-rbac-security-spring-boot-starter`。

## 5 分钟接入

**依赖**：`auth-core-spring-boot-starter` + `basic-spring-boot-starter` + MyBatis + 数据库驱动。

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
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>2.3.1</version>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

**建表**：数据权限最小需要 `sys_auth_data`。完整脚本见 [relation/auth-rbac/auth-rbac.sql](../../relation/auth-rbac/auth-rbac.sql)；只做数据权限时可以只建这一张表，再用 `cu.auth.auth-data-table.*` 把表名与列名指过去。`@AuthMenu` 的菜单生成另需菜单表与 `MenuApiService`。

**最小 Bean**：`AuthCurrentUserContextHolder` 与 `AuthMenuHandler` 是硬前提；没有它们应用起不来。

```java
@Configuration
public class AuthBeans {

    @Bean
    AuthCurrentUserContextHolder authCurrentUserContextHolder() {
        return new AuthCurrentUserContextHolder() {
            @Override
            public CurrentUser getUser() {
                return new CurrentUser().setUsername("demo");
            }

            @Override
            public Collection<FlatteningAuthority> getAuths() {
                // 真实项目从登录态或自己的权限服务取；这里返回空集合只用于启动
                return Collections.emptyList();
            }
        };
    }

    @Bean
    AuthMenuHandler authMenuHandler() {
        // 不使用菜单鉴权时给一个放行实现；使用菜单时换成真正的判定
        return (auths, path) -> true;
    }
}
```

**最小配置**：默认表名就是 `sys_auth_data`，使用自定义表时才需要覆盖。

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:auth_demo;MODE=MySQL;DB_CLOSE_DELAY=-1
    username: sa
    password:

cu:
  auth:
    auth-data-table:
      table-name: sys_auth_data
```

**验证**：给一个方法标 `@AuthDataOperation(businessFunction = "demo", operate = "edit")`，请求体实现 `DataIdCarrier`；权限表里给当前用户配置一条 `demo/edit/<dataId>` 的 authority 后请求返回 200，没有配置时返回 HTTP 403 + `code=403` + `errorType=AUTHORIZATION`。用法错误（没有承载对象、ID 为空、超过 100 条）返回 HTTP 400 + `code=400` + `errorType=USAGE`。

完整可运行示例见 [sample/auth-core-boot-sample](../../sample/auth-core-boot-sample/README.md)，它用自定义 `demo_*` 表和自实现的上下文/菜单处理器验证 core 的全部接缝。

## 自动装配

| 配置类 | 生效条件 | 产出 |
| --- | --- | --- |
| `AuthCoreConfig` | 无 | `DataCheckAspect`、`AuthRangeCache`、`JdbcAuthDataHandler`（使用方已有 `AuthDataHandler` 时让位）、`AuthDataTable`、`SqlAuthInject`、`MybatisDataRangeInterceptor`、`AuthRangeStartupValidator`、`JdbcAuthDataPersistent`；菜单：`MenuAccessInterceptor`、`MenuApiGeneratorProcessor`、`InitMenuApiCommandLineRunner`；拒绝通道：`AuthExceptionHandler`（仅 Web 且使用方未定义时） |
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

完整键表与默认值见 [docs/application.yml](../../docs/application.yml)。

## 前置条件与已知空转项

- 需要 `JdbcTemplate`（DataSource）；`JdbcAuthDataPersistent` 直接依赖它。
- 数据范围能力需要 MyBatis：`MybatisDataRangeInterceptor` 挂在 `SqlSessionFactory` 上；没有 `SqlSessionFactory` 时 `AuthRangeStartupValidator` 跳过扫描，`@AuthDataRange` 也不会生效。
- 必须提供 `AuthCurrentUserContextHolder` 与 `AuthMenuHandler`，否则启动失败。
- 表名与七个列名必须与实际表一致；默认值对应 [relation/auth-rbac/auth-rbac.sql](../../relation/auth-rbac/auth-rbac.sql) 的 `sys_auth_data`。
- `MenuApiGeneratorProcessor` 只有容器中存在 `MenuApiService` 时才生成菜单；没有时只输出 WARN，不阻塞启动。
- `AuthExceptionHandler` 只在 Web 应用且使用方未定义同类处理器时注册；非 Web 场景没有该拒绝通道。
- 当前没有 `@AuthDataAuthEdit`；编辑数据权限用 `AuthDataAdaptor`，见 [auth/docs/annotations.md](../docs/annotations.md)。

## 相关文档

- [auth 总览](../README.md)
- [权限注解与权限编辑](../docs/annotations.md)
- [RBAC 模型与扩展](../docs/rbac-and-extension.md)
- [auth-core](../auth-core/README.md)
- [auth-core 验证示例](../../sample/auth-core-boot-sample/README.md)
- [全量配置参考](../../docs/application.yml)
