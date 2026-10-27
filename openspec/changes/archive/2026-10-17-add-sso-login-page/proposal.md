# Proposal

## Why

`login-security-spring-boot-starter` 目前只对外暴露 JSON 端点，仓库内没有任何前端页面，`sample/login-boot-sample` 因此无法演示它自己支持的四种登录方式（账号密码 / OAuth2 / OIDC / SAML2）；更关键的是 SSO 两条协议在浏览器整页跳转完成后，后端把登录结果 JSON 直接吐在用户屏幕上，没有任何调用方在消费它。SSO 与账号密码在"结果该给谁"上有本质区别，框架需要显式区分这两种响应姿态。

## What Changes

- 登录成功响应按认证协议类型分流：账号密码保持返回 JSON；OAuth2 / OIDC / SAML2 在配置了重定向目标时改为把浏览器送回前端页面。
- 登录失败响应采用同一个判别依据，避免 SSO 失败（用户在身份源取消、state/InResponseTo 不匹配、Audience 校验失败）时仍把裸 JSON 甩在浏览器上。
- 新增一个跨协议的重定向目标配置项。未配置时三种协议的现有 JSON 行为逐字保持不变，因此对现有下游消费方不构成破坏。
- 新增一个只读的登录方式发现接口：按已配置的 registration 数量返回 SSO 入口列表，供前端逐条渲染，不在前端硬编码任何身份源。
- `cu.security.login.saml.registrations[]` 新增 `display-name`，与 OAuth2 侧已有的 `client-name` 对称，用于按钮文案。
- `sample/login-boot-sample` 新增纯静态前端登录页（原生 HTML/CSS/JS，不引入模板引擎、不引入构建工具），账号密码表单 + 动态渲染的 SSO 入口 + 登录态展示与登出。
- 更新 `login-security-spring-boot-starter/README.md` 与 sample 的 `application.yml` 参数总表注释，使其覆盖上述新增配置项。

## Capabilities

### New Capabilities

- `identity/login-outcome-posture`: 认证结果（成功与失败）交付给调用方的姿态——按协议类别在 JSON 与整页重定向之间分流，以及未配置目标时的回退行为。
- `identity/login-method-discovery`: 对外暴露"当前可用登录方式"的可读契约，包括身份源数量的来源与展示名的取值与回退。
- `identity/sample-login-page`: sample 前端登录页的对外可观察行为：动态渲染入口数量、同源会话依赖、请求格式约束、错误提示判据。

### Modified Capabilities

无。`identity/oauth2-login` 只约束 userinfo 端点的 TLS 校验行为，本次改动不触碰该语义。

## Impact

**框架侧（`login-security-spring-boot-starter`）**

- `authentication/response/JsonAuthenticationSuccessHandler.java`：`onAuthenticationSuccess` 增加分支；`onLogoutSuccess` 不动（登出由前端 fetch 发起，保持 JSON）。
- `authentication/response/SecurityAuthenticationExceptionResolverHandler.java`：认证失败入口应用同一判别依据。
- 三类成功/失败处理器共用同一个 Bean（`SecurityBeanConfig.java:58-63`，`PasswordSecurityConfig.java:48`、`Oauth2Config.java:169/171`、`SamlConfig.java:81/82`），因此分支落点是单点。
- `authentication/saml/properties/SamlProperties.java`：`RegistrationSamlProperties` 新增 `display-name`；已废弃的 `loginPageUrl`（`:88-89`）语义是登录**入口**而非**回跳**，不复用、不复活。
- 新增跨协议配置项及其绑定位置（见 design.md 决策）。

**Sample 侧（`sample/login-boot-sample`）**

- 新增 `src/main/resources/static/` 下的 `login.html` 与 `js/`、`css/`；三者恰好命中 `SecurityConfig.java:79` 已有的 `web.ignoring` 白名单，安全配置无需改动。
- 新增登录方式发现接口的 Controller 与一个供页面刷新后判定登录态的已认证端点。
- `application.yml` 增加新配置项示例值。

**外部契约与风险**

- 发现接口的路径与响应结构是新增对外契约，一旦发布即为兼容性地基。
- 与在途 change `sample-h2-profiles`（27/29 任务）都会触碰 `sample/login-boot-sample/src/main/resources/application.yml`，需在其归档后变基或协调顺序。
- SSO 重定向方案不返回任何业务数据，因此不引入"凭证进 URL"的问题；跳转目标只携带状态标记，不携带 token。
