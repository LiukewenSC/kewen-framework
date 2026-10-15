

本 starter 在同一套 Spring Security 过滤器链上并列挂载三种登录方式：**账号密码登录**、**OAuth2 / OIDC 单点登录**、**SAML2 单点登录**。三者共享登出、成功/失败处理器、放行 URL 汇总等基础设施，但配置前缀、路径、认证源彼此独立。

## 必要实现

- **必须实现 `SecurityUserDetailsService`**，否则启动即报错了找不到实现类；这是加载用户的核心逻辑。
- **账号密码登录强依赖**这个接口；**IDP 单点登录（OAuth2 / OIDC / SAML2）不依赖**它（用户信息从 IdP 拉）。

## 三协议总览

| 协议 | 配置前缀 | 关键注册类 | 认证过滤器 | 是否依赖 SecurityUserDetailsService |
|---|---|---|---|---|
| 账号密码 | `cu.security.login.password` | `PasswordSecurityConfig` | `JsonLoginFilter` | ✅ |
| OAuth2 | `cu.security.login.oauth2` | `Oauth2Config` | `OAuth2AuthorizationRequestRedirectFilter` + `OAuth2LoginAuthenticationFilter` | ❌ |
| OIDC | `cu.security.login.oauth2`（`protocol-type: oidc`） | `Oauth2Config` | 同上，额外走 `OidcUserService` 校验 ID Token | ❌ |
| SAML2 | `cu.security.login.saml` | `SamlConfig` | `Saml2WebSsoAuthenticationRequestFilter` + `Saml2WebSsoAuthenticationFilter` | ❌ |

所有 `SecurityAuthenticationSuccessHandler` / `SecurityAuthenticationExceptionResolverHandler` 都是共享的：三条链路的成功走同一份 JSON 输出，异常走同一份 `HandlerExceptionResolver` 列表；每种协议各自实现一个 `AuthenticationSuccessResultConverter`（`SecurityUser...` / `Oauth2...` / `Saml...`）来分派 principal。

---

# 一、账号密码登录

## 背景

- 入口过滤器 `JsonLoginFilter extends UsernamePasswordAuthenticationFilter`，由 `JsonLoginAuthenticationFilterConfigurer` 挂链，注册类是 `PasswordSecurityConfig implements HttpSecurityCustomizer`。
- **无登录页**：`loginPage(...)` 被重写为直接 `throw new RuntimeException("启动失败，不支持配置loginPage")`，前端分离场景。
- 全局 `SecurityConfig` 里 `.csrf().disable()`，`/login` 与 `/logout` 均无需 CSRF token。
- 密码编码器为 `DelegatingPasswordEncoder`（`SecurityBeanConfig` 里 `@ConditionalOnMissingBean`）。

## 配置项

前缀：`cu.security.login.password`（对应 `SecurityLoginProperties`）

| 字段 | 默认值 | 必填 | 说明 |
|---|---|---|---|
| `login-url` | `/login` | 否 | POST 匹配路径 |
| `username-parameter` | `username` | 否 | JSON body / form 参数字段名 |
| `password-parameter` | `password` | 否 | JSON body / form 参数字段名 |

## 端点/入口总表

| 用途 | HTTP 方法 | 路径 | 由谁处理 | 是否可配置 |
|---|---|---|---|---|
| **账号密码登录** | POST | `{login-url}`，默认 `/login` | `JsonLoginFilter` | `cu.security.login.password.login-url` |
| 登出 | POST | `/logout` | Spring Security 默认 `LogoutFilter` | 本 starter 未覆盖 |
| 未登录访问受保护资源 | any | 命中 `authenticationEntryPoint` | `SecurityAuthenticationExceptionResolverHandler` | 通过 `@ControllerAdvice` 覆盖 |
| 无权限访问 | any | 命中 `accessDeniedHandler` | 同上 | 同上 |

