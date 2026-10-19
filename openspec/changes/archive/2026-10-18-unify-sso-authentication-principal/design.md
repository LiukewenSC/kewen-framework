# Design

## Context

四种认证方式现在往安全上下文里塞的是四种不同主体：账密是 `SecurityUser`，OAuth2 是 `DefaultOAuth2User`，OIDC 是 `DefaultOidcUser`，SAML2 是 `DefaultSaml2AuthenticatedPrincipal`。弥合这个差异靠的是一层按 `Authentication` 分派的转换器，见 proposal.md - Why。

下面的约束全部从本机 `~/.m2` 里 spring-security 5.7.11 的 sources jar 核对过，不是推断：

- `OAuth2AuthenticatedPrincipal.getAttributes()` 返回 `Map<String, Object>`（抽象方法），`Saml2AuthenticatedPrincipal.getAttributes()` 返回 `Map<String, List<Object>>`（default 方法）。同名、同参数、返回类型互不兼容 → **同一个类不可能同时实现这两个接口**，这是编译期事实。
- `Saml2Authentication` 的构造只要求 `AuthenticatedPrincipal`，`SecurityUser` 已经实现它 → SAML 侧放 `SecurityUser` 在编译上完全可行。
- 但 `OpenSamlLogoutRequestResolver`（取 SessionIndex、取 registrationId）与 `Saml2LogoutRequestFilter`、`OpenSamlLogoutResponseResolver` 都以 `principal instanceof Saml2AuthenticatedPrincipal` 为前提；registrationId 取不到时 `DefaultRelyingPartyRegistrationResolver.resolve(request, null)` 在请求 URL 里没有 `{registrationId}` 变量的情况下返回 null 注册项，登出请求整个不生成。`SamlConfig` 显式配置了 `singleLogoutServiceLocation`，说明服务侧单点登出是要用的功能。
- `OidcClientInitiatedLogoutSuccessHandler` 要求 `authentication.getPrincipal() instanceof OidcUser` 才取得到 id_token_hint。
- `AbstractAuthenticationToken.getName()` 先判 `UserDetails` 再判 `AuthenticatedPrincipal` → 主体换成 `SecurityUser` 后，`Authentication.getName()` 从"身份源 `user-name-attribute-name` 指向的值"变成本地用户名。
- `OidcAuthorizationCodeAuthenticationProvider` 与 `OAuth2LoginAuthenticationProvider` 的权限来自 `principal.getAuthorities()` → 主体带上本系统权限后，认证对象的权限自动跟着变，不需要额外装配。
- `OpenSamlAuthenticationProvider.authenticate` 的顺序是 `parse → process(全部签名/条件/主体校验) → responseAuthenticationConverter.convert(...)`，且 5.7.11 提供 `setResponseAuthenticationConverter` → 替换转换器只替换"组装"，**不会绕开任何校验**。
- 该 provider 的默认转换器用 `new DefaultSaml2AuthenticatedPrincipal(username, attributes)` 两参构造，SessionIndex 为空 → Spring 自己的服务侧登出请求今天就不带 SessionIndex。
- Lombok：`SecurityUser` 是 `@Data @Accessors(chain = true)`，字段 `name`（姓名）的 getter 同时充当 `AuthenticatedPrincipal.getName()`，语义与"主体标识"冲突。
- `OidcUser` 的父接口链上还挂着 `StandardClaimAccessor`，它提供 `default String getGender()`。`SecurityUser.gender` 原本是 `Integer`，同名方法返回类型不同 → **`SecurityUser` 的任何子类都无法实现 `OidcUser`**。这一条在规划阶段没识别出来，是实施时编译报错才发现的，处理见 D1b。
- `Saml2LoginConfigurer` 会无条件再注册一个默认的 `OpenSamlAuthenticationProvider`（`registerDefaultAuthenticationProvider`），因此只把自定义 provider 交给 `http.authenticationProvider(...)` 不保证它被选中——实测默认那个胜出、主体仍是 `DefaultSaml2AuthenticatedPrincipal`。处理见 D3。

### 与一条既有决策记录的冲突

`docs/说明文档/已废弃实现与决策记录.md` 第一节记录的正是"要不要在认证链路里做身份源主体 → 业务用户的转换"，当时的结论是**不做**，理由两条：业务查询把认证流程拖住；查不到用户时以认证异常冒出来，难以和真正的认证失败区分。它选的替代方案就是本 change 要删掉的这层延迟转换。

