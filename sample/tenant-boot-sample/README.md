# tenant-boot-sample

## 演示什么

`tenant-spring-boot-starter` 的租户上下文：从请求头 `tenantId` 读取租户 ID，写入 `TenantContext`，业务代码在同一线程内读取。默认 H2 内存库，端口 8080；示例不访问任何表。

## 零配置启动

```bash
cd sample/tenant-boot-sample
mvn spring-boot:run
```

默认 `h2` profile。`basic-spring-boot-starter` 传递了 MyBatis-Plus 与 DataSource 要求，所以即使不建表也需要一个可连接的数据源。

## 打开什么

| 请求 | 用途 |
| --- | --- |
| `GET /hello` | 返回 `hello - <tenantId>`，tenantId 来自 `TenantContext` |

## 验证清单

```bash
curl -H "tenantId: 123" http://localhost:8080/hello
# hello - 123

curl http://localhost:8080/hello
# hello - null
```

- 请求头名固定为 `tenantId`，值按 `Long` 解析。
- 请求头为空时不设置上下文，业务侧读到 `null`。
- 请求头不是数字时会抛 `NumberFormatException`，按 500 处理。

## 不覆盖什么

- 不做 SQL 级数据隔离，也不自动改写 SQL；本 starter 只维护上下文与 Feign 头。
- 示例没有 Feign 客户端，Feign 透传分支未演示。
- 不演示异步线程的租户传播；实现使用 `InheritableThreadLocal`，创建子线程时会继承当时的值。

## 相关文档

- [tenant-spring-boot-starter](../../boot/tenant-spring-boot-starter/README.md)
- [basic-spring-boot-starter](../../boot/basic-spring-boot-starter/README.md)
- [项目根 README](../../README.md)
