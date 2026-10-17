# Design

## Context

动机见 `proposal.md - Why`，需求见 `specs/`。这里只记录塑造方案的现状与约束，均已核对到代码行。

**三条协议共用同一对处理器。** `SecurityBeanConfig.java:58-63` 以 `@ConditionalOnMissingBean` 注册唯一的 `SecurityAuthenticationSuccessHandler`，被 `PasswordSecurityConfig.java:48`、`Oauth2Config.java:169`、`SamlConfig.java:81` 三处注入为 successHandler，失败侧 `Oauth2Config.java:171`、`SamlConfig.java:82` 与 `PasswordSecurityConfig.java:49` 共用 `SecurityAuthenticationExceptionResolverHandler`。因此姿态分流只需一个落点即可覆盖四种入口。`SecurityConfig.java:108-110` 的 `logoutSuccessHandler` 引用同一个对象的另一个方法，登出不受影响。

**框架明确拒绝登录页语义。** `JsonLoginAuthenticationFilterConfigurer.java:37-40` 把 `loginPage(String)` 硬写成 `throw new RuntimeException`；`SamlProperties.java:88-89` 的 `loginPageUrl` 已 `@Deprecated` 且注释自述"SpringSecurity 5.3 没有地方可以修改"，其语义是登录**入口**而非**回跳**。两者都不能拿来承载本次需要的"完成后回前端"。

**SSO 发起端点的实测结果与原判断相反。** 原以为 `/oauth2/authorization` 因位于 `anyRequest().authenticated()` 之下而可能返回 401；实测（见文末"实测记录"）该路径**返回 302 且 Location 正确**，`/saml2/authenticate/demo-saml` 返回 200 的 HTTP-POST 自动提交表单，两者都**无需任何新增放行**即可用——Spring Security 的入口点对未认证请求产出的正是通往授权端点的重定向。因此 D9 取消，D8 的"不可达"前提不成立。

**身份源两侧不对称，且展示名不能从框架对象读。** `SamlConfig.java:107-116` 的 `RelyingPartyRegistrationRepository` 是 Bean 且实现 `Iterable`；`Oauth2Config.java:205` 的 `clientRegistrationRepository()` 是 `private` 且无 `@Bean`，同一份 clients 在 `Oauth2Config.java:165` 与 `:306` 被各自重建。更关键的是 `clientName` 只在 OIDC 分支写入（`Oauth2Config.java:283`），`createOAuth2ClientRegistration`（`:226-242`）根本不读它——从 `ClientRegistration` 反查展示名会在手动端点模式下拿到 null。

**配置类现状。** `cu.security.login` 前缀本身没有任何属性类绑定，现有三个子前缀各自独立：`password`（`SecurityLoginProperties`）、`oauth2`（`Oauth2OidcProperties`，含 `clientName`）、`saml`（`SamlProperties`，`RegistrationSamlProperties` 只有 `metadataResource` / `registrationId` / `spEntityId` 三个字段）。

**其他约束。** sample 只依赖 `login-spring-boot-starter` 与 web/jdbc/h2，未引入 auth-rbac，故 `cu.security.current-user-url` 无消费方。记住我三层未接入（无 `http.rememberMe()` 调用、`SpringSessionRememberMeServices` 只读请求参数、其 `autoLogin()` 恒返回 null）。`JsonLoginFilter.java:35` 用 `equals` 比较 Content-Type，带 charset 参数会降级为表单解析。OIDC 模式在 `build()` 时即拉取 issuer metadata，身份源不在线则应用启动失败。

## Goals / Non-Goals

**Goals:**

- 一个落点覆盖四种入口的交付姿态分流，且对未配置回跳目标的应用零行为变更
- 展示身份源所需的字段与真实可达性绑定，前端不含任何身份源知识
- 页面与后端之间不出现"JSON 渲染给用户看"的路径

**Non-Goals:**

