# login-security-spring-boot-starter

## 这是什么

基于 Spring Security 的登录与单点登录 starter。三种登录方式并列挂在同一套过滤器链上：账号密码 JSON 登录、OAuth2 / OIDC 单点登录、SAML2 单点登录。三者共享登出、成功/失败处理器、放行 URL 汇总与统一认证主体 `SecurityUser`，但配置前缀、端点与身份源彼此独立。

## 何时使用

- 需要前后端分离的 JSON 登录、会话管理，或 OAuth2 / OIDC / SAML 单点登录。
- 想用 `SecurityUserContext` 统一取当前用户，不按协议分支。
- 不适用：只需要权限注解、不需要登录时，用 `auth-core-spring-boot-starter`；需要 RBAC + 登录态时，用 `auth-rbac-security-spring-boot-starter`。

## 5 分钟接入

**依赖**：

```xml
<dependencies>
    <dependency>
        <groupId>com.cu.framework.boot</groupId>
        <artifactId>login-security-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

**必要实现**：`SecurityUserDetailsService`。它是加载用户的核心逻辑，没有实现时启动失败。示例实现见 [SampleInMemorySecurityUserDetailsService](../../sample/login-boot-sample/src/main/java/com/cu/framework/sample/login/SampleInMemorySecurityUserDetailsService.java)。

**最小配置**：会话需要 DataSource 与 `spring-session-jdbc`。

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:login_demo;MODE=MySQL;DB_CLOSE_DELAY=-1
    username: sa
    password:
  session:
    store-type: jdbc
    jdbc:
      initialize-schema: embedded   # H2；MySQL 用 never 并先执行会话表脚本
```

**启动与验证**：

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' --cookie-jar cookie.txt

