# OAuth2

本文说明 OAuth2 授权码模式在本框架里怎么落地：一次登录经过哪些端点、每个配置项控制哪一步、失败时先查哪里。OIDC 是 OAuth2 之上加身份层，单独见 [OIDC](oidc.md)。

## 协议在做什么

OAuth2 授权码模式解决的是“让用户在身份源完成登录，应用拿到一个可换用户信息的令牌”：

- 应用是客户端（SP），身份源是授权服务器（IdP）。
- 浏览器在 IdP 登录后带着授权码回到应用。
- 应用在服务端用授权码换 `access_token`，再拉 userinfo。
- 应用不把 IdP 的令牌当成本系统会话；它只把 userinfo 里的用户名映射到本地账号，然后建立自己的会话。

本框架只使用授权码模式；`authorization-grant-type` 当前 `[未接线]`，配了也不会改成其他模式。

## 一次登录的端点

| 步骤 | 方法 | 路径 | 由谁处理 |
| --- | --- | --- | --- |
| 发起授权 | GET | `/oauth2/authorization/{registrationId}` | `OAuth2AuthorizationRequestRedirectFilter` |
| IdP 登录 | — | IdP 授权端点 | IdP |
| 回调 | GET | `/login/oauth2/code/{registrationId}?code=&state=` | `OAuth2LoginAuthenticationFilter` |
| 换令牌（SP→IdP） | POST | `clients[i].token-uri` | `DefaultAuthorizationCodeTokenResponseClient` |
| 拉 userinfo（SP→IdP） | GET | `clients[i].user-info-uri` | `RoutingOAuth2UserService` |
| 建立本地会话 | — | — | `SecurityAuthenticationSuccessHandler` |
| 登出 | POST | `/logout` | Spring Security `LogoutFilter` |

`state` 存在会话里（`HttpSessionOAuth2AuthorizationRequestRepository`），回调时校验；因此浏览器在授权跳转期间不能丢掉会话 Cookie。

## 配置

前缀：`cu.security.login.oauth2`。

**全局**

| key | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- |
| `sso-authorization-uri` | `/oauth2/authorization` | 否 | 本地授权入口基址；不要写 `{registrationId}`，解析器会自动拼接 |
| `login-processing-url` | `/login/oauth2/code/*` | 否 | 回调匹配模板，Ant 通配 `*`，不是 `{registrationId}` |
| `clients` | — | 否 | 留空时不装配 OAuth2/OIDC；配置后至少一条 |

**每个 `clients[i]`**

| key | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- |
| `registration-id` | — | 是 | 用于 `/oauth2/authorization/{registrationId}` |
| `protocol-type` | `oauth2` | 否 | `oauth2` 或 `oidc`；OAuth2 用本页，OIDC 见 [OIDC](oidc.md) |
| `client-id` | — | 是 | IdP 分配 |
| `client-secret` | — | 是 | IdP 分配 |
| `client-authentication-method` | `client_secret_basic` | 否 | `client_secret_basic` / `client_secret_post` / `none` |
| `redirect-uri-template` | `{baseUrl}/login/oauth2/code/{registrationId}` | 否 | IdP 侧登记的回调地址必须是展开后的完整 URL |
| `scopes` | — | 建议 | 申请的权限范围；OAuth2 手动模式按配置原样使用 |
| `authorization-uri` | — | OAuth2 是 | IdP 授权端点 |
| `token-uri` | — | OAuth2 是 | IdP 令牌端点 |
| `user-info-uri` | — | OAuth2 是 | IdP userinfo 端点 |
| `user-name-attribute-name` | — | OAuth2 是 | userinfo 响应中指向**本系统用户名**的字段 |
| `jwk-set-uri` | — | 否 | OIDC 可选覆盖；OAuth2 手动模式不使用 |
| `client-name` | — | 否 | 登录页按钮文案；不配回退 `registration-id` |
| `ignore-ssl` | `false` | 否 | 仅豁免 userinfo 请求的证书校验 |
| `authorization-grant-type` | — | — | **[未接线]** 固定授权码模式 |
| `user-info-authentication-method` | — | — | **[未接线]** 实际请求方式不由本项控制 |

示例：

```yaml
cu:
  security:
    login:
      oauth2:
        clients:
          - registration-id: corp-oauth2
            protocol-type: oauth2
            client-id: your-client-id
            client-secret: your-client-secret
            client-authentication-method: client_secret_basic
            redirect-uri-template: "{baseUrl}/login/oauth2/code/{registrationId}"
            scopes:
              - read
            authorization-uri: https://idp.example.com/oauth2/authorize
            token-uri: https://idp.example.com/oauth2/token
            user-info-uri: https://idp.example.com/oauth2/userinfo
            user-name-attribute-name: preferred_username
            client-name: 企业 OAuth2
```

`clients` 是列表，每条一个身份源。同一 IdP 要同时暴露 OAuth2 与 OIDC 入口时声明两条，`protocol-type` 与 `registration-id` 都不同；OAuth2 与 SAML 允许使用同名 `registration-id`（两套注册仓库、路径前缀不同），但排查日志时建议加 `-oauth2` / `-saml` 后缀区分。

## 用户名怎么映射到本地账号

`userinfo` 响应里的 `user-name-attribute-name` 字段值就是本系统用户名。框架用它调用 `SecurityUserDetailsService.loadUserByUsername(...)`：

- 取到本地用户：用本地用户与权限构建 `SecurityUser`，身份源声明只作为协议数据保留，不参与授权判定。
- 取不到：以 `sso_local_user_not_found` 结束本次认证，不创建会话。

常见错误是把 `sub` 配成用户名。`sub` 通常是身份源自己的数字 ID，与本系统用户名不是一回事；OAuth2 手动模式必须显式配 `user-name-attribute-name`。

## 失败时先查哪里

1. **`clients` 为空**：OAuth2/OIDC 都没装配，`/oauth2/authorization/...` 不存在。
2. **不需要显式配置 `authorizationRequestResolver`**：默认解析器已经按 `sso-authorization-uri` 拼 `registrationId`；只有要改请求参数或 state 生成时才需要自定义。
3. **302 到 IdP 后报 `invalid_redirect_uri`**：IdP 侧登记的回调地址与 `redirect-uri-template` 展开值不一致。
4. **回调 404 或落到白名单**：`login-processing-url` 与 `redirect-uri-template` 不匹配；回调模板要落在 `/login/oauth2/code/*` 内。
5. **userinfo 解析失败**：确认 `user-info-uri` 返回 JSON，且 `user-name-attribute-name` 在响应里存在。
6. **SSL 证书错误**：`ignore-ssl=true` 只影响 userinfo；IdP 自签证书在 token 交换阶段仍会失败，需要把证书导入 JDK cacerts。
7. **`state` 校验失败**：授权跳转期间会话丢失，或前后端跨站导致 Cookie 没带上。

## 安全注意

- 只使用授权码模式；不要把 `client-secret` 放到前端。
- `state` 必须保留；不要自定义授权解析器绕过它。
- `scopes` 按最小权限申请；OAuth2 拿到的 scope 不是本系统权限。
- `ignore-ssl` 只用于本地测试，不要带到生产。
- 会话 Cookie / `Authorization` 头是登录态载体；HTTPS 部署并设置合适的 SameSite 策略。
- 日志里不要打印 `client-secret`、`access_token` 与 userinfo 原文。

## 相关文档

- [单点登录总览](sso-overview.md)
- [OIDC](oidc.md)
- [SAML2](saml2.md)
- [login-security-spring-boot-starter](../README.md)
- [全量配置参考](../../../docs/application.yml)