**请求负载**：
- `Content-Type: application/json` 或 `application/json;charset=UTF-8`：body 里取 `{"username":"...","password":"..."}`
- 其他 Content-Type：回退到父类 form 分支，读 `application/x-www-form-urlencoded` 参数
- 非 POST：直接抛 `AuthenticationServiceException("Authentication method not supported: ...")`

## 与 Spring Security 默认的差异

| 项 | Spring Security 默认 | 本 starter 现状 | 说明 |
|---|---|---|---|
| 登录 URL | `/login` POST（formLogin 内建） | `/login` POST（Ant + `JsonLoginFilter`） | 路径一致，但处理过滤器不同 |
| 登录页 | `formLogin` 自动生成 `/login` GET 页 | **禁用**，配 `loginPage` 直接启动失败 | 前端分离 |
| CSRF | 默认启用 | `SecurityConfig` 里 `.csrf().disable()` | 走 session/cookie，前端不需要 CSRF token |
| Content-Type | 只支持 form | 优先 JSON，非 JSON 回退 form | 由 `JsonLoginFilter` 判断 |
| 用户来源 | 需要业务实现 `UserDetailsService` | 强制注入 `SecurityUserDetailsService` | 业务侧未提供实现，启动即失败 |
| 密码编码器 | `DelegatingPasswordEncoder` | 同（`@ConditionalOnMissingBean`） | 数据库 hash 需带 `{bcrypt}` 前缀 |
| 成功返回 | 默认 302 | 输出 JSON | `JsonAuthenticationSuccessHandler` 承担 |

## 各 sample 中的实际入口

### `sample/login-boot-sample`（端口 8080）

```yaml
cu:
  security:
    login:
      password:
        login-url: /login
        username-parameter: username
        password-parameter: password
```

- 登录：`POST http://localhost:8080/login`，body `{"username":"...","password":"..."}`
- 登出：`POST http://localhost:8080/logout`

### `sample/idaas-sp-boot-sample`（端口 8081）

配置同上（默认值），入口路径为 `/login` 与 `/logout`。

## 快速自测流程

1. 启动 `login-boot-sample`（H2 内存库，`SecurityUserDetailsService` demo 已提供实现）。
2. 未登录访问任一受保护接口 → 命中 `authenticationEntryPoint`，返回统一 JSON 异常结构（或兜底字符串）。
3. `curl -X POST http://localhost:8080/login -H "Content-Type: application/json" -d '{"username":"admin","password":"123456"}'` → 成功返回 `SecurityUserAuthenticationSuccessResultConverter` 输出的登录结果；Set-Cookie 带回 session。
4. 用同一 Cookie 再次访问受保护接口 → 200。
5. `curl -X POST http://localhost:8080/logout --cookie "..."` → 走同一个 `successHandler`，session 失效。

## 常见坑

1. **`/login` GET 会被拦截返回 401**：本 starter 没启用 `formLogin`，也就没有默认登录页。想放行登录页请通过 `UrlSecurityCustomizer.permitAll()` 或 `cu.auth.permit-url` 显式加入。
2. **`Content-Type` 必须精确匹配**：`JsonLoginFilter` 判断的是 `equals`，不是 `startsWith`。`application/json;charset=utf-8`（小写 utf-8）不会走 JSON 分支，会当 form 处理，最终读不到字段导致 `BadCredentials`。前端调用时用 `MediaType.APPLICATION_JSON_UTF8_VALUE`（大写 UTF-8）或干脆只写 `application/json`。
3. **`DelegatingPasswordEncoder` 对历史密码不友好**：数据库里存的是裸 bcrypt/MD5（没有 `{id}` 前缀）时会报 "There is no PasswordEncoder mapped for the id \"null\""。要么数据补前缀，要么业务侧覆盖 `PasswordEncoder` Bean。
4. **失败响应依赖 `HandlerExceptionResolver`**：容器里若一个都扫不到（例如没有 `@ControllerAdvice`），会走硬编码兜底字符串。生产项目至少提供一个统一异常处理类。
5. **`loginUrl` 不要与 `permit-url` 冲突**：`/login` 由 `JsonLoginAuthenticationFilterConfigurer` 自动 permitAll，业务侧再手写一次无害但容易误导。改自定义 `login-url` 时留意 starter 的自动放行是按配置值注册的，不需要额外放行。
6. **remember-me / 并发控制不在这里配**：见 `SessionConfig` 和 `cu.security.remember-me.*` / `cu.security.session.*`，与 password 模块解耦。

