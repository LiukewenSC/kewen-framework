# 单点登录总览

本文用最少的概念说明 OAuth2、OIDC、SAML2 在本框架里各自怎么走完一次登录，以及三者共享的会话、认证主体与结果交付规则。协议本身的细节见 [OAuth2](oauth2.md)、[OIDC](oidc.md)、[SAML2](saml2.md)。

## 身份源与本系统的分工

- 身份源只负责证明“这个人是谁”。
- 账号信息与权限一律来自本系统：SSO 成功后按身份源给出的用户名调用 `SecurityUserDetailsService`，取不到本地账号时以 `sso_local_user_not_found` 结束本次认证，不会留下一个没有权限的会话。
- 进入安全上下文的主体统一是 `SecurityUser`；OAuth2/OIDC 是 `Oauth2SecurityUser`，SAML 是 `SamlSecurityUser`。`SecurityUserContext.getCurrentUser()` 不需要知道本次登录走了哪种协议。
- 主体在会话里是登录时快照：改了权限或资料要重新登录才生效。
- 会话行里还存着身份源声明与 ID Token；不要把会话表导出或同步到日志。

身份源标识要能对上本地用户名：

- OAuth2 由 `user-name-attribute-name` 指定的声明提供（OIDC 未配置时回退 `sub`）。
- SAML 由断言的 NameID 提供。
- 两边取值口径必须一致；对不上时本次认证失败，不会创建无权限会话。

## 一次登录的时序

### OAuth2 / OIDC（SP 发起）

```
Browser              SP (this app)                 IdP
   |                      |                          |
   | GET /oauth2/authorization/{id}                   |
   |--------------------->|                          |
   | 302 to IdP authorize (state; OIDC adds nonce)    |
   |<---------------------|                          |
   | login / consent                                 |
   |----------------------------------------------->|
   | 302 /login/oauth2/code/{id}?code=...&state=...   |
   |<-----------------------------------------------|
   | GET callback                                    |
   |--------------------->|                          |
   |                      | POST token               |
   |                      |------------------------->|
   |                      | access_token (+ id_token)|
   |                      |<-------------------------|
   |                      | GET userinfo (optional)  |
   |                      |------------------------->|
   |                      |<-------------------------|
   | JSON or 302 result-url?loginState=success|failure|
   |<---------------------|                          |
```

OIDC 与 OAuth2 共用同一条过滤器链；区别是 OIDC 多一步 ID Token 校验（签名、nonce、`sub` 一致性），并可按需拉取 userinfo。

### SAML2（SP 发起）

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

当前只文档化 SP 发起流程。本框架没有为 SAML IdP 发起提供配置或示例，不承诺该模式可用。

## 端点总表

| 协议 | 用途 | 方法 | 默认路径 |
| --- | --- | --- | --- |
| 账号密码 | 登录 | POST | `/login` |
| 账号密码 | 登出 | POST | `/logout` |
| 通用 | 登录方式清单 | GET | `/login/options` |
| OAuth2/OIDC | 发起授权 | GET | `/oauth2/authorization/{registrationId}` |
| OAuth2/OIDC | 回调 | GET | `/login/oauth2/code/{registrationId}`（Ant 通配 `*`） |
| SAML2 | 发起登录 | GET | `/saml2/authenticate/{registrationId}` |
| SAML2 | ACS 回调 | POST | `/login/saml2/sso/{registrationId}` |
| SAML2 | SP Metadata | GET | `/saml2/service-provider-metadata/{registrationId}` |
| SAML2 | GET 退出（转 POST） | GET | `/saml2/logout` |
| SAML2 | 实际登出 | POST | `/logout` |
| SAML2 | SLO 请求/响应 | POST | `/logout/saml2/slo` |

OAuth2/OIDC 的本地授权基址与回调模板可配置；SAML 的 ACS 与 SLO 也可配置。SP Metadata 端点由本 starter 手动挂载，Spring Security 默认不暴露。

## 成功与失败怎么交付

由 `cu.security.login.sso.result-url` 决定，判定依据是“请求命中的认证端点属于哪类协议”，不看请求特征：

