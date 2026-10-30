# Spec Delta

## MODIFIED Requirements

### Requirement: 统一响应信封

接口成功返回 SHALL 为包含 `code`、`success`、`message`、`data`、`traceId` 五个字段的 JSON 对象；失败返回 SHALL 在此基础上额外包含 `errorType` 字段，成功返回 MUST NOT 包含该字段。业务数据为空时 `data` MUST 以 `null` 出现，而不是被省略。

#### Scenario: 无业务数据的请求处理成功

- **WHEN** 请求不返回业务数据的探针接口且处理成功
- **THEN** `code` 为 200，`success` 为 true，`message` 为"成功"
- **AND** `data` 字段存在且值为 `null`
- **AND** 响应体不出现 `errorType` 字段

#### Scenario: 请求处理失败仍返回同一信封

- **WHEN** 请求因参数校验、类型转换或请求体解析失败
- **THEN** HTTP 状态码为 400，`success` 为 false，`code` 为 400，`errorType` 为 `VALIDATION`
- **AND** 响应体仍可解析为同一信封结构，不返回未包装的异常信息

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
- **THEN** 请求不被接受，响应为 `success` 为 false、`code` 为 400、`errorType` 为 `VALIDATION` 的失败信封
- **AND** HTTP 状态码为 400

#### Scenario: 请求体字段格式不符

- **WHEN** 以不符合 `yyyy-MM-dd` 的文本提交 JSON 请求体中的 `localDate` 字段
- **THEN** 请求不被接受，响应为 `success` 为 false、`code` 为 400、`errorType` 为 `VALIDATION` 的失败信封
- **AND** HTTP 状态码为 400

## ADDED Requirements

### Requirement: 参数校验失败以 HTTP 400 返回失败信封

当请求体未通过声明式校验，系统 SHALL 返回 HTTP 400，响应体为 `code` 400、`errorType` 为 `VALIDATION`、`success` false 的失败信封。`message` SHALL 由各字段错误按 `[字段名]校验提示` 的形式以分号连接构成，并 MUST 完整保留每个字段的校验提示，不得截断末尾字符。校验失败 MUST NOT 以未包装的框架异常信息返回。

#### Scenario: 多个必填字段缺失

- **WHEN** 提交的请求体中 `id` 为 null 且 `name` 为空串
- **THEN** HTTP 状态码为 400
- **AND** `code` 为 400，`success` 为 false，`errorType` 为 `VALIDATION`
- **AND** `message` 同时包含 `[id]` 与 `[name]` 两个字段标记

#### Scenario: 单个字段失败时消息完整

- **WHEN** 提交的请求体仅有 `age` 一个字段校验不通过
- **THEN** `message` 为 `[age]年龄不能为空`
- **AND** 校验提示的末尾字符不被删除

#### Scenario: 全部字段合法

- **WHEN** 提交的请求体满足全部校验规则
- **THEN** 返回成功信封

## REMOVED Requirements

### Requirement: 参数校验失败以 HTTP 200 返回失败信封

**Reason**: 校验失败属于调用方输入错误，HTTP 200 与 `code` 500 既掩盖了失败性质，也与业务失败、数据库失败和系统故障混在一起；本变更将校验失败统一为 HTTP 400 + `code` 400 + `errorType` `VALIDATION`，同时修复消息末尾被截断的缺陷。

**Migration**: 依赖 HTTP 200 或 `code` 500 判断校验失败的调用方改为判断 `errorType` 为 `VALIDATION` 或 `code` 为 400；按已有 `message` 展示提示的调用方无需改动，但会看到修复后的完整提示文本。