## 参考代码

- `password/config/PasswordSecurityConfig.java`
- `password/config/JsonLoginAuthenticationFilterConfigurer.java`
- `password/filter/JsonLoginFilter.java`
- `password/properties/SecurityLoginProperties.java`
- `config/SecurityConfig.java` / `config/SecurityBeanConfig.java`

---

# 二、OAuth2 / OIDC 单点登录

## 背景

- 通过 `Oauth2Config implements HttpSecurityCustomizer` 注册 `http.oauth2Login(...)`。OAuth2 与 OIDC **共用同一条过滤器链**：OAuth2 走授权码 → 令牌 → userinfo；OIDC 在此基础上多一步 ID Token 校验（并按需拉取 userinfo）。
- 配置前缀：`cu.security.login.oauth2`（对应 `Oauth2OidcProperties`）。
- 与 SAML2 使用独立的过滤器链与独立的 Repository，路径互不冲突。

## 配置项

**全局（`cu.security.login.oauth2`）**

| 字段 | 默认值 | 必填 | 说明 |
|---|---|---|---|
| `sso-authorization-uri` | `/oauth2/authorization` | 否 | 本地授权入口基址（与 Spring Security 官方默认一致） |
| `login-processing-url` | `/login/oauth2/code/*` | 否 | IDP 回调匹配（Ant 通配，取自 `OAuth2LoginAuthenticationFilter.DEFAULT_FILTER_PROCESSES_URI`） |
| `clients` | — | ✅ | 至少一条；空列表启动 NPE |

**每个 `clients[i]`**

| 字段 | 默认值 | 必填 | 说明 |
|---|---|---|---|
| `registration-id` | — | ✅ | 用于路径变量 `/oauth2/authorization/{registrationId}` |
| `protocol-type` | `oauth2` | 否 | `oauth2` / `oidc` |
| `client-id` | — | ✅ | IDP 分配 |
| `client-secret` | — | ✅ | IDP 分配 |
| `client-authentication-method` | `client_secret_basic` | 否 | 也支持 `client_secret_post` / `none` |
| `redirect-uri-template` | `{baseUrl}/login/oauth2/code/{registrationId}` | 否 | 需与 `login-processing-url` 匹配 |
| `scopes` | — | 建议 | OIDC 场景自动补 `openid` |
| `issuer-uri` | — | `oidc` 必填 | 用于 `.well-known/openid-configuration` 自动发现 |
| `authorization-uri` | — | `oauth2` 必填 | IdP 授权端点 |
| `token-uri` | — | `oauth2` 必填 | IdP 令牌端点 |
| `user-info-uri` | — | `oauth2` 必填 | IdP userinfo 端点 |
| `user-name-attribute-name` | — | `oauth2` 必填 | userinfo 响应里代表用户唯一标识的字段 |
| `jwk-set-uri` | — | `oidc` 可选 | 手动覆盖自动发现结果 |
| `client-name` | — | 否 | 日志展示 |
| `ignore-ssl` | `false` | 否 | **仅影响 userinfo**，Discovery / JWK 拉取不受影响 |

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

## 与 Spring Security 默认的对齐

- `sso-authorization-uri` 默认 `/oauth2/authorization`，**与官方 `OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI` 一致**。
- `login-processing-url` 默认取自 `OAuth2LoginAuthenticationFilter.DEFAULT_FILTER_PROCESSES_URI = "/login/oauth2/code/*"`，同样对齐官方。
- `redirect-uri-template` 默认 `{baseUrl}/login/oauth2/code/{registrationId}`，需与 `login-processing-url` 匹配，否则 IDP 会 302 到一个没有 filter 拦截的地址，表现是回调页 404 或直接命中 permitAll 白名单。

