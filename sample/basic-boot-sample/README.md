# basic-boot-sample

## 演示什么

基础 Web 能力的端到端示例：请求追踪与请求日志、过滤器顺序、Jackson 日期序列化、参数校验、异步线程池、Spring 生命周期监听与异常上报。默认 H2 内存库，端口 8080。

## 零配置启动

```bash
cd sample/basic-boot-sample
mvn spring-boot:run
```

默认 `h2` profile，H2 控制台：<http://localhost:8080/h2-console>，JDBC URL 用 `application.yml` 里的 `jdbc:h2:mem:sample;...`。

> 示例默认打开了请求日志落库与方糖推送。没有真实方糖 SendKey 时，推送会失败但只记 WARN；不需要时把 `cu.notify.fang-tang.enabled` 与 `cu.request.message.fang-tang` 改为 `false`。

## 打开什么

| 请求 | 用途 |
| --- | --- |
| `GET /filter/hello` | 验证 early filter 顺序与统一响应 |
| `GET /jackson/paramSerializer` | 日期序列化 |
| `GET /jackson/paramSerializer2` / `paramSerializer3` | 其他日期格式 |
| `GET /jackson/deSerializerDate` | 日期反序列化 |
| `POST /validated/hello` | 参数校验失败时的统一异常响应 |

## 验证清单

```bash
curl http://localhost:8080/filter/hello
curl http://localhost:8080/jackson/paramSerializer
curl -X POST http://localhost:8080/validated/hello \
  -H "Content-Type: application/json" -d '{}'
```

- 每个响应都是 `Result` 结构，成功 `code=200`。
- 校验失败返回统一失败结构，消息里带字段名。
- 打开 H2 控制台查 `sys_request_log`，能看到本次请求的记录（`cu.request.persistent.database=true`）。
- 响应体里带 `traceId`；请求头传 `traceId: abc` 时沿用该值。

## 不覆盖什么

- 不演示 RBAC、登录、文件存储和多租户；这些各有独立示例。
- 方糖推送需要真实 SendKey，示例只验证“开关与监听器在线”，不保证消息送达。
- 异步线程池参数是硬编码的，示例不提供调参入口。

## 相关文档

- [basic-spring-boot-starter](../../boot/basic-spring-boot-starter/README.md)
- [basic](../../basic/README.md)
- [basic-support](../../basic-support/README.md)
- [项目根 README](../../README.md)