- 不改造记住我为真实能力（只声明它不可用）
- 不把 `ClientRegistrationRepository` 提升为 Bean，也不修正 `clientName` 在 oauth2 分支缺失的这一不一致
- 不做 SAML 断言经前端转发的方案（需要拆分 `assertionConsumerServiceLocation` 与过滤器监听路径，两者现在同源于 `SamlProperties.loginProcessingUrl`，见 `SamlConfig.java:83` 与 `:131→155`）
- 不引入前端构建工具链、单页应用框架或模板引擎
- 不为跨站第三方 Cookie（外部真实 IDP）调整会话 Cookie 属性

## Decisions

### D1 分流依据：请求命中的认证端点（实现期由"认证对象类型"改此）

原计划以 `authentication instanceof UsernamePasswordAuthenticationToken` 作判据。失败路径否掉了它：`SecurityAuthenticationExceptionResolverHandler` 的三个入口（`commence` / `handle` / `onAuthenticationFailure`）只收得到 `AuthenticationException`，其中 Authentication 对象在多数失败下为 null，`commence` 处理匿名请求时更是根本没有。spec 要求"认证失败采用与成功一致的判别依据"，判据就必须对两类结果都可得。

改为 `LoginDeliveryResolver`：**重定向只在该请求命中某个单点登录回调端点时发生**，端点模板取自 `Oauth2OidcProperties.loginProcessingUrl` 与 `SamlProperties.loginProcessingUrl`。名单外的所有请求——账号密码端点 `/login`、`/logout`、业务接口、静态资源——恒为 JSON。

该依据仍满足 spec 的两条场景：账号密码用表单方式提交时命中的还是 `/login`，照样返回 JSON；三种协议共用同一判定，不存在某一协议被静默留在 JSON。它也不是浏览器请求特征（`Accept` / `Sec-Fetch-*` / User-Agent），而是本应用自己配置出来的端点形状，原方案担心的"按传输细节分流"不成立。

反向白名单（列举 `OAuth2AuthenticationToken`、`Saml2Authentication` 才重定向）依然被否：将来接入新协议时会默认落回 JSON，正是要修的那个 bug 会重新出现。按请求特征判定同样被否：账号密码若被表单方式提交会被误判成整页跳转，破坏"账号密码永远 JSON"，且判据来自浏览器行为而非本应用契约。

**路径匹配用 Spring 的 `AntPathMatcher`，不是 Spring Security 的 `AntPathRequestMatcher`。** 实测 `new AntPathRequestMatcher("/login/oauth2/code/*")` 对 `/login/oauth2/code/demo-oauth2` 返回 **false**（该匹配器对以 `/*` 结尾的模板有尾斜杠要求），会漏掉真实回调地址；`AntPathMatcher` 按标准 Ant 语义匹配，SAML 的 `{registrationId}` 模板变量同样支持。路径由 `UrlPathHelper.getPathWithinApplication` 取得，剥离 contextPath。

**两项任务表未列出的追加**：`spring-test` 以 test scope 加入 starter 的 pom（3.1-3.3 的验证方式需要 `MockHttpServletRequest` / `MockHttpServletResponse`）；`JsonAuthenticationSuccessHandler` 构造函数增加一个 `LoginDeliveryResolver` 参数，直接 `new` 该类的下游要跟着改，经 `SecurityBeanConfig` 装配的不受影响。

### D2 触发条件：回跳目标为空则完全保持现状

新增属性未配置时，成功与失败都走现有的 JSON 路径，一行行为都不变。这使得该变更对全部下游消费方非破坏，同时让配置了目标的 sample 获得重定向。被否决的替代是"配置了身份源就默认重定向到一个约定路径"：那会让所有现存的纯 JSON 消费方在升级后立即改变行为。

### D3 新属性落在 `cu.security.login.sso` 前缀

新建 `SsoLoginProperties`，字段 `result-url`，与现有 `password` / `oauth2` / `saml` 三个子前缀同级。

放 `SecurityProperties`（`cu.security`）被否决：该前缀目前只有 `current-user-url`，语义是"当前用户"，不属于登录。分别加到 `Oauth2OidcProperties` 与 `SamlProperties` 也被否决：那会造成 D8 里"部分协议静默降级"的同一个病根。

`result-url` 的值直接交给 `response.sendRedirect()`：相对值由容器按当前请求的 scheme/host/port 解析，绝对值原样使用，因此同一属性同时支持同源页面与独立部署的前端，不需要 `{baseUrl}` 展开逻辑。追加状态标记时需处理配置值自带 query 的情况（`?` 与 `&` 二选一）。

