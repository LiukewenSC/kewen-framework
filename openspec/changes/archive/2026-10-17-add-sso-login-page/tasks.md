# Tasks

## 1. 前置实测（结论写回 design.md，后续任务依赖它）

- [x] 1.1 以默认 h2 profile 启动 `sample/login-boot-sample`（端口 8080），用 `curl -i -s http://localhost:8080/oauth2/authorization/demo-oauth2` 记录未认证访问的响应状态与 Location，判定授权端点是被 `authenticationEntryPoint` 转成重定向还是直接 401，并把结论写入 design.md 的 Open Questions 第一条与 D8/D9 —— 实测 **302**，结论已写入 design.md 实测记录 (1) 与 D8/D9
- [x] 1.2 对同一实例记录基线响应码到 design.md：`/`、`GET /login`、`/saml2/authenticate/demo-saml`、`/login/oauth2/code/demo-oauth2?code=x&state=y`、`/login.html`（此时文件尚不存在，用于区分 401 与 404 的语义）—— 已写入 design.md 实测记录表格；根路径实为 HTTP 200 + 业务码 401，`.html`/`js`/`css` 为 404 证明 `web.ignoring` 生效
- [x] 1.3 查 Spring Security 5.7.11 的 `Saml2WebSsoAuthenticationRequestFilter` 与 `Saml2Configurer` 是否提供修改认证发起路径的入口，结论写回 design.md D7 与 Open Questions 第二条；若有入口，把"改为属性驱动并与 `sso-authorization-uri` 对称"列为后续任务 —— `Saml2LoginConfigurer:117` 该字段 private 无 setter，`loginPage()` 不改发起路径，**无入口**，D7 维持常量

## 2. 框架：新增配置项与属性字段

- [x] 2.1 新建 `SsoLoginProperties`，绑定 `cu.security.login.sso`，字段 `resultUrl`（原命名 `redirectUrl`，后因易与 `redirect-uri-template` 混淆而改名），并在 `SecurityBeanConfig` 的 `@EnableConfigurationProperties` 中注册；验证：`mvn -q -pl login-security-spring-boot-starter -am -DskipTests install` 编译通过，sample 加上该配置项后启动无绑定告警
- [x] 2.2 为 `SamlProperties.RegistrationSamlProperties` 增加 `displayName` 字段；验证：在 sample 的 `cu.security.login.saml.registrations[0]` 下写 `display-name` 后能取到值（以单测断言绑定结果，不留"看起来配了其实没生效"的字段）—— `LoginSsoPropertiesBindingTest` 4 例全绿，starter 合计 8 例通过
- [x] 2.3 在 `login-security-spring-boot-starter/README.md` 的配置参数表与 `sample/login-boot-sample/src/main/resources/application.yml` 顶部的参数总表注释中登记这两个新项，并注明"未配置 `result-url` 时交付姿态与变更前一致"；验证：逐项对照两处文档均出现且取值说明与代码默认值一致 —— 另在 README 新增"跨协议共享配置"与"未认证请求会看到什么"两节，把实测的 200+业务码 401 记进文档

## 3. 框架：认证结果交付姿态分流

- [x] 3.1 在 `JsonAuthenticationSuccessHandler.onAuthenticationSuccess` 内按认证端点分流（原写"按 `UsernamePasswordAuthenticationToken` 判定"，实现期因失败侧拿不到认证对象而改为端点判定，见 D1）：命中 SSO 回调端点且 `resultUrl` 非空时 `sendRedirect`，分支位于 `resultResolver.resolver(...)` 之前；验证：`LoginDeliveryPostureTest` 断言 `/login` 产出 JSON 体、两个回调端点产出 302 且响应体为空且解析器未被调用
- [x] 3.2 在 `SecurityAuthenticationExceptionResolverHandler` 的失败入口应用与 3.1 完全相同的判据（判据落在共用的 `LoginDeliveryResolver.shouldRedirect`，`commence`/`handle`/`onAuthenticationFailure` 三个入口统一受益）：SSO 失败产出带失败标记的重定向，账号密码失败保持既有 JSON 解析链；验证：单测断言 `/login` 上的 `BadCredentialsException` 仍进入 `HandlerExceptionResolver` 链且无重定向，`/` 上的未登录请求同样不重定向；SSO 回调上的失败产出 302 且不进解析链
- [x] 3.3 实现重定向 URL 的状态标记拼接：参数名固定为 `loginState`，值为 `success` / `failure`，配置值自带 query 时以 `&` 追加；URL 中 MUST NOT 出现会话标识或令牌；验证：单测断言两种标记下的 Location 精确值，并断言同一 `resultUrl` 分别带与不带 query 时拼接结果都正确（含绝对地址一例）
- [x] 3.4 在 `login-security-spring-boot-starter/README.md` 的登录章节说明四种入口各自的交付姿态与"账号密码永远 JSON"这一约束，并把"配置 `redirect-url` 后 SSO 失败不再返回 JSON"列为行为变更；验证：文档描述与 3.1-3.3 的断言一致，无遗漏协议 —— 已在 README"跨协议共享配置"节内加"各入口的交付姿态"表与四条要点

