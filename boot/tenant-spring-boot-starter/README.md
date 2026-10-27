# tenant-spring-boot-starter

多租户上下文：从请求头读取租户 ID 写入 `TenantContext`，并在 Feign 调用时把租户 ID 透传给下游。

## 自动装配

| 配置类 | 生效条件 | 产出 |
| --- | --- | --- |
| `TenantConfig` | `cu.tenant.open=true` | `TenantRequestFilter`（`EarlyRequestFilter`，`@Order(2)`） |
| `TenantHeaderInterceptor` | `cu.tenant.open=true` 且 classpath 存在 Feign | Feign `RequestInterceptor`，向请求头写入 `tenantId` |

## 配置项

| key | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- |
| `cu.tenant.open` | `false` | 否 | 是否开启租户：同时启用请求过滤器与 Feign 租户头拦截器 |

## 启动前置条件与已知空转项

- `TenantRequestFilter` 实现的是 basic starter 的 `EarlyRequestFilter`，由 `EarlyRequestFilterProxy` 统一执行；本 starter 已传递依赖 `basic-spring-boot-starter`。
- 请求头名固定为 `tenantId`，值会按 `Long` 解析；请求头为空时不设置上下文，解析完成后 `finally` 清除。
- 请求头不是数字时会抛 `NumberFormatException`，按 500 处理。
- Feign 拦截器只在 `TenantContext` 有值时添加请求头；异步线程使用 `InheritableThreadLocal`，在创建子线程时继承当时的值。
- 本 starter 不访问任何数据表，没有建表前置。

## 完整示例

见 [docs/application.yml](../../docs/application.yml) 的 `tenant-spring-boot-starter` 分节。