### D4 状态标记：固定参数名，回跳目标自行剥离

重定向为 `result-url` + `?loginState=success|failure`。参数名固定、不作为配置项：它只被 sample 页面消费，配置它等于给自己增加一个前后端必须同步的参数。

会话本身靠 Cookie 生效，URL 只携带状态标记，不携带 token 或用户信息。防回环由页面负责：读到标记后 `history.replaceState` 剥离 query。之所以不在后端剥离，是因为剥离意味着后端要认识前端的地址结构，而该认识正是本次想避免的耦合。

### D5 展示名读框架自己的属性类，不读 `ClientRegistration`

发现接口注入 `Oauth2OidcProperties` 与 `SamlProperties`，展示名取 `client.getClientName()` / `registration.getDisplayName()`，空则回退 `registrationId`。

替代方案是"把 `ClientRegistrationRepository` 变成 Bean 再从 `ClientRegistration.getClientName()` 读"，被否决：一是 `Oauth2Config.java:226-242` 在 oauth2 分支未写入 clientName，读到的就是 null（spec 的"手动端点模式"场景正是针对这条）；二是它本身在容器里有 2~3 份互不相干的实例，提升为 Bean 需要先处理这个；三是属性类才是配置的真相源，协议对象是它的派生物。

`protocolType` 字段天然把 clients 分成 oauth2 与 oidc 两组，正好对应四种入口，不新增任何概念。

### D6 SAML 新增 `display-name`，不复活 `loginPageUrl`

`RegistrationSamlProperties` 增加 `display-name`，与 OAuth2 侧已有的 `client-name` 对称。这个不对称本来就是漏配（`Oauth2OidcProperties.java:219` 有、`SamlProperties.java:97-130` 没有）。

`loginPageUrl` 保持废弃状态：它的语义是入口地址，且注释自述在当前 Spring Security 版本无实现落点。把一个废弃且语义不符的字段拿来承载回跳，会得到一个"看起来配了其实没生效"的配置项——正是 spec 里"未生效的能力不得对外声明"要禁止的情形。

### D7 SAML 发起路径用常量，不加属性（实测已确认）

发现接口对 SAML 条目拼接 `/saml2/authenticate/` + `registrationId`。实测 5.7.11 的 `Saml2LoginConfigurer`：字段 `authenticationRequestUri = "/saml2/authenticate/{registrationId}"`（`:117`）为 **private 且无 setter**，`loginPage(String)`（`:174`）只设置未认证时的跳转页、不改变发起路径，`loginProcessingUrl`（`:205`）管的是 ACS 而非发起路径。所以新增一个"可配置但配了也不生效"的属性是负资产，维持常量。代码中以常量加一行说明存在，升级 Spring Security 时再评估。

### D8 ~~发现接口校验发起地址可达后~~ 剔除解析不出注册标识的条目（实测后修正）

原写法是对每条候选发起地址做 HTTP 可达性判定。实测否掉了它的前提：两种协议的发起地址在未认证下都完全可用（oauth2 → 302、saml → 200 + 自动提交表单），不存在"点了必然失败"的案例；而且"可达"的表现形式按协议不同（302 与 200+HTML），通用探测规则无法成立，用状态码白名单去枚举等于把协议差异写进探测逻辑，比它想防的问题更脆。

保留下来的真实失效模式只有一种：`registrationId` 取不出来。oauth2 侧 `registration-id` 留空会拼出 `/oauth2/authorization/null`；saml 侧留空时 getter 回退返回 `spEntityId`（`SamlProperties.java:124-129`），回退值也为空则同样拼出坏地址。所以判定改为静态校验：注册标识解析不出非空值的条目不返回，并输出可检索告警。接口仍标注 `@SecurityIgnore` 放行。

### D9 取消：不新增任何 SSO 发起端点放行

实测证明 `/oauth2/authorization/**` 与 `/saml2/authenticate/**` 在未认证下已能正常触发认证流程，新增 `permitAll` 不会改善行为，只会把"未认证请求经过入口点"换成"经过匿名过滤器"，白增加一处免登录面。本任务项不做代码改动，仅保留实测证据。