## 各 sample 中的实际入口

### `sample/login-boot-sample`（端口 8080）

配置：`clients[0].registration-id = demo-oauth2`，`protocol-type = oauth2`，端点占位指向 `http://localhost:9010/...`。

- 登录入口：http://localhost:8080/oauth2/authorization/demo-oauth2
- IDP 回调：http://localhost:8080/login/oauth2/code/demo-oauth2
- 退出（POST）：http://localhost:8080/logout

### `sample/idaas-sp-boot-sample`（端口 8081）

配置：三条 clients（`cu-local-oidc-registration`、`cu-local-oauth-registration`、`idp4-test-cu-loc-registration`）。

- OIDC 登录入口：http://localhost:8081/oauth2/authorization/cu-local-oidc-registration
- OIDC 回调：http://localhost:8081/login/oauth2/code/cu-local-oidc-registration
- OAuth2 登录入口：http://localhost:8081/oauth2/authorization/cu-local-oauth-registration
- OAuth2 回调：http://localhost:8081/login/oauth2/code/cu-local-oauth-registration

## 快速自测流程

1. 起本地 IDP（例如 Keycloak，端口 9010），把 `login-boot-sample` 的 `authorization-uri` / `token-uri` / `user-info-uri` 指向它。
2. 浏览器打开 http://localhost:8080/oauth2/authorization/demo-oauth2 ，正常应 302 到 IDP 登录页。
3. IDP 登录成功后 302 回 `/login/oauth2/code/demo-oauth2?code=...&state=...`，`OAuth2LoginAuthenticationFilter` 依次完成 CSRF 校验 → 换 token → 拉 userinfo → 走 `SecurityAuthenticationSuccessHandler`。
4. 想确认走的是 OIDC 分支还是 OAuth2 分支，看是否命中 `OidcUserService`：只有当响应里带回 `id_token` 时才会进入 OIDC 校验路径。

## 常见坑

1. **`Oauth2Config` 是无条件加载的**：只要引入了 `login-spring-boot-starter`，`cu.security.login.oauth2.clients` 必须至少一条，否则 `InMemoryClientRegistrationRepository` 空列表启动 NPE。
2. **`protocol-type: oidc` 在启动阶段就会访问网络**：`ClientRegistrations.fromIssuerLocation(issuerUri)` 同步拉取 `{issuer}/.well-known/openid-configuration`，IDP 不在线则应用起不来。本地 demo 想跳过网络请求就切回 `protocol-type: oauth2`。
3. **`ignore-ssl=true` 只影响 userinfo**：OIDC Discovery 与 JWK Set 拉取走 Spring 自带 HTTP 客户端，始终执行标准证书校验。自签 IDP 在 Discovery 阶段报 SSL 错时，把证书导入 JDK cacerts，配 `ignore-ssl` 没用。
4. **OAuth2 与 OIDC 共用同一路径**：同一条 `clients` 记录要么 OAuth2 要么 OIDC，由 `protocol-type` 决定。想同一份 IDP 暴露两种模式，需要声明两条 clients（参考 `idaas-sp-boot-sample`）。
5. **registrationId 与 SAML2 允许同名**：两套 registration 存在各自的 Repository，路径前缀不同（`/oauth2/authorization/*` vs `/saml2/authenticate/*`），不会冲突；但排查日志时容易混淆，建议在命名上加 `-oauth` / `-saml` 后缀区分。
6. **`OidcClientInitiatedLogoutSuccessHandler` 已注册但未接入过滤器链**：目前 `/logout` 只会走本地 session 失效，不会真正通知 IDP。若需要联动退出，还要在 `http.logout().logoutSuccessHandler(oidcLogoutSuccessHandler())` 处显式绑定。

