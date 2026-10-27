# SAML2

本文说明 SAML2 单点登录在本框架里的落地方式：SP 发起流程、metadata 解析、签名、Audience、SLO，以及 SP Metadata 端点怎样暴露给 IdP。OAuth2/OIDC 见 [OAuth2](oauth2.md) 与 [OIDC](oidc.md)。

## 协议在做什么

SAML2 用“断言（Assertion）”表达“IdP 已经认证了某个人”。SP 发起流程：

1. SP 生成 `AuthnRequest`，把浏览器送到 IdP 的 SSO 端点。
2. 用户在 IdP 登录。
3. IdP 生成 `SAMLResponse`（内含签名断言），让浏览器 POST 回 SP 的 ACS（Assertion Consumer Service）。
4. SP 校验签名、Audience、时间与 InResponseTo，再按 NameID 映射本地账号。

本框架只文档化 SP 发起流程。代码没有为 IdP 发起提供入口或示例，不承诺该模式可用。

```
Browser              SP (this app)                 IdP
   |                      |                          |
   | GET /saml2/authenticate/{id}                    |
   |--------------------->|                          |
   | 200 auto-submit POST form (SAMLRequest)         |
   |<---------------------|                          |
   | POST SAMLRequest to IdP SSO URL                 |
   |----------------------------------------------->|
   | login                                           |
   |<-----------------------------------------------|
   | auto-submit POST SAMLResponse to ACS            |
   |--------------------->|                          |
   |                      | verify signature/Audience|
   | JSON or 302 result-url?loginState=success|failure|
   |<---------------------|                          |
```

## 端点

| 用途 | 方法 | 默认路径 | 由谁处理 |
| --- | --- | --- | --- |
| 发起登录 | GET | `/saml2/authenticate/{registrationId}` | `Saml2WebSsoAuthenticationRequestFilter` |
| ACS 回调 | POST | `/login/saml2/sso/{registrationId}` | `Saml2WebSsoAuthenticationFilter` |
| SP Metadata | GET | `/saml2/service-provider-metadata/{registrationId}` | `Saml2MetadataFilter`（本 starter 手动挂载） |
| GET 退出 | GET | `/saml2/logout` | `SamlLogoutController` 返回自动提交的 POST 表单 |
| 实际登出 | POST | `/logout` | Spring Security `LogoutFilter` + SAML SLO |
| SLO 请求/响应 | POST | `/logout/saml2/slo` | `Saml2LogoutRequestFilter` / `Saml2LogoutResponseFilter` |

`/saml2/authenticate/{registrationId}` 是 Spring Security 固定入口，本 starter 未做成配置项；要自定义入口只能另建 Controller 重定向。

## 配置

前缀：`cu.security.login.saml`。

**全局**

| key | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- |
| `login-processing-url` | `/login/saml2/sso/{registrationId}` | 否 | ACS 路径模板；**必须包含 `{registrationId}`**；不要写 `{baseUrl}`，构建时会前置 |
| `logout-request-url` | `/logout/saml2/slo` | 否 | SP 侧 LogoutRequest 接收地址 |
| `logout-response-url` | `/logout/saml2/slo` | 否 | SP 侧 LogoutResponse 接收地址 |
| `registrations` | — | 否 | 留空时不装配 SAML；配置后至少一条 |
| `sp-signing-keystore` | — | 否 | 不配则 AuthnRequest / LogoutRequest 不签名 |
| `login-page-url` | — | — | **[已废弃]** 未实现 |

**每个 `registrations[i]`**

| key | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- |
| `registration-id` | 回退 `sp-entity-id` | 建议 | 用于 `/saml2/authenticate/{registrationId}` 与 ACS |
| `sp-entity-id` | `cu-saml` | 是 | SP Entity ID；必须与 IdP 侧配置的 SP entityId / Audience 一致 |
| `metadata-resource` | `classpath:saml/idp-metadata.xml` | 是 | IdP metadata 文件；解析 entityId、SSO URL、验签证书、SLO |
| `display-name` | 回退 `registration-id` | 否 | 登录页按钮文案，不参与协议字段 |

**`sp-signing-keystore`**

| key | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- |
| `keystore-resource` | — | 否 | 例 `classpath:saml/sp-keystore.jks`；未配则不签名 |
| `keystore-password` | `changeit` | 否 | JKS 密码 |
| `key-alias` | `sp-signing` | 否 | 密钥别名 |
| `key-password` | `changeit` | 否 | 密钥密码 |

示例：

