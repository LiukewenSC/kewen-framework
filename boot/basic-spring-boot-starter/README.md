# basic-spring-boot-starter

## 这是什么

基础 Web 能力的自动装配：异步线程池、请求追踪与请求日志、统一异常/响应格式、MyBatis-Plus 分页、方糖消息通知。它是大多数项目的第一个依赖。

## 何时使用

- 需要统一 `Result` 响应、全局异常处理、traceId、请求日志与分页。
- 使用 MyBatis-Plus 访问数据库。
- 不适用：非 Web 或不用 MyBatis-Plus 的项目；此时可以直接依赖 [basic](../../basic/README.md) 的基础库，不要引本 starter。

## 5 分钟接入

**依赖**：

```xml
<dependency>
    <groupId>com.cu.framework.boot</groupId>
    <artifactId>basic-spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

**建表**：默认不落请求日志，可以先不建表。启用 `cu.request.persistent.database=true` 或文件存储时，执行 [relation/basic-support/basic-support.sql](../../relation/basic-support/basic-support.sql)（`sys_request_log`、`file_detail`、`file_part_detail`、`sys_storage_file`）。

**最小配置**：H2 本地演示只需要数据源。

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:demo;MODE=MySQL;DB_CLOSE_DELAY=-1
    username: sa
    password:
```

**启动**：普通 Spring Boot 应用即可。starter 会注册请求追踪、请求日志、统一异常与响应 advice、MyBatis-Plus 分页；`GET /request/visitStatistic` 是 `basic-support` 带来的访问统计端点。

**验证**：

```bash
curl http://localhost:8080/request/visitStatistic
```

返回 `Result` 结构；如果开启了请求日志落库，`sys_request_log` 会多出本次请求的记录。完整示例见 [sample/basic-boot-sample](../../sample/basic-boot-sample/README.md)。

## 自动装配

| 配置类 | 生效条件 | 产出 |
| --- | --- | --- |
| `AsyncConfig` | 无 | `@EnableAsync` + `asyncExecutor`（核心 2×CPU+1、最大 5×CPU、队列 2×CPU、`CallerRunsPolicy`）与异步异常打印 |
| `BasicSupportConfig` | 无 | 扫描 `com.cu.framework.basic.support` 及其 Mapper；按开关注册请求日志落库监听器、方糖通知监听器 |
| `EarlyFilterConfig` | 无 | `TraceRequestFilter`、`RequestLoggerFilter`、`EarlyRequestFilterProxy` |
| `FangTangMessageConfig` | `cu.notify.fang-tang.enabled=true` | `FangTangMessageClient`；`key` 或 `domain` 为空会启动失败 |
| `MybatisPlusConfig` | 无 | `MybatisPlusInterceptor`（MySQL 分页） |
| `RequestResponseFormatConfig` | 无 | 全局异常 advice、链路流水号响应 advice、Jackson 日期格式、`Date` / `LocalDate` / `LocalDateTime` 转换器 |

## 配置项

| key | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- |
| `cu.request.persistent.database` | `false` | 否 | 是否把请求日志落库；需要 `sys_request_log` 表 |
| `cu.request.message.fang-tang` | `false` | 否 | 是否把请求来源 IP 推送到方糖；需与 `cu.notify.fang-tang.enabled` **同时为 true** |
| `cu.notify.fang-tang.enabled` | `false` | 否 | 是否创建方糖消息客户端；false 时不创建 |
| `cu.notify.fang-tang.key` | — | `enabled=true` 时必填 | 方糖 SendKey；为空会启动失败 |
| `cu.notify.fang-tang.domain` | `https://sctapi.ftqq.com` | 否 | 方糖推送接口域名 |

完整键表见 [docs/application.yml](../../docs/application.yml)。

## 前置条件与已知空转项

- **DataSource 是硬前提**：本 starter 传递引入 MyBatis-Plus、MySQL 驱动与 p6spy，`BasicSupportConfig` 还会执行 `@MapperScan`；没有 DataSource 时启动失败。
- `cu.request.persistent.database=true` 需要 `sys_request_log` 表，脚本见 [relation/basic-support/basic-support.sql](../../relation/basic-support/basic-support.sql)。
- 请求日志的方糖通知需要 `cu.request.message.fang-tang=true` 且 `cu.notify.fang-tang.enabled=true`；后者为 true 时 `key` 不能为空。
- `AsyncConfig` 的线程池参数是硬编码的，没有配置项。
- 请求日志过滤器会读取请求体；非 UTF-8 或非 JSON 请求体的边界行为见 [basic-support](../../basic-support/README.md) 与相关规格，当前没有额外开关。
- 无自有“空转”键。

## 相关文档

- [basic](../../basic/README.md)
- [basic-support](../../basic-support/README.md)
- [basic 示例](../../sample/basic-boot-sample/README.md)
- [SQL 脚本](../../relation/README.md)
- [全量配置参考](../../docs/application.yml)
