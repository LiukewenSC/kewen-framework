# Tasks

## 1. 协议子类型

- [x] 1.1 新增 `oauth2/Oauth2SecurityUser`（`extends SecurityUser implements OidcUser`，承载 `attributes` / `idToken` / `userInfo`，纯 OAuth2 客户端 idToken 为 null）；`Oauth2SecurityUserTest` 5 条断言固定语义边界：`getUsername()` 是本地用户名而 `getName()` 是姓名、权限只来自本地账号、`getClaims()==getAttributes()`、`getIdToken()` 原样保留（对应 design R1/R8）
- [x] 1.2 新增 `saml/SamlSecurityUser`（`extends SecurityUser implements Saml2AuthenticatedPrincipal`，承载多值 `attributes` / `sessionIndexes` / `relyingPartyRegistrationId`）；`SamlSecurityUserTest` 2 条断言三者返回组装时给定值且账号字段逐项复制
- [x] 1.3 两个子类补齐 `@EqualsAndHashCode(callSuper = true)` + `@Getter/@Setter`，`mvn -pl login-security-spring-boot-starter compile` 通过且无 lombok 继承告警
- [x] 1.4 （实施中新增，见 design D1b）`SecurityUser.gender` 与 `LoginSuccessResult.gender` 由 `Integer` 改 `String`，否则子类型无法实现 `OidcUser`；连带 `RbacSecurityUserDetailsService` 的 Integer→String 转换。验证：全仓 `mvn -o -DskipTests install` 通过，auth-rbac sample 以 H2 启动成功

## 2. OAuth2 / OIDC 主体装配

- [x] 2.1 装配落在 `oauth2/SecurityUserSsoAssembler`：以 `OAuth2User.getName()` 为用户名调 `SecurityUserDetailsService.loadUserByUsername` 组装 `Oauth2SecurityUser`，账号不存在时抛带 `SsoErrorCodes.LOCAL_USER_NOT_FOUND` 的 `OAuth2AuthenticationException`。 **实现改为在外层包装而非派生 `DefaultOAuth2UserService`**（见 design D3），行为与任务描述一致。`SsoSecurityUserConverterTest` 覆盖映射成功与本地无账号两条分支
- [x] 2.2 `Oauth2Config` 的 `buildUserServiceMap()` 分发表一行未动（含 `SECURE_FALLBACK` 与 ignore-ssl 的 `RestOperations` 注入），`oauth2Login` 用新的 `mappedUserService()` 包一层；验证 `Oauth2UserServiceWiringTest` 4 条断言原样通过
- [x] 2.3 新增 `oauth2/SecurityUserOidcUserService extends OidcUserService` 覆写 `loadUser(OidcUserRequest)`，内部 userinfo 服务仍指向未装配的那张分发表（避免同一次登录查两遍账号）；`OidcUser`/idToken/权限三条断言在 `SecurityUserSsoAssemblerTest.assemblesOidcPrincipalKeepingIdToken` 上验证（该 Service 本身是三行委托，端到端留给 6.1 的 OIDC 实跑）
- [x] 2.4 `createOidcClientRegistration` 补 `userNameAttributeName`（方法放宽为包内可见以便测试）；`OidcClientRegistrationTest` 用本地一次性发现端点断言：配了 `preferred_username` 就生效，没配则回退 `sub`
- [x] 2.5 删除 `Oauth2Config.oauth2JsonSuccessResultConverter()` Bean 与 `oauth2/result/Oauth2AuthenticationSuccessResultConverter.java`（连空目录一起）；全仓 grep 该类名仅剩决策记录里的历史描述

## 3. SAML 主体装配

- [x] 3.1 新增 `saml/SecurityUserSamlAuthenticationConverter`：委托 `createDefaultResponseAuthenticationConverter()` 取回属性/NameID/registrationId，补 `AuthnStatement` 的会话索引，产出 `new Saml2Authentication(samlSecurityUser, saml2Response, 本系统权限)`；本地无账号抛 `Saml2AuthenticationException`。`SamlConfig` 侧**必须改用 `.authenticationManager(new ProviderManager(provider))`**——只 `http.authenticationProvider(...)` 时 `Saml2LoginConfigurer` 自注册的默认 provider 实测胜出，主体仍是 `DefaultSaml2AuthenticatedPrincipal`（已实跑复现并修正）
- [x] 3.2 `SecurityUserSamlAuthenticationConverterTest` 用无法解析的断言走真实 provider，断言抛 `Saml2AuthenticationException` 且组装环节计数为 0，证明校验先于组装且未被绕过；另有 6.1/6.3 的真实签名断言端到端验证
- [x] 3.3 删除 `SamlAuthenticationSuccessResultConverter` 与 `SamlConfig` 中的 Bean 注册；编译通过、无残留引用

## 4. 消费侧收敛