```yaml
cu:
  security:
    login:
      saml:
        login-processing-url: /login/saml2/sso/{registrationId}
        logout-request-url: /logout/saml2/slo
        logout-response-url: /logout/saml2/slo
        registrations:
          - registration-id: corp-saml
            sp-entity-id: cu-framework-local
            metadata-resource: "classpath:saml/idp-metadata.xml"
            display-name: 企业 SAML
        sp-signing-keystore:
          keystore-resource:            # 留空 → 不签名
          keystore-password: changeit
          key-alias: sp-signing
          key-password: changeit
```

`registrations` 是列表，每条对应一个 IdP；每个 IdP 一份 metadata 文件。metadata 从 IdP 管理员处获取，或从 IdP 的 metadata 端点下载（常见路径 `/saml/metadata`）；注意随文件下发的签名证书有有效期，过期前要更新并重启。

## IdP metadata 的硬性要求

`IdpMetadataParser` 只从 metadata 构建注册，当前不支持手动填写 entityId / SSO URL / 证书。metadata 必须满足：

- 根元素是 `EntityDescriptor`，并含 `IDPSSODescriptor`。
- 至少一个 `SingleSignOnService`；优先 HTTP-POST，其次 HTTP-Redirect。
- 含签名证书（`KeyDescriptor use="signing"` 下的 X.509）。
- `WantAuthnRequestsSigned` **必须显式写值**：解析器把它拆箱成 `boolean`，缺省会 NPE。
- `SingleLogoutService` 可选；没有则不注册 SLO。

metadata 在启动时加载；更新 metadata 后需要重启应用。

## SP Metadata 端点

Spring Security 5.7 默认不暴露 SP metadata；本 starter 在 `SamlConfig` 里手动挂载 `Saml2MetadataFilter`：

```
GET /saml2/service-provider-metadata/{registrationId}
```

返回的 XML 包含 SP 的 Entity ID、ACS、SLO 与签名证书（配置了 keystore 时）。把这段 XML 交给 IdP 管理员，或在 IdP 侧按同样字段登记。

配置了 keystore 时：

```bash
keytool -genkeypair -alias sp-signing -keyalg RSA -keysize 2048 \
  -validity 3650 -keystore sp-keystore.jks \
  -storepass changeit -keypass changeit

keytool -exportcert -alias sp-signing -keystore sp-keystore.jks \
  -storepass changeit -file sp-certificate.crt
```

把 `sp-certificate.crt` 提供给 IdP，用于验证 SP 的签名。

## 签名与 Audience

- IdP metadata 的 `WantAuthnRequestsSigned=true` 时，SP 必须配置 `sp-signing-keystore`，否则 IdP 会拒绝未签名的 AuthnRequest。
- `sp-entity-id` 必须与 IdP 侧给本 SP 配置的 entityId / Audience 完全一致；不一致会在 `AudienceRestriction` 校验失败。
- 认证 Provider 使用 `OpenSamlAuthenticationProvider`，不是 `OpenSaml4AuthenticationProvider`：后者要求 JDK 11+，本框架保持 JDK 8 兼容。

## SLO

- `logout-request-url` 与 `logout-response-url` 默认都是 `/logout/saml2/slo`，可以分开配。
- `Saml2LogoutResponseValidator.validateDestination` 使用全匹配；反向代理场景要确保 `{baseUrl}` 解析出的 scheme/host/port 与 IdP 侧登记的 destination 完全一致。
- 浏览器直接访问 `GET /saml2/logout` 时，Controller 返回自动提交的 POST 表单，再由 `POST /logout` 触发 SLO。
- IdP 侧没有 `SingleLogoutService` 时，只失效本地会话，不会通知 IdP。

## 常见失败

1. **`registrations` 为空**：SAML 不装配，`/saml2/authenticate/...` 不存在。
2. **启动 NPE**：metadata 里 `WantAuthnRequestsSigned` 没写值。
3. **Audience 不匹配**：`sp-entity-id` 与 IdP 配置不一致。
4. **IdP 拒绝 AuthnRequest**：`WantAuthnRequestsSigned=true` 但没配 keystore。
5. **SLO destination 不匹配**：反向代理的 host/scheme/port 与 IdP 登记值不一致。
6. **`login-page-url` 配了不生效**：该字段已废弃且未实现；自定义入口请另建 Controller。

## 安全注意

- 保留签名、Audience、时间与 InResponseTo 校验；不要为了兼容非标准 IdP 关闭。
- metadata 与签名证书要从可信渠道获取；证书过期前更新并重启。
- HTTPS 部署；SLO 的 destination 使用外部可达地址。
- 断言里的属性不参与本系统授权；权限来自本地 `SecurityUserDetailsService`。

## 相关文档

- [单点登录总览](sso-overview.md)
- [OAuth2](oauth2.md)
- [OIDC](oidc.md)
- [login-security-spring-boot-starter](../README.md)
- [login 示例](../../../sample/login-boot-sample/README.md)
- [全量配置参考](../../../docs/application.yml)
