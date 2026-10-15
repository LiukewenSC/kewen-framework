# OAuth2 / OIDC 单点登录入口一览

## 背景

- `login-spring-boot-starter` 通过 `Oauth2Config implements HttpSecurityCustomizer` 注册 `http.oauth2Login(...)`，OAuth2 与 OIDC **共用同一条过滤器链**：OAuth2 走授权码 → 令牌 → userinfo；OIDC 在此基础上多一步 ID Token 校验（并按需拉取 userinfo）。
- 配置前缀：`cu.security.login.oauth2`（对应 `Oauth2OidcProperties`）。
- 与 SAML2 使用独立的过滤器链与独立的 Repository，路径互不冲突。

## 端点/入口总表

| 用途 | HTTP 方法 | 路径 | 由谁处理 | 是否可配置 |
|---|---|---|---|---|
| **SP 发起授权（登录入口）** | GET | `{sso-authorization-uri}/{registrationId}`，默认 `/oauth2/authorization/{registrationId}` | `OAuth2AuthorizationRequestRedirectFilter` | `cu.security.login.oauth2.sso-authorization-uri` |
| IDP 回调（授权码换令牌 + 加载用户） | GET | `{login-processing-url}`，默认 `/login/oauth2/code/{registrationId}` | `OAuth2LoginAuthenticationFilter` | `cu.security.login.oauth2.login-processing-url`（Ant 通配） |
| 令牌交换（SP→IDP 出站） | POST | `clients[i].token-uri` 或 OIDC 自动发现 | `DefaultAuthorizationCodeTokenResponseClient` | 每个 client 单独配 |
| userinfo 拉取（SP→IDP 出站） | GET | `clients[i].user-info-uri` 或 OIDC 自动发现 | `oauth2UserService` / `oidcUserService` | 每个 client 单独配 |
| OIDC 端点自动发现（**启动阶段执行**） | GET | `{issuer-uri}/.well-known/openid-configuration` | `ClientRegistrations.fromIssuerLocation` | 由 `protocol-type: oidc` 触发 |
| 退出 | POST | `/logout` | Spring Security 默认 `LogoutFilter` | 本 starter 未覆盖 |
| OIDC RP-Initiated Logout | — | 由 `OidcClientInitiatedLogoutSuccessHandler` 跳转到 IDP 的 `end_session_endpoint` | 已声明为 Bean | ⚠️ 当前 **未** 在 `customizer()` 中接入 `http.logout()`，实际不会触发 |

## 与 Spring Security 默认的对齐说明

- 授权入口 `sso-authorization-uri` 默认值为 `/oauth2/authorization`，**与 Spring Security 官方 `OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI` 一致**。
- 回调 `login-processing-url` 默认值取自 `OAuth2LoginAuthenticationFilter.DEFAULT_FILTER_PROCESSES_URI = "/login/oauth2/code/*"`，同样对齐官方。
- `redirect-uri-template` 默认 `{baseUrl}/login/oauth2/code/{registrationId}`，需要与 `login-processing-url` 匹配，否则 IDP 会 302 到一个没有 filter 拦截的地址，表现是回调页 404 或直接命中 permitAll 白名单。

## 常见坑

1. **`Oauth2Config` 是无条件加载的**：只要引入了 `login-spring-boot-starter`，`cu.security.login.oauth2.clients` 必须至少一条，否则 `InMemoryClientRegistrationRepository` 空列表启动 NPE。
2. **`protocol-type: oidc` 在启动阶段就会访问网络**：`ClientRegistrations.fromIssuerLocation(issuerUri)` 同步拉取 `{issuer}/.well-known/openid-configuration`，IDP 不在线则应用起不来。本地 demo 想跳过网络请求就切回 `protocol-type: oauth2`（详见 `sample/login-boot-sample`）。
3. **`ignore-ssl=true` 只影响 userinfo**：OIDC Discovery 与 JWK Set 拉取走 Spring 自带 HTTP 客户端，始终执行标准证书校验。自签 IDP 在 Discovery 阶段报 SSL 错时，把证书导入 JDK cacerts，配 `ignore-ssl` 没用。
4. **OAuth2 与 OIDC 共用同一路径**：同一条 `clients` 记录要么 OAuth2 要么 OIDC，由 `protocol-type` 决定。想同一份 IDP 暴露两种模式，需要声明两条 clients（参考 `idaas-sp-boot-sample` 的 `cu-local-oidc-registration` 与 `cu-local-oauth-registration`）。
5. **registrationId 与 SAML2 允许同名**：两套 registration 存在各自的 Repository，路径前缀不同（`/oauth2/authorization/*` vs `/saml2/authenticate/*`），不会冲突；但排查日志时容易混淆，建议在命名上加 `-oauth` / `-saml` 后缀区分。
6. **`OidcClientInitiatedLogoutSuccessHandler` 已注册但未接入过滤器链**：目前 `/logout` 只会走本地 session 失效，不会真正通知 IDP。若需要联动退出，还要在 `http.logout().logoutSuccessHandler(oidcLogoutSuccessHandler())` 处显式绑定，或走 SAML2 那条 SLO 链路。

## 各 sample 中的实际入口

### `sample/login-boot-sample`（端口 8080）

配置：`clients[0].registration-id = demo-oauth2`，`protocol-type = oauth2`，端点占位指向 `http://localhost:9010/...`。

- 登录入口：http://localhost:8080/oauth2/authorization/demo-oauth2
- IDP 回调：http://localhost:8080/login/oauth2/code/demo-oauth2
- 退出（POST）：http://localhost:8080/logout

## 快速自测流程

1. 起本地 IDP（例如 Keycloak，端口 9010），把 `login-boot-sample` 的 `authorization-uri` / `token-uri` / `user-info-uri` 指向它。
2. 浏览器打开 http://localhost:8080/oauth2/authorization/demo-oauth2 ，正常应 302 到 IDP 登录页。
3. IDP 登录成功后 302 回 `/login/oauth2/code/demo-oauth2?code=...&state=...`，`OAuth2LoginAuthenticationFilter` 依次完成 CSRF 校验 → 换 token → 拉 userinfo → 走 `SecurityAuthenticationSuccessHandler`。
4. 想确认走的是 OIDC 分支还是 OAuth2 分支，看是否命中 `OidcUserService`：只有当响应里带回 `id_token` 时才会进入 OIDC 校验路径。

## 参考

- `login-spring-boot-starter/src/main/java/com/cu/framework/authentication/oauth2/Oauth2Config.java`
- `login-spring-boot-starter/src/main/java/com/cu/framework/authentication/oauth2/Oauth2OidcProperties.java`
- `docs/说明文档/oidc接入踩坑记录.md`
- `docs/说明文档/SAML2_SP_Metadata配置指南.md`