- [x] 4.1 `SecurityUserContext` 退化为纯静态：取 `SecurityContextHolder` 的 principal，不是 `SecurityUser` 就抛 `NoLoginException`；删掉 `ApplicationContextAware`、`ObjectProvider`、`UserDetailsService` 注入与 `SecurityBeanConfig` 里那个已无意义的 Bean。`SecurityUserContextTest` 覆盖无认证/匿名/账密/OAuth2/SAML 五种输入，并断言连续两次取到的是同一实例（即不再回查账号存储）
- [x] 4.2 `LoginSuccessResult.of(SecurityUser)` 静态工厂；`LoginSuccessResultTest` 断言字段逐项对应且不带 password、token 由处理器写入
- [x] 4.3 `JsonAuthenticationSuccessHandler` 改用 4.2 投影、去掉转换器循环与构造参数；同步清理 `SecurityBeanConfig` 与 `SecurityConfig` 的注入项。`LoginDeliveryPostureTest` 移除 `emptyConverters` 后 9 条姿态断言全过（顺带把测试里 `String` 类型的 principal 换成 `SecurityUser`，因为处理器现在认的是主体类型）
- [x] 4.4 删除 `SecurityUserAuthenticationSuccessResultConverter` 与 `AuthenticationSuccessResultConverter`；`mvn -pl login-security-spring-boot-starter clean test` 39 条全绿，全仓无代码引用

## 5. 配置语义与文档

- [x] 5.1 `Oauth2OidcProperties.userNameAttributeName` 注释改为"该声明的值须等于本系统用户名，登录后据此定位本地账号"，并说明未配置时 OIDC 回退 `sub`；`README.md` 与 `README-SSO.md` 的配置表同步
- [x] 5.2 `sample/login-boot-sample` 三条 OAuth2 客户端的 `user-name-attribute-name` 从 `sub` 改为 `preferred_username`；grep 确认不再有 `: sub`
- [x] 5.3 `README-SSO.md` 新增"身份源与本系统的分工"一节：主体统一、映射字段口径、`sso_local_user_not_found`、SAML 的 NameID 必须取 `username`（服务侧登出的 NameID 取自 `Authentication.getName()`）、权限为登录时快照、会话表存有 claims/idToken 不可导出
- [x] 5.4 `docs/说明文档/已废弃实现与决策记录.md` 新增第六节记录转换层的删除，四段齐全，并写明它当初换来的两个理由现在分别由谁承担
- [x] 5.5 改写同文档第一节：标注其替代方案已被废弃、给出失效的旧路径，保留"据提交记录推断、非作者确认"的标注

## 6. 集成验证（本机实跑）

## 6. 集成验证（本机实跑）

- [x] 6.1 三条链路各完整登录一次，全部实测通过（脚本走真实 IDP kw-idp-simple:9010，断言/授权/回调都是真的）：**OAuth2** 302 回 `/login.html?loginState=success`，`GET /api/me` 的 `authorities` 为 `ROLE_ADMIN/ROLE_USER`（无 `SCOPE_read`）、username/name 来自本地账号；**OIDC**（demo-oidc，discovery + nonce + jwks 验签）同样成功且 `/api/me` 一致——只有 `SecurityUserOidcUserService` 真的产出 `SecurityUser` 才可能成功，否则 401；**SAML2** NameID 取 username 时成功，`/api/me` 一致。顺带实证 2.4：OIDC 条目按 `preferred_username` 匹配到了本地 admin（按 `sub` 则必然落在 6.3 的拒登分支）
- [x] 6.3 "身份源有此人、本系统无账号"：把 IDP 的 `subject_source` 留在 `email` 跑 SAML，实测 302 回 `/login.html?loginState=failure`，同会话 `/api/me` 仍 401 未登录；服务端日志正面归因到新错误码——`单点登录认证失败，按失败姿态回跳：[sso_local_user_not_found] 断言认证的账号 [admin@example.com] 在本系统中不存在`。为拿到这条归因，给 `SecurityAuthenticationExceptionResolverHandler` 的重定向分支补了一行 WARN（此前 SSO 失败在服务端完全无声）
- [ ] 6.2 登出不退化。**SAML 侧验不成，根因已定位且与本变更无关**：干净实例上 SAML 会话 `POST /logout` 稳定返回 401（OIDC 会话同一请求返回 `{code:200,data:null}` 且无 Location，符合登出姿态要求）。链路是：sample 的 `saml/metadata/idp-metadata.xml` 里没有 `SingleLogoutService`（grep 计数 0）→ `SamlConfig.buildRegistrationFromMetadata` 因此不给 `assertingPartyDetails` 设 SLO 地址 → 5.7 的 `OpenSamlLogoutRequestResolver.resolve` 开头就 `return null` → `Saml2LogoutRequestFilter` 清掉上下文后继续走链 → `FilterSecurityInterceptor` 视其为匿名 → `InsufficientAuthenticationException` → 401。也就是说 `SamlSecurityUser` 带的 registrationId 与会话索引根本没被读到，要端到端验证 SLO 得先让 IDP 的元数据声明 `SingleLogoutService`，属单独一件事。OIDC 侧的 `id_token_hint` 端到端也不可观测——`oidcLogoutSuccessHandler` 这个 Bean 本框架从未接入过滤器链（`README.md` 第 6 条既已记录）；其前置条件"主体仍是 `OidcUser` 且带 ID Token"由 1.1 单测断言，且 6.1 的 OIDC 实跑只有在满足该条件时才可能成功
- [x] 6.4 `mvn -o -DskipTests install` 全 reactor 通过；`login-boot-sample` 与 `auth-rbac-boot-sample` 均以默认 H2 profile 启动成功，登录样例的账密与三条 SSO 入口、RBAC 样例的接口清单未回归
