# Spec Delta

## Purpose

约束框架所有异常出口共用的错误分类规则：每类失败在响应中携带唯一分类标记，并固定对应 HTTP 状态、业务码与日志级别，使调用方与服务端排查者无需解析消息文本就能区分认证、授权、参数、业务、数据库与系统故障。

## ADDED Requirements

### Requirement: 失败响应携带唯一错误分类

系统 SHALL 在每一个失败响应的统一信封中携带 `errorType` 字段，取值限定为 `AUTHENTICATION`、`AUTHORIZATION`、`VALIDATION`、`BUSINESS`、`USAGE`、`DATABASE`、`SYSTEM` 之一。一次失败 MUST 只归入一个分类。成功响应 MUST NOT 携带该字段，既有成功信封保持不变。

#### Scenario: 失败信封带分类标记

- **WHEN** 任意请求以失败结果结束
- **THEN** 响应体包含 `success` 为 false 与上述七个取值之一的 `errorType`
- **AND** `errorType` 与本次失败的成因一致

#### Scenario: 成功响应不带分类标记

- **WHEN** 请求处理成功
- **THEN** 响应体不出现 `errorType` 字段
- **AND** 其余成功字段与变更前一致

### Requirement: 分类与 HTTP 状态及业务码固定对应

系统 SHALL 按固定映射输出错误分类：`AUTHENTICATION` 对应 HTTP 401 与 `code` 401；`AUTHORIZATION` 对应 HTTP 403 与 `code` 403；`VALIDATION`、`BUSINESS`、`USAGE` 对应 HTTP 400 与 `code` 400；`DATABASE`、`SYSTEM` 对应 HTTP 500 与 `code` 500。HTTP 状态与响应体 `code` MUST 一致。

#### Scenario: 未认证或凭据错误

- **WHEN** 未登录访问受保护资源，或提交错误的账号密码
- **THEN** HTTP 状态为 401，`code` 为 401，`errorType` 为 `AUTHENTICATION`

#### Scenario: 已认证但无权限

- **WHEN** 已登录用户访问其未被授予的端点或数据
- **THEN** HTTP 状态为 403，`code` 为 403，`errorType` 为 `AUTHORIZATION`

#### Scenario: 参数校验失败

- **WHEN** 请求体或参数未通过校验、无法解析、类型不匹配
- **THEN** HTTP 状态为 400，`code` 为 400，`errorType` 为 `VALIDATION`

#### Scenario: 业务规则失败

- **WHEN** 业务代码以业务失败结束本次请求
- **THEN** HTTP 状态为 400，`code` 为 400，`errorType` 为 `BUSINESS`

#### Scenario: 调用方式错误

- **WHEN** 权限框架无法判定本次调用，例如未提供业务数据 ID 或 ID 数量超出上限
- **THEN** HTTP 状态为 400，`code` 为 400，`errorType` 为 `USAGE`

#### Scenario: 数据库访问失败

- **WHEN** 请求处理中发生数据库访问失败
- **THEN** HTTP 状态为 500，`code` 为 500，`errorType` 为 `DATABASE`

#### Scenario: 未预期异常

- **WHEN** 请求处理中抛出未被归类为上述任何一类的异常
- **THEN** HTTP 状态为 500，`code` 为 500，`errorType` 为 `SYSTEM`

### Requirement: 异常按成因唯一归属

系统 SHALL 按以下规则归属异常，且同一异常 MUST NOT 在不同入口落入不同分类：认证过程失败（未登录、凭据错误、账号被禁用或锁定、会话失效、认证信息不足）归入 `AUTHENTICATION`；已认证后的权限拒绝归入 `AUTHORIZATION`；请求参数校验、绑定、解析与类型转换失败归入 `VALIDATION`；业务异常归入 `BUSINESS`；权限框架的调用方式错误归入 `USAGE`；数据库访问异常归入 `DATABASE`；空指针与其余未处理异常归入 `SYSTEM`。

#### Scenario: 认证失败不被归为授权失败

- **WHEN** 匿名请求访问受保护资源，或登录时凭据错误
- **THEN** `errorType` 为 `AUTHENTICATION`
- **AND** 提示文案不出现“权限不足”一类表述

#### Scenario: 登录过程中的数据库失败仍归数据库

- **WHEN** 登录时查询用户数据发生数据库访问失败，且该异常被认证流程包装
- **THEN** `errorType` 为 `DATABASE`，HTTP 状态为 500
- **AND** 不因为异常外层属于认证类型而改成 `AUTHENTICATION`