## 4. 框架：登录方式发现接口

- [x] 4.1 新增未经认证可访问的只读接口 `/api/login/options`，标注 `@SecurityIgnore`，注入 `Oauth2OidcProperties` 与 `SamlProperties`（不注入 `ClientRegistrationRepository`），返回账号密码段与单点登录条目数组；验证：给定 1 条 oauth2 客户端 + 1 条 saml 注册项时响应含 2 条且协议类别分别为 oauth2 与 saml
- [x] 4.2 实现展示名取值与回退：oauth2/oidc 取 `clientName`、saml 取 `displayName`、缺省回退 `registrationId`；验证：单测覆盖"手动端点模式（`protocol-type: oauth2`，其 `ClientRegistration` 未写入 clientName）仍能拿到展示名"这一条，并断言展示名永不为 null 或空串
- [x] 4.3 实现发起地址：oauth2/oidc 用 `ssoAuthorizationUri` 前缀拼接注册标识，saml 用常量 `/saml2/authenticate/` 前缀（依 1.3 结论决定是否转为属性），两类地址在同一响应字段名下给出；验证：单测断言两条完整地址字符串，并断言 `protocol-type: oidc` 的条目被标注为 oidc 而非 oauth2
- [x] 4.4 按 D8 修正后的形式实现剔除：注册标识解析不出非空值的条目不返回并输出含该配置位置的告警（原"HTTP 探测发起地址可达"已被实测否掉——两种协议的发起地址未认证下均可用，且表现形式不一致）；spec 该条需求已按 D8 改写为"无法构成有效发起地址的条目不得对外声明"，`clientWithoutRegistrationIdIsExcluded` 单测覆盖
- [x] 4.5 收敛响应字段集合为协议类别、注册标识、展示名、发起地址，以及账号密码段的地址与两个字段名；验证：单测以真实含密钥配置断言响应序列化结果不含 `clientSecret`、`keystorePassword`、`keyPassword`、证书与 metadata 资源路径；并断言不出现记住我参数名
- [x] 4.6 ~~依 1.1 结论放行 `/oauth2/authorization/**`~~ —— 1.1 实测为 **302**，放行并非必需且会白增一处免登录面，按 D9 决定**不做代码改动**；已用 `curl -i` 复验 `/oauth2/authorization/demo-oauth2` 与 `/saml2/authenticate/demo-saml` 在未认证下分别得到 302 与自动提交表单
- [x] 4.7 在 `login-security-spring-boot-starter/README.md` 登记该接口的完整契约：路径、未经认证可访问、逐字段含义、明确不含机密、以及"条目数随身份源配置变化"；验证：按文档给的 `curl` 命令可直接得到与文档一致的响应结构

## 5. Sample：配置启用与动态数量演示

- [x] 5.1 在 sample 的 `application.yml` 配置 `cu.security.login.sso.result-url: /login.html`，为 `demo-oauth2` 补 `client-name`、为 `demo-saml` 补 `display-name`；验证：`curl /api/login/options` 返回两条入口且展示名均为中文配置值
- [x] 5.2 追加 2 条 `protocol-type: oauth2` 的演示客户端，验证入口数量动态生效；验证项：应用启动无网络请求发生、`/api/login/options` 返回 4 条单点登录入口、前端渲染数量随之变化（由 6.2 承接目视确认）
- [x] 5.3 在 `application.yml` 注释中说明 OIDC 条目为何默认不配（构建时即拉取 issuer metadata，身份源离线则启动失败），并给出需要演示 oidc 类别时应如何配置；验证：按注释开启一条指向可达 issuer 的 oidc 配置能启动，且 `/api/login/options` 中该条目标注为 oidc

## 6. Sample：静态登录页

