# basic-spring-boot-starter

基础 Web 能力自动装配：异步线程池、请求追踪与请求日志、统一异常/响应格式、MyBatis-Plus 分页、方糖消息通知。

## 自动装配

| 配置类 | 生效条件 | 产出 |
| --- | --- | --- |
| `AsyncConfig` | 无 | `@EnableAsync` + `asyncExecutor`（核心 2×CPU+1、最大 5×CPU、队列 2×CPU、`CallerRunsPolicy`）与异步异常打印 |
| `BasicSupportConfig` | 无 | 扫描 `com.cu.framework.basic.support` 及其 Mapper；按开关注册请求日志落库监听器、方糖通知监听器 |
| `EarlyFilterConfig` | 无 | `TraceRequestFilter`、`RequestLoggerFilter`、`EarlyRequestFilterProxy` |
| `FangTangMessageConfig` | `cu.notify.fang-tang.enabled=true` 时创建客户端 | `FangTangMessageClient`；`key` 或 `domain` 为空会启动失败 |
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

## 启动前置条件与已知空转项

- **DataSource 是硬前提**：本 starter 传递引入 MyBatis-Plus、MySQL 驱动与 p6spy，`BasicSupportConfig` 还会执行 `@MapperScan`；没有 DataSource 时启动失败。
- `cu.request.persistent.database=true` 需要 `sys_request_log` 表，建表脚本见 [docs/sql/basic-support.sql](../../docs/sql/basic-support.sql)。
- 请求日志的方糖通知需要 `cu.request.message.fang-tang=true` 且 `cu.notify.fang-tang.enabled=true`；后者为 true 时 `key` 不能为空。
- 无自有“空转”键；`AsyncConfig` 的线程池参数是硬编码的，没有配置项。

## 完整示例

见 [docs/application.yml](../../docs/application.yml) 的 `basic-spring-boot-starter` 分节。