#### Scenario: 空指针不再按业务失败处理

- **WHEN** 请求处理中出现空指针异常
- **THEN** `errorType` 为 `SYSTEM`，HTTP 状态为 500
- **AND** 不返回 `BUSINESS` 分类

### Requirement: 认证相关失败语义可区分

系统 SHALL 为不同认证失败给出可区分的提示语义：未登录或会话失效 MUST 提示“未登录或登录状态已失效，请重新登录”；登录凭据错误 MUST 统一提示“用户名或密码错误”，不得因用户名是否存在而改变；账号被禁用、被锁定或过期 MUST 给出对应状态提示；已认证但无权限 MUST 提示“没有访问该资源的权限”。系统 MUST NOT 把未登录或凭据错误描述成权限不足。

#### Scenario: 未登录的提示不再写成权限不足

- **WHEN** 未登录用户访问受保护资源
- **THEN** 提示为“未登录或登录状态已失效，请重新登录”
- **AND** 提示中不出现“权限不足”

#### Scenario: 凭据错误不暴露账号是否存在

- **WHEN** 分别以不存在的用户名和错误的密码发起登录
- **THEN** 两种情况的响应均为 HTTP 401、`errorType` 为 `AUTHENTICATION`、提示为“用户名或密码错误”

#### Scenario: 无权限与未登录可区分

- **WHEN** 已登录用户访问其未被授予的端点
- **THEN** 响应的 `errorType` 为 `AUTHORIZATION`，提示为“没有访问该资源的权限”

#### Scenario: 账号状态异常有独立提示

- **WHEN** 登录账号处于禁用、锁定或过期状态
- **THEN** 响应为 HTTP 401、`errorType` 为 `AUTHENTICATION`
- **AND** 提示分别说明账号被禁用、被锁定或已过期

### Requirement: 日志按分类分级且可定位

系统 SHALL 为每次被处理的失败输出且只输出一条分类日志，内容包含分类标记、HTTP 方法、请求路径与失败摘要。未登录或会话失效 SHALL 以 INFO 记录；凭据错误、账号状态异常、权限拒绝、参数校验失败、业务失败与调用方式错误 SHALL 以 WARN 记录；数据库与系统故障 SHALL 以 ERROR 记录。INFO 与 WARN 记录 MUST NOT 打印异常堆栈；ERROR 记录 MUST 保留完整堆栈。日志 MUST NOT 包含密码、令牌或其他凭据。

#### Scenario: 匿名访问只留一行可辨认日志

- **WHEN** 未登录用户访问受保护资源
- **THEN** 服务端只新增一条带 `[AUTHENTICATION]` 标记的 INFO 日志，包含请求方法与路径
- **AND** 不产生 ERROR 级堆栈日志

#### Scenario: 数据库故障保留完整堆栈

- **WHEN** 请求处理因数据库访问失败结束
- **THEN** 服务端产生带 `[DATABASE]` 标记的 ERROR 日志并保留完整堆栈
- **AND** 响应体只包含通用提示

#### Scenario: 日志不记录凭据

- **WHEN** 用户提交错误的密码
- **THEN** 本次失败日志不包含所提交的密码或会话令牌

### Requirement: 所有异常出口输出统一信封

系统 SHALL 使所有异常出口——包括异常 advice、会话过期的直接响应、以及安全过滤器中无解析器可用的兜底出口——输出与统一响应一致的 JSON 信封并携带 `errorType`。系统 MUST NOT 向调用方输出纯文本错误页或未包装的异常原文。单点登录失败在已配置回跳目标时仍按重定向交付，不受本要求影响。

#### Scenario: 无解析器兜底仍返回统一信封

- **WHEN** 安全异常没有可用的异常解析器
- **THEN** 响应为 JSON 失败信封，`errorType` 为 `SYSTEM`，HTTP 状态为 500
- **AND** 异常原文只写入服务端日志

#### Scenario: 会话过期响应带认证分类

- **WHEN** 会话因过期或并发登录控制而失效
- **THEN** 响应为 HTTP 401 的 JSON 失败信封，`errorType` 为 `AUTHENTICATION`
- **AND** 提示为“登录状态已失效，请重新登录”

#### Scenario: 单点登录失败仍重定向

- **WHEN** 单点登录回调失败且应用配置了回跳目标
- **THEN** 响应为携带失败标记的重定向
- **AND** 不写出 JSON 响应体