- [x] 6.1 新增 `src/main/resources/static/login.html`，含账号密码表单、单点登录入口容器、已登录视图区；验证：`curl -i http://localhost:8080/login.html` 在未认证状态下返回 HTML 而非 401，且 `SecurityConfig.java:79` 的白名单未被修改
- [x] 6.2 新增 `static/js/login.js`：拉取 `/api/login/options` 按数组长度渲染按钮，账号密码请求头精确写 `application/json`（不带 charset 参数），成败一律以响应体 `success` 字段判断，读取并 `history.replaceState` 剥离 `loginState`，SSO 按钮通过顶层导航发起；验证：浏览器实测按钮数量与 5.2 的 4 条一致、文案来自接口、用错误密码提交看到表单区错误提示而非跳转
- [x] 6.3 新增 `static/css/login.css` 并在页面引用；验证：`curl -i http://localhost:8080/css/login.css` 返回 200 且页面呈现样式
- [x] 6.4 在 sample 新增位于认证保护下的 `/api/me`（刻意不加 `@SecurityIgnore`），页面每次加载先查它判定登录态并渲染对应视图，已登录区提供登出；验证：登录后刷新页面仍显示当前用户，登出后回到登录表单且再刷新仍是表单
- [x] 6.5 确认页面不含"记住我"控件；验证：页面源码与渲染后的 DOM 中均无该控件，且 `/api/login/options` 未返回相关参数

## 7. 端到端集成验收

- [x] 7.1 以 `admin/admin123` 走完账号密码登录 → 已登录视图 → 登出，记录实际观察到的响应与页面状态；验证项：全程后端只出 JSON、无重定向、`Content-Type` 精确值生效
- [x] 7.2 用 `curl -i` 验证三种 SSO 的发起端点均产生指向身份源的重定向，并核对 Location 中的 `redirect_uri` 等于配置的后端回调地址。**已用真实 IDP 完成整条往返**：OAuth2 走 IDP 登录页 + 授权同意页，SAML 由 IDP 直接签发断言 POST 回 ACS，两者最终都落在 `/login.html`（状态标记被页面剥离），已登录视图分别显示 `sub=1`（ROLE_USER、SCOPE_read）与 NameID `admin@example.com`
- [x] 7.3 用 `curl -i "http://localhost:8080/login/oauth2/code/demo-oauth2?code=x&state=bad"` 构造校验失败的回调，验证返回带 `loginState=failure` 的重定向而非裸 JSON；验证项：Location 精确值与 `Set-Cookie` 行为
- [x] 7.4 回归未配置 `resultUrl` 的下游行为：临时移除该配置后重启，验证三种 SSO 成功处理器与失败处理器仍产出与变更前一致的 JSON，且账号密码登录不受影响；验证完成后恢复配置
- [x] 7.5 复核设计边界并追加证据到 design.md：实测根路径 `/` 返回 **HTTP 200 + 业务码 401**（不是 401 状态码，D12 结论"不新增放行"不受影响，但 7.5 原表述按实测更正）；`/api/login/options` 未带 Cookie 返回完整清单；`/api/me` 未登录返回 HTTP 200 + `{"code":401,"success":false}`，登录后返回用户名与权限

## 8. 首页与根路径入口（2026-10-17 追加，推翻 D12 原决定）

- [x] 8.1 用 `cu.auth.permit-url: /` 放行根路径，让 Boot 的欢迎页把 `static/index.html` 当首页返回；验证：未认证 `curl -i http://localhost:8080/` 返回 **200 + `Content-Type: text/html`**，不再是 `{"code":401}` JSON
- [x] 8.2 新增 `static/index.html` 与 `static/js/index.js`：整页初始 `hidden`，脚本查 `GET /api/me`，未认证 `location.replace('/login.html')`，已认证显示登录名与权限；验证：浏览器访问 `/` 最终停在 `/login.html` 且登录表单可见；登录后进入 `/` 停在首页并显示 `admin` / `ROLE_ADMIN、ROLE_USER`
- [x] 8.3 首页提供登出入口，成功后回登录页；验证：在首页点登出后落在 `/login.html`，`/api/me` 复探返回 `code:401`，再访问 `/` 仍自动转入登录页
- [x] 8.4 在 `login.html` 的已登录视图加"进入首页"入口，避免首页只能手输 `/` 到达；验证：登录后该链接文本与 `href="/"` 存在且点击可达首页
- [x] 8.5 复核既有行为未被根路径放行破坏：`/login.html` 200、`/api/login/options` 仍 4 条入口、SSO 回调失败仍 `302 /login.html?loginState=failure`、账号密码仍 JSON；验证：以上四项在 8080 重新起实例后复测通过，框架侧 25 例单测仍全绿