### D10 前端形态：同源静态三件套

`src/main/resources/static/login.html` + `static/js/login.js` + `static/css/login.css`，原生 JS/CSS，无构建步骤。这一布局恰好全部命中 `SecurityConfig.java:79` 已有的 `web.ignoring` 白名单（`/**/**.html`、`/js/**`、`/css/**`），安全配置无需为本页面新增任何条目。

被否决的替代：独立前端工程 + 独立端口。它要求 `.cors()` 生效（`SecurityConfig.java:122` 当前是注释状态）并需要处理跨站 Cookie，把一个演示任务变成一轮基础设施改动。同源部署并不违反前后端分离——后端全程只当静态文件服务器与 JSON 端点，不拼装任何 HTML。

### D11 登录态判定端点由 sample 自建

页面刷新后需要判断当前会话是否已认证。`cu.security.current-user-url` 在本 sample 无消费方（未引入 auth-rbac starter），故在 sample 内提供一个位于 `anyRequest().authenticated()` 保护下的 `/api/me`，从 `SecurityContextHolder` 读取当前认证结果返回。它刻意不加 `@SecurityIgnore`——未登录时返回 401 正是页面需要的信号。

### D12 根路径返回首页，未登录由首页自己跳（2026-10-17 由用户推翻原决定后修订）

原决定是"根路径不做任何处理，README 写明入口是 `/login.html`"。用户在实际访问 `http://localhost:8080/` 看到 JSON 报错后否掉了它，要求根路径就是首页、未登录跳登录页，并点名只在 sample 里做。

落地方式选的是**前端跳转**而不是后端 302：用现成的 `cu.auth.permit-url: /` 放行根路径，让 Spring Boot 的欢迎页机制把 `static/index.html` 当首页吐出；首页整页初始 `hidden`，脚本查 `GET /api/me`，未认证就 `location.replace('/login.html')`。

被否决的后端跳转方案（在 `/` 上放一个放行 + 检查 SecurityContext + `sendRedirect` 的控制器）少一次往返、且不依赖 JS，但要新增一个返回非 JSON 的 sample 端点，而"后端只出静态文件与 JSON"正是这条链路上他一直守的边界；`location.replace` 还顺带避免了未登录的首页被压进历史、点后退再跳一次的往复。代价是未登录访问 `/` 多一次资源请求，且首屏短暂空白。

`/login.html` 的已登录视图同时加了"进入首页"入口，否则首页只能靠手输 `/` 到达。

### D13 不渲染记住我

页面不放"记住我"勾选框，发现接口也不返回该参数（spec 已约束）。放一个后端不消费的控件比不放更糟：它会让演示者以为它生效。

## Risks / Trade-offs

- **[三条协议共用一个 handler，分支判据漏判即静默走 JSON]** → D1 采用"账号密码为白名单、其余皆 SSO"的默认方向，使漏判的后果偏向新方向而非旧行为；并以一个覆盖三种协议的集成验收（登录往返后地址栏是页面而非 JSON）代替单测枚举类型。
- **[授权端点未被放行时，发现接口返回点了就失败的按钮]** → D8 在响应组装阶段做可达性判定并剔除 + 告警；D9 提供放行。两者叠加，配置遗漏的后果从"静默无效"变成"日志可见"。
- **[回跳带状态参数，用户收藏或前进后退回到带参数地址会再次触发判定]** → D4 由页面 `history.replaceState` 剥离；页面判定登录态只认 `/api/me`，状态参数仅作提示，因此剥离失败也只多显示一次提示，不会形成回环。
- **[发现接口未经认证可访问，泄露身份源清单]** → spec 已约束响应字段集合（协议类别、注册标识、展示名、发起地址），MUST NOT 含 client-secret、密钥库口令、证书内容与资源路径。验收含一次人工响应体审阅。
- **[OIDC 条目要真正演示出来需要在线 issuer]** → `Oauth2OidcProperties` 的 oidc 分支在构建时拉取 issuer metadata，身份源离线则启动失败。演示多条目 SSO 时以 `protocol-type: oauth2` 的条目增加数量（启动不发网络请求），oidc 条目只在有可达 issuer 时配置。README 的示例配置注明这一差别。
- **[真实外部 IDP 的跨站会话 Cookie]** → demo 的身份源在 `localhost:9010`，与页面 `:8080` 属同一 site（SameSite 判定看域名不看端口），Cookie 正常携带，本次不受影响。接真实外部 IDP 时需要 `server.servlet.session.cookie.same-site: none` 且 `secure: true`，而 `secure` 在非 HTTPS 上依赖浏览器把 localhost 视为安全上下文。列为 Non-Goal，不在本 change 处理。
- **[与在途 `sample-h2-profiles` 争抢 sample 的 `application.yml`]** → 该 change 尚余 2/29 任务。本 change 的 tasks 把 yml 改动收敛到 `cu.security.login` 段落，并在其归档后再执行；若需并行，冲突面限于该文件。
- **[失败路径分流改变了 SSO 失败时的 HTTP 语义]** → 现有 JSON 错误契约（状态码 401/403/200 混用、以 `success` 字段为准）只对账号密码保留，SSO 失败改产出重定向。这是 D1/D2 的直接后果，也是 spec "认证失败采用与成功一致的判别依据" 所要的行为；仅影响配置了回跳目标的应用。

