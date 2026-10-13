# Spec Delta

## Purpose

约束多身份源（OAuth2 / OIDC）登录过程中，访问用户信息端点时对传输层证书的校验行为，以及该行为在配置缺省与配置缺失两种情况下应落到哪一侧。

## ADDED Requirements

### Requirement: 用户信息端点默认校验 TLS 证书

系统 SHALL 在向身份源的用户信息（userinfo）端点发起请求时校验服务端证书与主机名。跳过校验 MUST 只能由显式配置触发，MUST NOT 作为任何代码路径的默认行为。

#### Scenario: 未提供任何 SSL 相关配置

- **WHEN** 应用配置了身份源客户端，但未声明 `ignore-ssl`
- **THEN** 用户信息请求使用启用证书校验的 HTTP 客户端发出
- **AND** 服务端证书不可信或主机名不匹配时请求失败，登录流程以认证异常终止

#### Scenario: 显式要求跳过校验

- **WHEN** 某客户端配置了 `ignore-ssl: true`
- **THEN** 该客户端的用户信息请求跳过证书与主机名校验
- **AND** 系统在启动或首次使用时输出可检索的告警日志，指明哪个 `registration-id` 处于不校验状态

### Requirement: SSL 豁免按身份源独立生效

系统 SHALL 使 `ignore-ssl` 以单个客户端为粒度生效。同一应用内配置了多个身份源时，对其中一个豁免证书校验 MUST NOT 影响其余身份源的校验行为。

#### Scenario: 混合配置一安全一豁免

- **WHEN** 应用同时配置 `internal`（`ignore-ssl: true`）与 `github`（未配置 `ignore-ssl`）两个客户端
- **THEN** 访问 `internal` 的用户信息端点时跳过校验
- **AND** 访问 `github` 的用户信息端点时执行校验

#### Scenario: 令牌交换与端点发现不受豁免影响

- **WHEN** 某客户端配置了 `ignore-ssl: true`
- **THEN** 该豁免仅作用于用户信息端点
- **AND** OIDC 端点发现（issuer metadata 拉取）仍执行标准证书校验

### Requirement: 未匹配身份源回落安全实例

当用户信息请求携带的 `registration-id` 在已配置客户端中不存在时，系统 SHALL 使用启用证书校验的实例处理该请求，MUST NOT 回落到任何跳过校验的实例。

#### Scenario: 请求的注册 ID 未配置

- **WHEN** 用户信息请求的 `registration-id` 未出现在配置列表中
- **THEN** 该请求以启用证书校验的 HTTP 客户端发出
- **AND** 请求不因找不到配置而使用不校验证书的通道

### Requirement: 配置项语义与文档一致

`ignore-ssl` 的取值含义 SHALL 与其字段名一致，且其注释与随附文档 MUST NOT 出现与字段语义相反的指引。

#### Scenario: 阅读配置项说明

- **WHEN** 开发者查看 `ignore-ssl` 的说明以确定生产环境应填何值
- **THEN** 说明中指明的生产环境取值等于"执行证书校验"所对应的值
- **AND** 该指引与仓库内其他描述同一开关的文档不冲突