## 参考代码

- `oauth2/Oauth2Config.java`
- `oauth2/Oauth2OidcProperties.java`
- `oauth2/RestOperationBuilder.java`
- `oauth2/result/Oauth2AuthenticationSuccessResultConverter.java`
- `docs/说明文档/oidc接入踩坑记录.md`

---

# 三、SAML2 单点登录

## 背景

- 通过 `SamlConfig implements HttpSecurityCustomizer` 注册 `http.saml2Login()` + `http.saml2Logout()`，并额外手动挂 `Saml2MetadataFilter` 暴露 SP metadata（Spring Security 默认不挂）。
- 配置前缀：`cu.security.login.saml`（对应 `SamlProperties`）。
- SP 发起 / ACS 回调 / SLO 三条独立过滤器，认证过滤器为 `OpenSamlAuthenticationProvider`（受 JDK 8 兼容约束，未使用 `OpenSaml4AuthenticationProvider`）。

## 配置项

**全局（`cu.security.login.saml`）**

| 字段 | 默认值 | 必填 | 说明 |
|---|---|---|---|
| `login-processing-url` | `/login/saml2/sso/{registrationId}` | 否 | ACS 路径模板，IdP 用它 POST SAMLResponse |
| `logout-request-url` | `/logout/saml2/slo` | 否 | SP 侧 LogoutRequest 接收地址 |
| `logout-response-url` | `/logout/saml2/slo` | 否 | SP 侧 LogoutResponse 接收地址 |
| `registrations` | — | ✅ | 至少一条；空列表 `SamlConfig` 启动 NPE |
| `sp-signing-keystore` | — | 否 | 不配则 AuthnRequest / LogoutRequest 不签名 |
| `login-page-url` | — | — | 已 `@Deprecated`，当前未实现 |

**每个 `registrations[i]`**

| 字段 | 默认值 | 必填 | 说明 |
|---|---|---|---|
| `registration-id` | 未填时 getter 回退返回 `sp-entity-id` | 建议 | 用于路径变量 `/saml2/authenticate/{registrationId}` |
| `sp-entity-id` | `cu-saml` | ✅ | 需与 IDP 侧配置的 SP Audience 一致，否则 `AudienceRestriction` 校验失败 |
| `metadata-resource` | `classpath:saml/idp-metadata.xml` | ✅ | 解析出 entityId / SSO URL / 验签证书 / SLO URL |

**`sp-signing-keystore`**

| 字段 | 默认值 | 必填 | 说明 |
|---|---|---|---|
| `keystore-resource` | — | 否 | 例 `classpath:saml/sp-keystore.jks`；未配则不签名 |
| `keystore-password` | `changeit` | 否 | JKS 密码 |
| `key-alias` | `sp-signing` | 否 | 密钥别名 |
| `key-password` | `changeit` | 否 | 密钥密码 |

**IdP metadata.xml 硬性要求**

- `WantAuthnRequestsSigned` **必须显式写值**：`IdpMetadataParser` 会拆箱成 `boolean`，缺省 NPE。
- 至少包含一个 `SingleSignOnService`（HTTP-POST 优先，其次 HTTP-Redirect）。
- `SingleLogoutService` 可选，无则不注册 SLO。

## 端点/入口总表

| 用途 | HTTP 方法 | 路径 | 由谁处理 | 是否可配置 |
|---|---|---|---|---|
| **SP 发起登录（入口）** | GET | `/saml2/authenticate/{registrationId}` | `Saml2WebSsoAuthenticationRequestFilter` | ❌ Spring Security 默认，本 starter 未改 |
| ACS 登录回调 | POST | `{login-processing-url}` | `Saml2WebSsoAuthenticationFilter` | ✅ `cu.security.login.saml.login-processing-url` |
| SP 元数据 | GET | `/saml2/service-provider-metadata/{registrationId}` | `Saml2MetadataFilter`（本 starter 手动挂上） | ❌ 固定路径 |
| GET 触发退出 | GET | `/saml2/logout` | `SamlLogoutController`（返回自动提交的 POST 表单） | ❌ 固定路径 |
| 实际登出 | POST | `/logout` | Spring Security `LogoutFilter` + `Saml2LogoutRequestFilter` | ❌ |
| SLO 请求/响应 | POST | `/logout/saml2/slo` | `Saml2LogoutRequestFilter` / `Saml2LogoutResponseFilter` | ✅ `logout-request-url` / `logout-response-url` |