## Migration Plan

1. 框架侧改动全部以"回跳目标未配置即不生效"为前提合入，下游无需变更配置即可保持现有行为。
2. 新增配置项后同步 `login-spring-boot-starter/README.md` 的参数表与 `sample/login-boot-sample/src/main/resources/application.yml` 顶部的参数总表注释——该文件已是项目约定的配置文档，遗漏它等同于文档与实现不一致。
3. sample 侧启用：配置 `result-url`、加入静态三件套与 `/api/me`。
4. 回滚：删除 `result-url` 配置即恢复变更前的全部对外行为（D2 保证，已实测）；`display-name` 与新增属性类是纯增量，可保留。

## Open Questions

三个问题均已关闭，结论并入上文：`/oauth2/authorization/**` 未认证下返回 302（见 D8/D9 与实测记录），Spring Security 5.7.11 不提供 SAML 发起路径的配置入口（见 D7）。

原 spec 里"单点登录发起地址对未认证调用方可达"这条需求的触发条件经实测不存在（两种协议的发起地址均未认证下即可用，且表现形式一为 302 一为 200+HTML，探测规则无法统一），已按 D8 改写为"无法构成有效发起地址的条目不得对外声明"——保留"不对前端撒谎"这一真实约束，把判据换成本项目实际可检的注册标识缺失。若你认为该收窄过头，恢复原写法即可，代价是实现一条永不触发的规则。

## 运行期证据（任务组 7，2026-10-16，IDP 在 localhost:9010 在线）

| 场景 | 实测结果 |
|---|---|
| 账号密码登录（页面点按钮） | HTTP 200 JSON，下发 `SESSION` Cookie；已登录视图显示 `admin` / `ROLE_ADMIN、ROLE_USER`，提示"登录成功" |
| 密码错误 | HTTP 403 + `application/json`，与变更前一致，无重定向 |
| 登出 | HTTP 200 JSON；随后 `/api/me` 回到 `{"code":401,"success":false}` |
| 页面刷新保态 | 重新加载后仍是已登录视图，登录态来自 `/api/me` 而非登录响应体 |
| OAuth2 真实往返 | `/oauth2/authorization/demo-oauth2` → IDP 登录页 → 授权同意页 → 回调 → **302 `/login.html?loginState=success`** → 页面剥离标记后显示已登录，`/api/me` 为 `sub=1`、`ROLE_USER、SCOPE_read` |
| SAML 真实往返 | `/saml2/authenticate/demo-saml` → IDP 签发断言 POST 回 ACS → **302 同上** → 已登录视图 NameID 为 `admin@example.com` |
| 回调校验失败（OAuth2） | 无 session 时 `GET /login/oauth2/code/demo-oauth2?code=x&state=bad` → **302 `?loginState=failure`**，`Set-Cookie` 新建会话 |
| 回调校验失败（SAML） | 向 ACS POST 垃圾 `SAMLRequest` → **302 `?loginState=failure`** |
| 回退（`result-url` 置空重启） | 上述两条 SSO 失败分别回到 **403 JSON**（`authorization_request_not_found`）与 **HTTP 200 + 业务码 401 JSON**；账号密码仍 403 JSON；`/api/login/options` 不受影响 → D2 承诺的"未配置即与变更前一致"成立 |
| 登录方式清单 | 未带 Cookie 返回 4 条入口（3 个 oauth2 + 1 个 saml），展示名为 yml 中配置的中文值；响应体中搜不到 client-secret、client-id、密钥库口令、metadata 路径、`9010`、`remember` |
| 静态资源免登录 | `/login.html` 200 `text/html`、`/js/login.js` 200、`/css/login.css` 200，`SecurityConfig.java:79` 白名单未改动 |
| 框架侧单测 | `mvn -pl login-spring-boot-starter test` → 25 例通过（既有 4 例 + 新增 21 例） |

