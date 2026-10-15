# 必要实现

必须要实现 `SecurityUserDetailsService` 否则找不到实现类会报错，这个也是加载用户的核心逻辑；
账号密码登陆强依赖这个接口，但IDP的单点登陆不依赖；

# 账密登陆说明

## 与 Spring Security 默认的差异

| 项 | Spring Security 默认 | 本 starter 现状 | 说明 |
|---|---|---|---|
| 登录 URL | `/login` POST（formLogin 内建） | `/login` POST（Ant + `JsonLoginFilter`） | 路径一致，但处理过滤器不同 |
| 登录页 | `formLogin` 会自动生成 `/login` GET 页 | **禁用**，配置 `loginPage` 会直接启动失败 | 前端分离场景 |
| CSRF | 默认启用 | `SecurityConfig` 里 `.csrf().disable()` | 走 session/cookie 或 JWT，前端不再需要带 CSRF token |
| Content-Type | 只支持 form | 优先 JSON，非 JSON 回退 form | 由 `JsonLoginFilter` 判断 |
| 用户来源 | 需要业务实现 `UserDetailsService` | 强制注入 `SecurityUserDetailsService`（`SecurityConfig.configure(AuthenticationManagerBuilder)`） | 若业务侧未提供实现，启动即失败 |
| 密码编码器 | `DelegatingPasswordEncoder` | 同（`SecurityBeanConfig` 注册，`@ConditionalOnMissingBean`） | 数据库里的 hash 需带 `{bcrypt}` 等 id 前缀；不带前缀的老数据需业务侧覆盖 Bean |
| 成功返回 | 默认 302 | 输出 JSON | 由 `JsonAuthenticationSuccessHandler` 承担 |

## 常见坑

1. **`/login` GET 会被拦截返回 401**：本 starter 没启用 `formLogin`，也就没有默认登录页。想放行登录页请通过 `UrlSecurityCustomizer.permitAll()` 或 `cu.auth.permit-url` 显式加入。
2. **`Content-Type` 必须精确匹配**：`JsonLoginFilter` 判断的是 `equals`，不是 `startsWith`。`application/json;charset=utf-8`（小写 utf-8）不会走 JSON 分支，而是走父类 form 分支，最终读不到 body 里的字段导致 `BadCredentials`。前端调用时用 `MediaType.APPLICATION_JSON_UTF8_VALUE`（`application/json;charset=UTF-8`）或干脆只写 `application/json`。
3. **`DelegatingPasswordEncoder` 对历史密码不友好**：数据库里存的是裸 bcrypt/MD5 摘要（没有 `{id}` 前缀）时会报 "There is no PasswordEncoder mapped for the id \"null\""。要么把数据补上前缀，要么在业务侧注册一个 `PasswordEncoder` Bean 覆盖默认。
4. **失败响应依赖 `HandlerExceptionResolver`**：容器里若一个都扫不到（例如没有 `@ControllerAdvice`），会走硬编码兜底字符串。生产项目建议至少提供一个统一异常处理类，返回结构化的错误码。
5. **`loginUrl` 不要与 `permit-url` 冲突**：`/login` 由 `JsonLoginAuthenticationFilterConfigurer` 自动 permitAll，业务侧再手写一次无害但容易误导。改自定义 `login-url` 时留意本 starter 的自动放行是按配置值注册的，不需要额外放行。
6. **remember-me / 并发控制不在这里配**：见 `SessionConfig` 和 `cu.security.remember-me.*` / `cu.security.session.*`，与 password 模块解耦；JSON 登录只会走 `setDetails(request, authenticationToken)`，是否记住我由具体 `RememberMeServices` 判断（cookie 里是否有 remember-me 参数）。

## 各 sample 中的实际入口

### `sample/login-boot-sample`（端口 8080）

配置：
```yaml
cu.security.login.password:
  login-url: /login
  username-parameter: username
  password-parameter: password

```

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


## SAML SSO 入口一览

`SamlConfig.customizer()` 里通过 `http.saml2Login()` 注册的过滤器链沿用了 Spring Security 5.3 的默认路径，只有 **ACS 回调地址** 被 `login-processing-url` 覆盖，其他都是固定的。

| 用途 | HTTP 方法 | 路径 | 说明 |
|---|---|---|---|
| **SP 发起登录（入口）** | GET | `/saml2/authenticate/{registrationId}` | Spring Security 默认，**你在找的就是这个**，浏览器访问后会 302 跳到 IdP 的 SSO URL |
| ACS 登录回调 | POST | `{login-processing-url}` 展开后的地址 | IdP 把 SAMLResponse POST 回来的地址，可在 `cu.security.login.saml.login-processing-url` 配置 |
| SP 元数据 | GET | `/saml2/service-provider-metadata/{registrationId}` | 由 `Saml2MetadataFilter` 提供，给 IdP 侧导入用 |
| GET 触发退出 | GET | `/saml2/logout` | `SamlLogoutController` 提供的辅助页，返回自动提交的 POST 表单 |
| 实际登出 | POST | `/logout` | Spring Security 标准登出，会串起 SAML2 SLO |
| SLO 请求/响应 | POST | `/logout/saml2/slo` | 由 `logout-request-url` / `logout-response-url` 配置 |


`sample/login-boot-sample`（端口 8080，registration-id = `demo-saml`）

如配置：

```yaml
cu:
  security:
    login:
      saml:
        # SP 侧 ACS（登录回调）地址模板
        login-processing-url: /login/saml2/sso/{registrationId}
        logout-request-url: /logout/saml2/slo
        logout-response-url: /logout/saml2/slo
        # 同样无默认值且必须至少配一个：SamlConfig 里直接 registrations.stream()，留空会启动失败
        registrations:
          - registration-id: demo-saml            # 留空时 getter 回退返回 sp-entity-id
            sp-entity-id: cu-framework-local      # 需与 IDP 侧配置的 SP Audience 一致
            metadata-resource: "classpath:saml/idp-metadata.xml"   # 会解析出 entityId / SSO URL / 验签证书
            #登录入口：http://localhost:8080/saml2/authenticate/demo-saml
            #ACS 回调：http://localhost:8080/login/saml2/sso/demo-saml
            #SP 元数据：http://localhost:8080/saml2/service-provider-metadata/demo-saml
            #退出：http://localhost:8080/saml2/logout
        # SP 签名密钥库：不配则 AuthnRequest / LogoutRequest 不签名。
        # demo 的 idp-metadata.xml 里 WantAuthnRequestsSigned=false，所以留空即可跑通
```


- **登录入口**：http://localhost:8080/saml2/authenticate/demo-saml
- ACS 回调：http://localhost:8080/login/saml2/sso/demo-saml
- SP 元数据：http://localhost:8080/saml2/service-provider-metadata/demo-saml
- 退出：http://localhost:8080/saml2/logout

例如：http://localhost:8080/saml2/authenticate/cu-local-registration

## 提示
- 访问入口前确保 `login-boot-sample` 已启用 SAML（`application.yml` 里 `cu.security.login.saml.registrations` 至少一条），否则 `SamlConfig` 启动就会 NPE。
- demo 用的 `idp-metadata.xml` 里 SSO 地址是占位的 `http://localhost:9010/saml/sso`，要跑通完整流程记得把 IdP 起在 9010，或整份替换 metadata。