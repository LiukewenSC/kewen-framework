# OIDC

OIDC 是 OAuth2 之上的身份层：仍然走授权码流程，但多了 `openid` scope、`id_token`（JWT）与标准的身份声明。本文说明本框架怎样接 OIDC、哪些配置在启动期就会访问网络、以及常见错误怎么定位。OAuth2 的通用流程见 [OAuth2](oauth2.md)。

## OIDC 比 OAuth2 多了什么

- 授权请求必须带 `openid` scope；框架在构建 OIDC 客户端时会自动补。
- 回调换到的响应里有 `id_token`，它是 JWT，用于证明“这次登录是谁”。
- `id_token` 要验签（JWKS）、校验 `issuer`、`audience`、`nonce` 与有效期，并检查它与 userinfo 的 `sub` 一致。
- userinfo 可选：是否拉取取决于 scopes 与 userinfo 端点配置；即使不拉 userinfo，ID Token 也足以建立身份。

本框架的 OIDC 用户服务继承 Spring Security 的 `OidcUserService`，上述校验沿用父类；它只在链路末端把协议主体换成本框架的 `SecurityUser`。

## 端点与启动期行为

| 用途 | 路径/地址 | 何时发生 |
| --- | --- | --- |
| 发起授权 | `/oauth2/authorization/{registrationId}` | 浏览器访问 |
| 回调 | `/login/oauth2/code/{registrationId}` | IdP 重定向回来 |
| OIDC Discovery | `{issuer-uri}/.well-known/openid-configuration` | **应用启动时** |
| JWKS | discovery 给出的 `jwks_uri`，或 `jwk-set-uri` 覆盖 | 校验 ID Token 时 |
| userinfo | discovery 给出的 `userinfo_endpoint`，或 `user-info-uri` 覆盖 | 登录时按需 |

**启动期网络依赖**：`protocol-type: oidc` 的客户端在构建 `ClientRegistration` 时同步调用 `ClientRegistrations.fromIssuerLocation(issuerUri)`。身份源不在线，整个应用起不来；这不是登录时才失败。本地演示想避免网络请求，改用 `protocol-type: oauth2` 并手动配端点。

## 配置

前缀与 OAuth2 共用：`cu.security.login.oauth2`。OIDC 客户端把 `protocol-type` 设为 `oidc`。

**OIDC 专用/关键字段**

| key | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- |
| `protocol-type` | `oauth2` | 是 | 必须显式写 `oidc` |
| `issuer-uri` | — | 是 | OIDC 的 issuer 基址；启动时拉取 discovery |
| `scopes` | — | 建议 | 框架自动补 `openid`；可另加 `profile`、`email` 等 |
| `user-name-attribute-name` | 回退 `sub` | 否 | 指向本系统用户名的声明；不配时用 `sub` |
| `jwk-set-uri` | discovery 值 | 否 | 手动覆盖 JWKS 地址 |
| `authorization-uri` / `token-uri` / `user-info-uri` | discovery 值 | 否 | 手动覆盖自动发现结果 |
| `client-authentication-method` | `client_secret_basic` | 否 | `client_secret_basic` / `client_secret_post` / `none` |
| `ignore-ssl` | `false` | 否 | **只影响 userinfo**；不影响 discovery 与 JWKS |
| `client-name` | 回退 `registration-id` | 否 | 登录页按钮文案 |

示例：

```yaml
cu:
  security:
    login:
      oauth2:
        clients:
          - registration-id: corp-oidc
            protocol-type: oidc
            issuer-uri: https://idp.example.com
            client-id: your-client-id
            client-secret: your-client-secret
            client-authentication-method: client_secret_basic
            redirect-uri-template: "{baseUrl}/login/oauth2/code/{registrationId}"
            scopes:
              - openid
              - profile
            user-name-attribute-name: preferred_username
            client-name: 企业 OIDC
```

## 用户名映射

- 配了 `user-name-attribute-name`：用该声明的值作为本系统用户名。
- 没配：主体的 name 取 `sub`，通常对不上本系统用户名，登录会以 `sso_local_user_not_found` 失败。
- 取不到本地账号时不会留下无权限会话；这是身份映射错误，不是权限拒绝。

## 排错清单

1. **应用启动就报连接失败 / 404**
   检查 `issuer-uri` 是否可从本机访问，以及 `/.well-known/openid-configuration` 是否返回标准 JSON。OIDC 在启动期就会请求它。

2. **`Could not extract response: no suitable HttpMessageConverter found ... content type [text/html]`**
   不是缺少 converter，而是 userinfo 端点返回了 HTML（常见于未认证、地址错、被重定向到登录页）。先用 curl 带上 token 直接请求 userinfo，确认返回 JSON。

3. **`BadJwtException: Signed JWT rejected`**
   先看 JWKS 拉到的公钥是否正确：`jwk-set-uri` 或 discovery 给出的地址是否带必需参数、是否返回了完整 `keys`。不要在配置里写一个需要额外查询参数的地址。

4. **`invalid_nonce`**
   `nonce` 存在会话里，回调时校验。常见原因是授权跳转期间会话丢失、多个标签页互相覆盖、IdP 重放或时钟偏差。确认 Cookie 在跳转期间带上，并检查 IdP 与应用时间。

5. **`ignore-ssl=true` 仍然 SSL 失败**
   它只给 userinfo 请求换 HTTP 客户端。Discovery 与 JWKS 使用 Spring 自带客户端，仍会校验证书；自签 IdP 需要把证书导入 JDK cacerts。

6. **登录成功但取当前用户失败**
   检查 `user-name-attribute-name` 是否指向本系统用户名；再看会话主体是不是 `SecurityUser`。升级前留下的旧会话可能不是统一主体，重新登录即可。

## 安全注意

- `nonce`、`state` 与 `id_token` 校验必须保留；不要为了兼容非标准 IdP 关闭这些校验。
- `issuer`、`audience`、`sub` 一致性是 OIDC 的身份边界；不要只解码 ID Token 不验签。
- 身份源声明不参与本系统授权；权限只来自本地 `SecurityUserDetailsService` 装载的结果。
- OIDC RP-Initiated Logout 当前未接入过滤器链，`/logout` 只失效本地会话，不会通知 IdP。
- 会话里会存 ID Token 与声明；不要把会话表导出或打印。

## 相关文档

- [单点登录总览](sso-overview.md)
- [OAuth2](oauth2.md)
- [SAML2](saml2.md)
- [login-security-spring-boot-starter](../README.md)
- [全量配置参考](../../../docs/application.yml)