本 change 的方向与该记录相反，冲突按如下方式处理，不是回避：

- "难以区分"这一条由 D5 的可区分错误码正面解决——这正是当年放弃时缺的那块。
- "认证链路被数据库拖住"这一条不换位置解决，而是算总账：现状是登录时零次查询、之后每次取当前用户一次查询；改后是登录时一次、之后零次。会话内取用户两次以上即回本，而 `getCurrentUser()` 是权限判定的入口，正常一次请求就会调用。
- 该记录本身标注"据提交记录推断，非作者确认"，且它写的落地路径 `qy-idaas/idaas-authentications/...` 在仓库里已不存在（同一个类现在在 `login-spring-boot-starter/.../oauth2/result/`），指向已失效。
- 决定推翻之后，第一节与新增的转换层删除记录都要改写，理由不能跟着代码消失。见 tasks 第 5 组。

## Goals / Non-Goals

**Goals:**

- 三种协议的认证主体都能赋值给 `SecurityUser`，业务读当前用户不需要知道协议。
- 删掉转换器这一层，且不用新的 SPI 去替代它。
- OIDC 与 SAML2 依赖认证主体的协议级功能不退化。
- `ignore-ssl` 分发逻辑、交付姿态判定、`LoginSuccessResult` 的对外字段，三处零改动。

**Non-Goals:**

- 不做身份源账号与本系统账号的绑定关系表，不做首次登录自动建号。
- 不改前端契约，不改 `LoginSuccessResult` 字段。
- 不做旧会话兼容与迁移（开发阶段）。
- 不做会话内权限刷新（快照语义由 specs 显式承认）。
- 不动记住我空转、跨站 Cookie、OIDC 依赖 issuer 在线等已知缺口。

## Decisions

### D1 基类 + 两个协议子类型，而不是给 SecurityUser 加上全部接口

```
SecurityUser  (UserDetails, AuthenticatedPrincipal)      <- 业务唯一认识的类型，注解与字段不动
   ├── Oauth2SecurityUser  implements OidcUser           <- attributes / idToken / userInfo
   └── SamlSecurityUser    implements Saml2AuthenticatedPrincipal
                                                          <- 多值 attributes / sessionIndexes / registrationId
```

冲突来自 Context 第一条：`getAttributes()` 的返回类型使"一个类实现两套协议接口"不可能。OIDC 侧必须实现 `OidcUser`（id_token_hint），SAML 侧必须实现 `Saml2AuthenticatedPrincipal`（登出），所以只能分两个子类型。从业务视角仍然是"三种返回的认证实体都一样"：`SecurityUser u = SecurityUserContext.getCurrentUser()`。

- 备选 A：`SecurityUser` 直接实现 `OidcUser`，SAML 主体也用 `SecurityUser`，另外自写 `Saml2LogoutRequestResolver` / `Saml2LogoutResponseResolver` 从配置里取 registrationId。否决：为了补回本来免费可用的功能，要在框架里覆写两个 Spring 类，还得把 SAML 多值属性压平、给 `SecurityUser` 挂上一批只对单一协议有意义的字段和方法。与"尽量不乱改框架已有的东西"相反。
- 备选 B：OAuth2 与 OIDC 各一个子类型。否决：`OauthProtocolType` 就这两档且同属 OAuth2User 家族，一个类够用（纯 OAuth2 时 idToken 为 null），拆两个类只多出一份判空分支。
- 链式 setter：`@Accessors(chain = true)` 下父类 setter 返回 `SecurityUser`，无法继续链式设置子类字段 → 组装时先链式赋完父类字段，再单独赋子类原生字段。不改 `SecurityUser` 的注解，避免波及账密路径。

### D1b gender 由 Integer 改为 String（实施中经作者裁决）

`SecurityUser` 的子类型要实现 `OidcUser`，而它的父接口 `StandardClaimAccessor` 带来 `default String getGender()`，与 `Integer getGender()` 冲突（见 Context）。两条路：

- **采纳：把 `SecurityUser.gender` 与 `LoginSuccessResult.gender` 改为 `String`。** 于是子类可以直接实现 `OidcUser`，三种协议的主体字面上是同一个类。连带 `RbacSecurityUserDetailsService` 要把库里 `tinyint` 的 `Integer` 转成 String，登录响应的 `gender` 由 `1` 变成 `"1"`（前端不读该字段，已核对 sample 的 js）。
- 否决：OIDC 用一个不继承 `SecurityUser` 的 `OidcUser` 包装类内嵌本地用户，`getCurrentUser()` 多一个解包分支。这样三种协议的主体在 `SecurityContext` 里就不是同一个类，与本次需求"三种返回的认证实体都是一样的"直接抵触。