## 与 Spring Security 默认的差异

| 项 | Spring Security 默认 | 本 starter 现状 | 说明 |
|---|---|---|---|
| SP 登录入口 | `/saml2/authenticate/{registrationId}` | 同 | 未覆盖 |
| ACS 回调 | `/login/saml2/sso/{registrationId}` | 同 | 通过 `loginProcessingUrl` 允许覆盖 |
| SP metadata 端点 | **默认不注册** | **额外手动挂 `Saml2MetadataFilter`** | 见 `SamlConfig.customizer()`；这是本仓库对官方的一处主动增强 |
| 认证 Provider | `OpenSaml4AuthenticationProvider`（Spring Security 5.7 起推荐） | `OpenSamlAuthenticationProvider` | JDK 8 兼容决策，见 `已废弃实现与决策记录.md` |
| SLO 端点 | `/logout/saml2/slo` | 同 | 拆成 request / response 两字段，可独立配 |
| GET 退出 | 不支持（CSRF 只走 POST） | 提供 `SamlLogoutController` 转成自动提交的 POST 表单 | 便于浏览器直接跳 |

## 各 sample 中的实际入口

### `sample/login-boot-sample`（端口 8080，registration-id = `demo-saml`）

```yaml
cu:
  security:
    login:
      saml:
        login-processing-url: /login/saml2/sso/{registrationId}
        logout-request-url: /logout/saml2/slo
        logout-response-url: /logout/saml2/slo
        registrations:
          - registration-id: demo-saml
            sp-entity-id: cu-framework-local
            metadata-resource: "classpath:saml/idp-metadata.xml"
        sp-signing-keystore:
          keystore-resource:            # 留空 → 不签名，demo 用
          keystore-password: changeit
          key-alias: sp-signing
          key-password: changeit
```

- 登录入口：http://localhost:8080/saml2/authenticate/demo-saml
- ACS 回调：http://localhost:8080/login/saml2/sso/demo-saml
- SP 元数据：http://localhost:8080/saml2/service-provider-metadata/demo-saml
- 退出：http://localhost:8080/saml2/logout

### `sample/idaas-sp-boot-sample`（端口 8081）

配置四条 registration：`cu-local-registration`、`idp4-test-cu-loc-registration`、`idp4-dev-cu-loc-registration`、`idp4-loc-cu-loc-registration`；且启用 `sp-signing-keystore=classpath:saml/sp-keystore.jks`。

- 登录入口：http://localhost:8081/saml2/authenticate/cu-local-registration（其余 registrationId 同理）
- ACS 回调：http://localhost:8081/login/saml2/sso/{registrationId}
- SP 元数据：http://localhost:8081/saml2/service-provider-metadata/{registrationId}
- 退出：http://localhost:8081/saml2/logout

## 快速自测流程

1. 准备 IdP metadata.xml（自签或从真实 IdP 下载），放到 `classpath:saml/idp-metadata.xml`。
2. 若 metadata 里 `WantAuthnRequestsSigned="true"`，用 `keytool` 生成 SP keystore 并配到 `sp-signing-keystore`；否则 keystore 留空即可。
3. 浏览器访问 http://localhost:8080/saml2/authenticate/demo-saml → 302 到 IdP SSO URL，附带 `SAMLRequest` 参数。
4. IdP 完成认证后 POST 回 `/login/saml2/sso/demo-saml`，`Saml2WebSsoAuthenticationFilter` 校验 Assertion 签名与 Audience → 走 `SecurityAuthenticationSuccessHandler`。
5. 想让 IdP 侧配置 SP，把 http://localhost:8080/saml2/service-provider-metadata/demo-saml 返回的 XML 交给 IdP 管理员即可。
6. 退出：`GET /saml2/logout` → 自动提交 `POST /logout` → 触发 SAML2 SLO 链路。

