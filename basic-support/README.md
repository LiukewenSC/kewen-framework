# basic-support

`basic-support` 是 `basic` 的持久化与通知扩展：请求日志落库、方糖消息推送、MyBatis-Plus 分页工具与若干通用工具。它由 [basic-spring-boot-starter](../boot/basic-spring-boot-starter/README.md) 装配，单独引入只提供类与 Mapper，不会自动注册监听器。

## 包结构

| 包 | 内容 |
| --- | --- |
| `log` | `RequestLogDatabasePersistentListener`、`RequestLogFangTangMessageListener` |
| `log.persistent` | `RequestLogService`、`TodayStatisticResp` |
| `mp` | `SysRequestLog` 实体、Mapper、Service |
| `notify.fangtang` | `FangTangMessageClient`、`FangTangMessageDTO` |
| `utils` | `MybatisPageExecutor`、`PageUtil`、`SnowflakeIdGenerator`、`MarkdownUtil` |
| `BasicSupportEndpoint` | `GET /request/visitStatistic` 访问统计 |

## 请求日志落库

`RequestLogDatabasePersistentListener` 监听 `basic` 发布的 `RequestLoggerEvent`，异步写入 `sys_request_log`：

- 需要 `cu.request.persistent.database=true` 且存在 `DataSource`。
- 表结构见 [relation/basic-support/basic-support.sql](../relation/basic-support/basic-support.sql)。
- 写入字段包括方法、耗时、IP、params、body、`api_path` 与 `traceId`；`traceId` 从事件对象取，不依赖异步线程的 ThreadLocal。
- 监听器带 `@Async`，使用 basic starter 的 `asyncExecutor`。

## 方糖通知

`RequestLogFangTangMessageListener` 监听同一个事件，向方糖推送请求详情：

- 需要 `cu.request.message.fang-tang=true` 且 `cu.notify.fang-tang.enabled=true`；`key` 为空时启动失败。
- 以请求来源 IP 为键做去重，缓存 30 分钟；同一 IP 在有效期内只推一次。
- 消息正文是 Markdown，由 `MarkdownUtil` 渲染。
- 发送失败只记录 WARN，不抛给调用方，不影响主流程。

## 分页工具

- `PageUtil`：`pageReq2BaomidouPage`、`baomidouPage2PageResult`、`page(...)` 系列，适合简单分页。
- `MybatisPageExecutor`：除了基本转换，还提供 `Query`（`pageConsumer` + `Wrapper`）与结果转换函数，适合需要自定义分页参数或返回类型的场景。
- 两者功能有重叠，尚未合并；新代码按团队习惯选一个，不要在同一模块里混用。

`SnowflakeIdGenerator` 提供雪花 ID；`MarkdownUtil` 提供 Markdown 段落/表格构建，主要用于通知正文。

## 访问统计端点

`BasicSupportEndpoint` 提供：

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| GET | `/request/visitStatistic?startDate=&endDate=` | 按日期范围统计访问量；参数为空时取当天 |

该端点由 basic starter 的组件扫描注册；只引 `basic-support` 时不会自动成为 Controller。

## 前置条件

- **DataSource 是硬前提**：`mp` 包与请求日志落库都依赖 MyBatis-Plus 和 DataSource。
- 请求日志落库需要 `sys_request_log` 表；完整脚本见 [relation/basic-support/basic-support.sql](../relation/basic-support/basic-support.sql)。
- 方糖通知需要 `cu.request.message.fang-tang=true` 与 `cu.notify.fang-tang.enabled=true` 同时成立。

## 相关文档

- [basic](../basic/README.md)
- [basic-spring-boot-starter](../boot/basic-spring-boot-starter/README.md)
- [basic 示例](../sample/basic-boot-sample/README.md)
- [SQL 脚本](../relation/README.md)