### D2 映射复用 SecurityUserDetailsService，不新增 SPI

协议校验通过、拿到身份源主体后，用 `principal.getName()` 作为本地用户名调用 `SecurityUserDetailsService.loadUserByUsername()`，把结果字段填进子类型。

`getName()` 的来源各协议本来就有定义，不需要新增配置：OAuth2 由既有的 `user-name-attribute-name` 决定，OIDC 在未配置时回退为 `sub`，SAML2 为断言的 NameID 值。

- 备选：新增一个"身份源主体 → 本地用户"的映射接口。否决：需求已定"身份源只验身份、权限本系统给"，业务要改映射行为本来就该覆盖 `SecurityUserDetailsService`，再来一层只是多一个要维护的对外概念。
- 连带影响：`user-name-attribute-name` 的语义从"代表用户唯一标识的字段"收紧为"该字段的值等于本地用户名"。属性注释、sample 配置、`docs/` 里的接入说明都要跟着改——现在 sample 配的是 `sub`，而本地 IDP 的 `sub` 是数字 ID，按新语义永远查不到账号。

### D3 各协议只动官方已经留出的钩子

- **OAuth2**：`oauth2UserService()` 那张按 registrationId 的分发表 **一行不动**（含 `SECURE_FALLBACK` 静态兜底与 `ignore-ssl` 的 `RestOperations` 注入），装配统一主体发生在它外面：`mappedUserService()` 取该 Bean 的结果再过一次 `SsoSecurityUserConverter`。原计划是派生一个 `DefaultOAuth2UserService` 子类塞进表里，实现时改为在外层包装——表内实例类型不变，`Oauth2UserServiceWiringTest` 里"实例必须等于 `SECURE_FALLBACK`"那类断言才不用跟着改，且 `identity/oauth2-login` 的四条要求完全不涉及。
- **OIDC**：`OidcUserService` 子类化，覆写 `loadUser(OidcUserRequest)`，把 `super.loadUser()` 结果的 claims / idToken / userInfo 落到 `Oauth2SecurityUser`。它的内部 userinfo 服务仍指向那张分发表（与现状 `oidcUserService()` 复用 `oauth2UserService()` 一致）——注意必须指向**未装配**的那个 Bean，否则同一次登录会查两遍本地账号。
- **SAML**：`OpenSamlAuthenticationProvider.setResponseAuthenticationConverter(...)` 里产出 `new Saml2Authentication(samlSecurityUser, saml2Response, authorities)`，组装时委托 `createDefaultResponseAuthenticationConverter()` 拿回属性/NameID/registrationId，只补会话索引。校验不受影响：provider 的顺序是 `parse → process(全部校验) → converter`。
  **但必须同时给 `saml2Login` 指定专属 `ProviderManager`**：`Saml2LoginConfigurer` 自己会再注册一个默认 provider，只靠 `http.authenticationProvider(...)` 时实测默认那个胜出（主体仍是 `DefaultSaml2AuthenticatedPrincipal`），因此原来那句 `http.authenticationProvider(...)` 换成 `.authenticationManager(new ProviderManager(provider))`，SAML 的认证器与共享 manager 脱钩。
- **`createOidcClientRegistration` 补 `userNameAttributeName`**：现在这个属性没传给 builder，OIDC 条目的 `getName()` 恒为 `sub`，D2 的映射在 OIDC 下没法按用户名走。
- **SessionIndex 主动填**：默认转换器不填（Context 最后一条），我们在组装时能从 `assertion.getAuthnStatements()` 取到，就填上，让 specs 里"登出请求包含会话索引"成立。这是相对 Spring 默认行为的有意增强，不是恢复。

### D4 响应体保留 LoginSuccessResult，改为从 SecurityUser 投影

`JsonAuthenticationSuccessHandler` 直接认 `SecurityUser`，投影逻辑收成 `LoginSuccessResult` 上的一个静态工厂；`token` 与 `loginTime` 的写入规则不变。