curl http://localhost:8080/api/login/options
```

- `/login` 是本 starter 的 JSON 登录端点（默认值），成功返回 JSON 并带回会话 Cookie。
- `/api/login/options` 未认证可访问，返回账密提交信息与单点登录入口清单。
- 本 starter 没有默认的“当前用户”接口；业务侧用 `SecurityUserContext.getCurrentUser()`，或引入 `auth-rbac-security-spring-boot-starter` 获得 `/currentUser`。

完整示例见 [sample/login-boot-sample](../../sample/login-boot-sample/README.md)：默认 H2、端口 8080，带登录页、账密登录与三协议入口。

## 三协议总览

| 协议 | 配置前缀 | 关键注册类 | 认证过滤器 | 依赖 `SecurityUserDetailsService` |
| --- | --- | --- | --- | --- |
| 账号密码 | `cu.security.login.password` | `PasswordSecurityConfig` | `JsonLoginFilter` | 是 |
| OAuth2 | `cu.security.login.oauth2` | `Oauth2Config` | `OAuth2AuthorizationRequestRedirectFilter` + `OAuth2LoginAuthenticationFilter` | 否 |
| OIDC | `cu.security.login.oauth2`（`protocol-type: oidc`） | `Oauth2Config` | 同上，额外走 `OidcUserService` 校验 ID Token | 否 |
| SAML2 | `cu.security.login.saml` | `SamlConfig` | `Saml2WebSsoAuthenticationRequestFilter` + `Saml2WebSsoAuthenticationFilter` | 否 |

三种协议进入安全上下文的主体都是 `SecurityUser`（单点登录的是其协议子类型），结果处理与 `SecurityUserContext` 不需要按协议分派。

## 未认证请求看到什么

下表是 `sample/login-boot-sample` 实测结果，未做额外放行：

| 请求 | 响应 |
| --- | --- |
| `GET /`、`GET /login` | HTTP 401 + `{"code":401,"success":false,"message":"未登录或登录状态已失效，请重新登录","errorType":"AUTHENTICATION"}` |
| 账号密码错误的 `POST /login` | HTTP 401 + `{"code":401,"success":false,"message":"用户名或密码错误","errorType":"AUTHENTICATION"}` |
| `GET /oauth2/authorization/{id}` | HTTP 302，Location 指向 IDP 授权端点并带 `state` 与 `redirect_uri` |
| `GET /saml2/authenticate/{id}` | HTTP 200 + HTTP-POST Binding 的自动提交表单（含 `SAMLRequest`） |
| `GET /api/login/options` | HTTP 200；`@SecurityIgnore` 自动放行 |
| `*.html`、`/js/**`、`/css/**` | 经 `web.ignoring` 绕过安全过滤器链，资源不存在时由 MVC 返回 404 |

未登录与凭据错误都返回 **HTTP 401 + `code` 401**，用 `errorType=AUTHENTICATION` 与提示文案区分；无权限返回 403 + `errorType=AUTHORIZATION`。账号密码登录永远返回 JSON；单点登录的交付姿态由 `cu.security.login.sso.result-url` 决定。

## 自动装配

| 配置类 | 生效条件 | 产出 |
| --- | --- | --- |
| `SecurityBeanConfig` | 无 | 全局异常过滤器、`PasswordEncoder`（未定义时）、`LoginOptionsFactory` / `LoginOptionsEndpoint`、`LoginDeliveryResolver`、默认 JSON 成功处理器、安全异常解析处理器、放行 URL 收集器、结果解析器 |
| `SecurityConfig` | 无 | 安全过滤链：静态资源放行、放行清单、登出、认证入口/拒绝处理器、禁用 CSRF，并应用全部 `HttpSecurityCustomizer` |
| `KewenAuthConfig` | 无 | `SecurityResponseBodyResultResolver`、`AuthenticationExceptionAdviceHandler` |
| `SessionConfig` | 无 | 优先 Header（`Authorization`）再回退 Cookie 的会话解析器；`remember-me` Bean；`JdbcIndexedSessionRepository` 包装器；最大会话数定制器 |
| `PasswordSecurityConfig` | 无 | JSON 账号密码登录过滤器 |
| `SamlConfig` | `registrations` 非空 | SAML2 登录、登出、SP Metadata 暴露 |
| `Oauth2Config` | `clients` 非空 | OAuth2 / OIDC 登录与客户端注册仓库 |

## 配置项

| 分组 | 键 | 说明 |
| --- | --- | --- |
| 放行 | `cu.auth.permit-url` | 免登录 URL，分号或逗号分隔；与 `@SecurityIgnore` 合并 |
| 当前用户 | `cu.security.current-user-url` | 默认 `/currentUser`；只被 `auth-rbac-security` 的端点消费，单独引入本 starter 时不生效 |
| 会话 | `cu.security.session.maximum-sessions` / `max-sessions-prevents-login` | 最大会话数、超限是否阻止登录 |
| 记住我 | `cu.security.remember-me.*` | **[未接线]** 只创建 `RememberMeServices` Bean，未挂入过滤器链 |
| 账密登录 | `cu.security.login.password.*` | 登录地址与参数名，默认 `/login`、`username`、`password` |
| SSO 结果页 | `cu.security.login.sso.result-url` | 留空返回 JSON；配置后 OAuth2/OIDC/SAML 成败都 302 到该地址并带 `loginState=success|failure` |
| OAuth2/OIDC | `cu.security.login.oauth2.*` | 见 [docs/oauth2.md](docs/oauth2.md)、[docs/oidc.md](docs/oidc.md) |
| SAML2 | `cu.security.login.saml.*` | 见 [docs/saml2.md](docs/saml2.md) |

完整键表与默认值见 [docs/application.yml](../../docs/application.yml)。

## 前置条件与已知空转项

- **必须实现 `SecurityUserDetailsService`**；账号密码登录强依赖它，OAuth2 / OIDC / SAML2 不依赖它（用户信息从身份源拉取后按用户名映射本地账号）。
- 会话持久化依赖 `spring-session-jdbc` 与 DataSource；MySQL 启动前执行 [relation/login-security/spring-session-mysql.sql](../../relation/login-security/spring-session-mysql.sql)，并把 `spring.session.jdbc.initialize-schema` 设为 `never`。
- 没有登录页：`formLogin` 未启用，`loginPage(...)` 会直接启动失败；需要登录页时放行静态页并自行实现。
- `cu.security.remember-me.*` 当前 `[未接线]`。
- `cu.security.login.oauth2.clients[i].authorization-grant-type`、`user-info-authentication-method` 当前 `[未接线]`。
- `cu.security.login.saml.login-page-url` 当前 `[已废弃]`，未实现。
- `OidcClientInitiatedLogoutSuccessHandler` 已声明为 Bean，但未接入 `http.logout()`；OIDC 登出不会通知 IDP。
- OIDC 客户端在启动阶段访问 `{issuer-uri}/.well-known/openid-configuration`；身份源不在线时应用起不来。
- SAML 当前只按 metadata 构建注册；`registrationId` 为空时回退 `sp-entity-id`；只文档化 SP 发起流程，未承诺 IdP 发起。
- JSON 登录的 `Content-Type` 必须精确等于 `application/json` 或 `application/json;charset=UTF-8`；带小写 charset 参数会被当成表单，读取不到用户名密码。

## 相关文档

- [单点登录总览](docs/sso-overview.md)
- [OAuth2 细节](docs/oauth2.md)
- [OIDC 细节](docs/oidc.md)
- [SAML2 细节](docs/saml2.md)
- [login 示例](../../sample/login-boot-sample/README.md)
- [auth 总览](../../auth/README.md)
- [全量配置参考](../../docs/application.yml)