## 常见坑

1. **`registrations` 必须至少一条**：`SamlConfig.buildRegistrationFromMetadata` 直接 `registrations.stream()`，留空 NPE。
2. **`metadata.xml` 中 `WantAuthnRequestsSigned` 必须显式写值**：`IdpMetadataParser` 拆箱 boolean 缺省会 NPE。
3. **`sp-entity-id` 需与 IdP 侧 Audience 严格一致**：不一致会在校验 `AudienceRestriction` 时报错，日志表现是 "does not match AudienceRestriction"。
4. **`WantAuthnRequestsSigned=true` 但没配 keystore**：SP 发出的 `AuthnRequest` 无签名，IdP 会拒。要么关 IdP 侧强制签名，要么补齐 keystore。
5. **`OpenSamlAuthenticationProvider` vs `OpenSaml4AuthenticationProvider`**：本 starter 用前者是为了 JDK 8 兼容；OpenSAML 4 需 JDK 11+，若项目要求 JDK 8 请勿盲目升级到后者。
6. **`/logout/saml2/slo` 若 IdP 侧 POST 的 destination 不匹配**：`OpenSamlLogoutResponseValidator.validateDestination` 用全匹配；`singleLogoutServiceLocation` 与 `singleLogoutServiceResponseLocation` 都通过 `{baseUrl}` 拼接，反向代理场景要注意 host 拼接结果。
7. **`login-page-url` 是历史遗留字段**：`@Deprecated` 且当前无实现，配了不会生效；如果需要自定义入口请另建 Controller 重定向到 `/saml2/authenticate/{registrationId}`。

## 参考代码

```
- `saml/SamlConfig.java`
- `saml/properties/SamlProperties.java`
- `saml/IdpMetadataParser.java`
- `saml/SamlLogoutController.java`
- `saml/SamlAuthenticationSuccessResultConverter.java`
- `docs/说明文档/SAML2_SP_Metadata配置指南.md`
```

---

### 主要变化点

1. **顶部新增「三协议总览」表**，一眼看清三者的配置前缀、注册类、认证过滤器、是否依赖 `SecurityUserDetailsService` 的差异。
2. **每个协议小节结构统一为 8 段**：背景 / 配置项 / 端点/入口总表 / 与 Spring Security 默认的差异 / 各 sample 中的实际入口 / 快速自测流程 / 常见坑 / 参考代码。之前 SAML 那段完全没有「配置项」小节，现在补齐。
3. **配置项表**都拆成「全局」+「每个 registration / client」两层，并新增「必填」列，替代原来散落在 YAML 注释里的口头约定。
4. **端点总表列头统一为 5 列**（用途 / HTTP 方法 / 路径 / 由谁处理 / 是否可配置），之前 SAML 那张表只有 4 列且缺「由谁处理」。
5. **「常见坑」全部编号化**，SAML 部分从原来只有两条"提示"扩到 7 条（覆盖 `registrations` 空 NPE、`WantAuthnRequestsSigned` NPE、`sp-entity-id` Audience 匹配、签名 keystore 缺失、OpenSAML 版本、SLO destination 严格校验、`login-page-url` 已废弃）。
6. **`快速自测流程` 从 OAuth2 一节扩到三节都有**，账密那段之前完全没有；SAML 那段之前也没有独立的自测步骤。
7. **`必要实现` 挪到最上方**，作为三协议的前置说明；原来只提了一句，现在明确写清"账密强依赖，其他三种不依赖"。
