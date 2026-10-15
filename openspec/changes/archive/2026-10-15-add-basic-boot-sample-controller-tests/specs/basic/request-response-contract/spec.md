# Spec Delta

## Purpose

约束 basic-boot-sample 的探针接口所体现的对外 HTTP 契约——统一响应信封、流水号回显、日期参数的绑定与序列化格式、校验与类型转换失败时的返回形状。这些行为已被调用方依赖，此前只存在于实现中而没有任何声明。

## ADDED Requirements

### Requirement: 统一响应信封

接口返回 SHALL 为包含 `code`、`success`、`message`、`data`、`traceId` 五个字段的 JSON 对象。业务数据为空时 `data` MUST 以 `null` 出现，而不是被省略。

#### Scenario: 无业务数据的请求处理成功

- **WHEN** 请求不返回业务数据的探针接口且处理成功
- **THEN** `code` 为 200，`success` 为 true，`message` 为"成功"
- **AND** `data` 字段存在且值为 `null`

#### Scenario: 请求处理失败仍返回同一信封

- **WHEN** 请求因参数校验、类型转换或请求体解析失败
- **THEN** `success` 为 false，`code` 为 500
- **AND** 响应体仍可解析为同一信封结构，不返回未包装的异常信息

### Requirement: 响应回显请求流水号

系统 SHALL 在响应的 `traceId` 字段返回本次请求的流水号。请求未携带流水号时 MUST 自行生成一个非空值，而不是返回空字段。该字段同时是流水号处理链路在本次请求中生效的外部可观测证据。

#### Scenario: 请求头未携带流水号

- **WHEN** 发出不带流水号请求头的请求
- **THEN** 响应的 `traceId` 存在且为非空字符串

#### Scenario: 请求头携带流水号

- **WHEN** 发出携带指定流水号请求头的请求
- **THEN** 响应的 `traceId` 等于该请求头的值

### Requirement: 日期参数按固定格式绑定并回显

`LocalDate` 参数 SHALL 按 `yyyy-MM-dd` 解析，`LocalDateTime` 参数 SHALL 按 `yyyy-MM-dd HH:mm:ss` 解析。两种日期参数 MUST 允许缺省。同一格式约定 MUST 对查询参数、参数对象属性绑定、JSON 请求体三种声明方式一致生效。

探针接口 SHALL 把绑定结果回显在 `data` 中，使解析结果可被外部观测；回显文本 MUST 与该值按序列化格式约定输出的结果一致。

#### Scenario: 符合格式的查询参数被绑定并回显

- **WHEN** 以 `2024-01-01` 提交 `LocalDate` 参数、以 `2024-01-01 12:30:45` 提交 `LocalDateTime` 参数
- **THEN** 请求被接受并返回成功信封
- **AND** `data` 中回显的两个值与提交的文本逐字相同

#### Scenario: 日期参数缺省

- **WHEN** 两个日期参数均不提交
- **THEN** 请求被接受并返回成功信封
- **AND** `data` 中对应的两个键仍存在且值为 `null`

#### Scenario: JSON 请求体中的日期字段

- **WHEN** 以 JSON 请求体提交 `localDate`、`localDateTime` 字段
- **THEN** 按同一格式约定解析成功，并在 `data` 中回显出与提交值逐字相同的文本

#### Scenario: 查询参数格式不符

- **WHEN** 以不符合 `yyyy-MM-dd` 的文本提交 `LocalDate` 查询参数
- **THEN** 请求不被接受，响应为 `success` 为 false、`code` 为 500 的失败信封
- **AND** HTTP 状态码为 500

#### Scenario: 请求体字段格式不符

- **WHEN** 以不符合 `yyyy-MM-dd` 的文本提交 JSON 请求体中的 `localDate` 字段
- **THEN** 请求不被接受，响应为 `success` 为 false、`code` 为 500 的失败信封
- **AND** HTTP 状态码为 500，而不是框架对不可读请求体默认的 400

### Requirement: 日期字段按固定格式序列化

响应体中的日期字段 SHALL 以字符串输出：`java.util.Date` 为 `yyyy-MM-dd HH:mm:ss`，`LocalDate` 为 `yyyy-MM-dd`，`LocalDateTime` 为 `yyyy-MM-dd HH:mm:ss`。三者 MUST NOT 输出为数字时间戳。

#### Scenario: 返回包含三种日期类型的响应

- **WHEN** 请求返回同时含 `Date`、`LocalDate`、`LocalDateTime` 字段的接口
- **THEN** `data` 中三个字段均为字符串
- **AND** `date` 与 `localDateTime` 匹配 `yyyy-MM-dd HH:mm:ss`，`localDate` 匹配 `yyyy-MM-dd`

### Requirement: 参数校验失败以 HTTP 200 返回失败信封

当请求体未通过声明式校验，系统 SHALL 返回 HTTP 200，响应体为 `code` 500、`success` false 的失败信封，`message` SHALL 由各字段错误按 `[字段名]校验提示` 的形式以分号连接构成。校验失败 MUST NOT 以未包装的框架异常信息返回。

#### Scenario: 多个必填字段缺失

- **WHEN** 提交的请求体中 `id` 为 null 且 `name` 为空串
- **THEN** HTTP 状态码为 200
- **AND** `code` 为 500，`success` 为 false
- **AND** `message` 同时包含 `[id]` 与 `[name]` 两个字段标记

#### Scenario: 单个字段失败时消息被截断

- **WHEN** 提交的请求体仅有 `age` 一个字段校验不通过
- **THEN** `message` 为 `[age]年龄不能为`
- **AND** 拼接结果末尾的分号与其前一个字符一并被删除，属已知缺陷，修复时本场景需同步更新

#### Scenario: 全部字段合法

- **WHEN** 提交的请求体满足全部校验规则
- **THEN** 返回成功信封