不把 `SecurityUser` 直接序列化成响应体，原因有三：`UserDetails` 的 `isAccountNonExpired()` 等会被 Jackson 展开成多余字段、`userAuthorities` 与现有 `authorities` 字段名不一致、`password` 有外泄面。

### D5 本地无对应账号 → 抛协议内认证异常

OAuth2/OIDC 抛 `OAuth2AuthenticationException`（带可区分错误码），SAML2 抛 `Saml2AuthenticationException`，两者都由既有 `SecurityAuthenticationExceptionResolverHandler` 接住。判定依据仍是"命中的认证端点"，所以 `identity/login-outcome-posture` 的失败姿态自动满足，不新增分支。

## Risks / Trade-offs

- **[R1] `getName()` 语义分裂** → `SecurityUser.getName()` 是姓名，而 `AuthenticatedPrincipal` / `OAuth2User` 约定它是主体标识。实现与评审时：凡是要"身份标识"的地方一律用 `getUsername()` 或声明原值字段，禁用 `getName()`。副作用是服务侧登出的 NameID 变成 `Authentication.getName()` = 本地用户名，所以身份源侧的 NameID 属性必须选 username，否则登出对不上身份源会话——这条要写进接入说明并在 sample 的 IDP 配置里对齐。
- **[R2] 权限成为会话期快照**，改权限不即时生效 → specs 已显式承认；账密登录本来就是这个语义，属于把 SSO 拉齐而不是新增退化。需要即时性时另开"会话内权限刷新"议题。
- **[R3] 会话体积与敏感内容**：主体上新挂 claims / idToken / 断言属性，`spring-session-jdbc` 会把它们写进会话行 → 与现状同级（Spring 本来就往会话里存 `DefaultOidcUser` / `DefaultSaml2AuthenticatedPrincipal`），不新增暴露面；但仓库是公开的，接入说明要写明会话表不可导出。
- **[R4] 移除 `SCOPE_*` 权限** → 已 grep 确认全仓无 `hasAuthority` / `hasRole` / `SCOPE_` 依赖；下游工程若用 `@PreAuthorize("hasAuthority('SCOPE_read')")` 会失效，已在 proposal 标为 BREAKING。
- **[R5] 加载本地账号发生在 userinfo / 断言校验线程内** → 身份源响应慢时登录更慢；无实测数据，接受。这条正是决策记录第一节的原始顾虑，账算在"与一条既有决策记录的冲突"里。
- **[R6] 旧 SSO 会话** principal 是协议自带类型，强转失败 → 按未登录处理，要求重新登录；开发阶段不做兼容代码。
- **[R7] `gender` 的对外取值类型变了**：登录响应里由 `1` 变成 `"1"`，`SecurityUser.getGender()` 的返回类型也从 `Integer` 变 `String`，下游工程若有 `int g = user.getGender()` 会编译失败。sample 前端不读该字段（已核对 `static/js/*.js`）；库里仍是 `tinyint`，转换只发生在 `RbacSecurityUserDetailsService` 一处。
- **[R8] 标准声明访问器被本地字段遮住**：`Oauth2SecurityUser` 的 `getGender()` / `getNickName()` / `getEmail()` 现在返回本系统账号的值，而不是 `StandardClaimAccessor` 约定的那个 OIDC 声明。要读声明原文得用 `getAttributes()` / `getClaim(...)`。这是实现"主体即本地账号"必然的取舍，写进类注释。

## Migration Plan

1. 先落两个子类型与新的用户服务 / 转换器组装，再把 `SecurityUserContext` 与 `JsonAuthenticationSuccessHandler` 切到强转，最后删转换器接口、三个实现和三处 Bean 注册。三步在同一 change 内连续完成，不留过渡开关与兼容分支。
2. 改 sample 的 `user-name-attribute-name` 与 IDP 侧配置：userinfo 返回的对应字段值等于本地用户名，SAML 应用的 NameID 属性选 username，回调地址仍在 IDP 的 `application` 表白名单内。
3. 验证：本机起 kw-idp-simple(:9010) 与 login-boot-sample，三条链路各登录一次；断言点为 `/api/me` 的 authorities 是本系统权限码而非 `SCOPE_read`、`getCurrentUser()` 返回本系统主键、OIDC 登出请求带 id_token_hint、SAML 服务侧登出请求能定位注册项并带 SessionIndex。
4. 回滚：改动集中在 `login-spring-boot-starter` 单模块，回滚即恢复转换器实现；回滚后既有会话同样需要重新登录。