未能覆盖的项：**"记住我"仍不生效**，本 change 只做到"不对外声明它"（接口不返回相关字段、页面不渲染控件），后端接入是独立的一块。另外**外部真实 IDP 的跨站 Cookie** 按 Non-Goal 未处理，本地 IDP 与页面同 site 所以不受影响。

## 实测记录（任务组 1，2026-10-16）

环境：`sample/login-boot-sample` 以默认 h2 profile 从 repackaged jar 启动于 8080；本地 IDP `kw-idp-simple` 运行于 9010。

| 请求（均未认证） | 实测结果 |
|---|---|
| `GET /` | HTTP **200** + body `{"code":401,"success":false,"message":"请求的权限不足，需要提升，请重新登录"}` |
| `GET /login` | 同上（HTTP 200 + 业务码 401） |
| `GET /oauth2/authorization/demo-oauth2` | HTTP **302**，Location = `http://localhost:9010/oauth2/authorize?response_type=code&client_id=client_50d901b5844247df&scope=read&state=...&redirect_uri=http://localhost:8080/login/oauth2/code/demo-oauth2` |
| `GET /saml2/authenticate/demo-saml` | HTTP **200** + HTTP-POST Binding 自动提交表单，form action = `http://localhost:9010/saml/sso`，含 `SAMLRequest` |
| `GET /login/oauth2/code/demo-oauth2?code=x&state=y` | HTTP **403**（过滤器已执行并因 state 无法关联而失败） |
| `GET /login.html`、`/js/login.js`、`/css/login.css` | HTTP **404**（`web.ignoring` 已放行，绕过安全链落到 MVC 后找不到资源） |
| `POST /login` `application/json` + admin/admin123 | HTTP **200** |

结论要点：

1. **(1) 授权端点无需放行** —— 推翻 D9，见 D8/D9 修正。
2. **(2) 根路径不是 401 而是 200 + 业务码 401** —— 7.5 里"根路径仍返回 401"的表述需按 `AuthenticationExceptionAdviceHandler` 的实际语义改为"HTTP 200 + `code:401`"。D12 的决定本身不受影响（仍不新增放行）。
3. **(3) `.html` 走 `web.ignoring` 的判断成立** —— 404 而非 401 直接证明该请求未经过 `FilterSecurityInterceptor`，D10 的免登录三件套路线可用；`/js/**`、`/css/**` 同理。
4. **(4) SAML 发起用的是 HTTP-POST Binding**，响应是 200 的自动提交表单而非 302，这是探测规则无法统一的原因之一。
5. **(5) 未发现任何"不可达的发起地址"**，D8 据此改为静态校验注册标识。
6. **(6) 附带发现**：IDP 的 `application` 表里该 client 注册的 scope 是 `openid,profile,email`，而 sample 配的是 `read`；IDP 不校验 scope，故 oauth2 链路照常（已实测拿到 302）。但 **oidc 类别要真正跑通必须让 scope 含 `openid`**，否则不返回 `id_token`；且 IDP 的 `/.well-known/openid-configuration` 当前在线可拉，所以任务 5.3 可以真实验证而不必停在"因无 IDP 未实测"。