| 请求命中的端点 | 配置了 `result-url` | 未配置 `result-url` |
| --- | --- | --- |
| `/login`（账号密码） | JSON | JSON |
| OAuth2/OIDC 回调 | 302 + `?loginState=success` | JSON |
| SAML ACS | 302 + `?loginState=success` | JSON |
| 上述回调上的认证失败 | 302 + `?loginState=failure` | JSON 错误体 |
| `/logout`、业务接口、静态资源 | JSON | JSON |

- 账号密码登录永远返回 JSON，不因请求是脚本还是表单导航而改变。
- 回跳地址只带状态标记，不含会话标识与令牌；会话靠 Cookie 生效。
- 配置 `result-url` 后，消费方自定义的 `AuthenticationSuccessResultResolver` 在跳转场景不会被调用。

## 会话与认证主体

- 会话解析器优先读 `Authorization` 请求头，其次回退 Cookie；Token 会话的请求头固定为 `Authorization`，没有配置项。
- 持久化使用 `spring-session-jdbc`：H2 用 jar 内 schema，MySQL 用 [relation/login-security/spring-session-mysql.sql](../../../relation/login-security/spring-session-mysql.sql) 并设置 `initialize-schema: never`。
- 并发控制：`cu.security.session.maximum-sessions` 与 `max-sessions-prevents-login`；超限时按配置挤掉最早会话或阻止新登录。
- 当前用户通过 `SecurityUserContext.getCurrentUser()` 获取；未登录或主体不是统一认证实体时抛 `NoLoginException`。

## 登录方式发现接口

`GET /login/options`，未认证可访问（`@SecurityIgnore` 自动放行）：

```json
{
  "password": {
    "loginUrl": "/login",
    "usernameParameter": "username",
    "passwordParameter": "password"
  },
  "sso": [
    {
      "protocol": "oauth2",
      "registrationId": "demo-oauth2",
      "displayName": "演示 OAuth2 登录",
      "loginUrl": "/oauth2/authorization/demo-oauth2"
    },
    {
      "protocol": "saml",
      "registrationId": "demo-saml",
      "displayName": "企业单点登录",
      "loginUrl": "/saml2/authenticate/demo-saml"
    }
  ]
}
```

- 入口条数等于配置有效（能解析出注册标识）的身份源条数；缺失 `registration-id` 的客户端会被排除并输出 WARN。
- 只返回展示所需字段，不含 `client-id`、`client-secret`、密钥库口令、证书或资源路径。
- 不返回 remember-me：当前未接入，前端不应渲染对应控件。

## 明确不支持的项

- `cu.security.remember-me.*`：`[未接线]`，只创建 Bean，未挂入过滤器链。
- OIDC RP-Initiated Logout：处理器已声明为 Bean，但未接入 `http.logout()`，`/logout` 只失效本地会话。
- SAML IdP 发起：没有配置或示例，不承诺可用。
- 账号密码没有登录页：`formLogin` 未启用，`loginPage(...)` 会启动失败。
- OAuth2 授权类型固定为授权码模式；`authorization-grant-type` 当前 `[未接线]`。

## 快速自测

1. 启动 [sample/login-boot-sample](../../../sample/login-boot-sample/README.md)（H2，端口 8080）。
2. `curl http://localhost:8080/login/options`，核对账密字段与 SSO 入口条数。
3. 用 `admin/admin123` 走一次账密登录，确认返回 JSON 且带回 Cookie。
4. 配 `result-url: /login.html` 后点一个 SSO 入口，确认成功/失败都 302 回登录页并带 `loginState`。
5. 观察 `/api/me`（示例自带）确认会话用户与权限；框架侧可用 `SecurityUserContext` 或 `auth-rbac-security` 的 `/currentUser`。

## 相关文档

- [login-security-spring-boot-starter](../README.md)
- [OAuth2 细节](oauth2.md)
- [OIDC 细节](oidc.md)
- [SAML2 细节](saml2.md)
- [login 示例](../../../sample/login-boot-sample/README.md)
- [全量配置参考](../../../docs/application.yml)
