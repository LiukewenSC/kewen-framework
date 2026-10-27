# tenant-spring-boot-starter

## 这是什么

多租户上下文：从请求头读取租户 ID 写入 `TenantContext`，并在 Feign 调用时把租户 ID 透传给下游。

## 何时使用

- 请求入口能提供 `tenantId` 请求头，业务代码需要在同一线程内读取租户 ID。
- 需要把租户 ID 自动透传给 Feign 下游。
- 不适用：需要按租户自动改写 SQL 或做数据隔离；本 starter 只维护上下文与 Feign 头，不碰 SQL。

## 5 分钟接入

**依赖**：`tenant-spring-boot-starter` + `basic-spring-boot-starter`。`basic-spring-boot-starter` 会传递 MyBatis-Plus 与 DataSource 要求，所以即使不使用数据库也要给一个可连接的数据源。

```xml
<dependencies>
    <dependency>
        <groupId>com.cu.framework.boot</groupId>
        <artifactId>basic-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.cu.framework.boot</groupId>
        <artifactId>tenant-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

**最小配置**：

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:tenant_demo;MODE=MySQL;DB_CLOSE_DELAY=-1
    username: sa
    password:

cu:
  tenant:
    open: true
```

**验证**：

```bash
curl -H "tenantId: 123" http://localhost:8080/hello
# hello - 123
```

示例接口从 `TenantContext.get()` 取租户 ID；不带请求头时上下文为空。完整示例见 [sample/tenant-boot-sample](../../sample/tenant-boot-sample/README.md)。

## 自动装配

| 配置类 | 生效条件 | 产出 |
| --- | --- | --- |
| `TenantConfig` | `cu.tenant.open=true` | `TenantRequestFilter`（`EarlyRequestFilter`，`@Order(2)`） |
| `TenantHeaderInterceptor` | `cu.tenant.open=true` 且 classpath 存在 Feign | Feign `RequestInterceptor`，向请求头写入 `tenantId` |

## 配置项

| key | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- |
| `cu.tenant.open` | `false` | 否 | 是否开启租户：同时启用请求过滤器与 Feign 租户头拦截器 |

## 前置条件与已知空转项

- `TenantRequestFilter` 实现的是 basic starter 的 `EarlyRequestFilter`，由 `EarlyRequestFilterProxy` 统一执行；本 starter 已传递依赖 `basic-spring-boot-starter`。
- 请求头名固定为 `tenantId`，值会按 `Long` 解析；请求头为空时不设置上下文，解析完成后 `finally` 清除。
- 请求头不是数字时会抛 `NumberFormatException`，按 500 处理。
- Feign 拦截器只在 `TenantContext` 有值时添加请求头；异步线程使用 `InheritableThreadLocal`，在创建子线程时继承当时的值。
- 本 starter 不访问任何数据表，没有建表前置。

## 相关文档

- [basic-spring-boot-starter](../basic-spring-boot-starter/README.md)
- [tenant 示例](../../sample/tenant-boot-sample/README.md)
- [全量配置参考](../../docs/application.yml)
