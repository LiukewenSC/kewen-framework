# Proposal

## Why

`Oauth2Config` 里 userinfo 这条链路叠了两层匿名 lambda（`oauth2UserService()` 的按 `registration-id` 路由，和 `mappedUserService()` 在其外面的装配），读代码时要跨两个 lambda 才能看出"谁在发 HTTP、谁在换主体"。同时配置类还兼任持有 SSL 兜底策略（`SECURE_FALLBACK`、`buildUserServiceMap()`、`resolveUserService()`），与它"声明 Bean 与构造 ClientRegistration"的职责混在一起。

## What Changes

- `oauth2UserService()` 与 `mappedUserService()` 两个 lambda 换成具名类：一个负责按 `registration-id` 路由到对应的用户信息服务实例，一个负责在路由结果之上装配统一认证主体。
- `SECURE_FALLBACK`、`buildUserServiceMap()`、`resolveUserService()` 从 `Oauth2Config` 迁出到负责路由的那个类，`Oauth2Config` 只保留 `@Bean` 声明与 `ClientRegistration` 构造。
- `SsoSecurityUserConverter` 的 `assemble(OAuth2User)` 与 `assemble(OidcUser)` 两个重载合并为一个方法：`OidcUser` 是 `OAuth2User` 的子类型，ID Token 与 UserInfo 用一次类型判断取出。
- OIDC 链路继续复用现有的 `SecurityUserOidcUserService`（`extends OidcUserService`），其内部 `setOauth2UserService(...)` 接的是**不装配**的路由实例，保持现状的"装配只发生在各协议链路末端一次"。
- 受影响测试类删除后重写，不保留旧实现。重写必须继续覆盖 `identity/oauth2-login` 已规定的两条语义：`ignore-ssl` 按客户端独立生效不得串用、`registration-id` 未匹配时落到执行标准证书校验的实例。

不含破坏性变更：对外配置项、认证主体类型、登录成功响应字段均不变。

## Capabilities

### New Capabilities

无。

### Modified Capabilities

无。本次不改变任何可观察行为：`identity/oauth2-login` 已经完整规定了 userinfo 的证书校验默认值、豁免粒度与未匹配时的回落方向，`identity/authentication-principal` 已经规定了主体统一、权限来自本系统、本地无账号即失败、以及 OIDC 需保留 ID Token。这些要求在重构前后保持一致，因此按纯重构处理，跳过 spec 增量（`.openspec.yaml` 置 `skip_specs: true`）。

## Impact

- 代码：`login-security-spring-boot-starter` 的 `com.cu.framework.authentication.oauth2` 包 —— `Oauth2Config`、`SsoSecurityUserConverter`、`SecurityUserOidcUserService`，以及新迁入的路由/装配具名类。
- 测试：`Oauth2UserServiceWiringTest`、`SsoSecurityUserConverterTest` 删除重写；`Oauth2SecurityUserTest` 预期不受影响。
- 不受影响：`ClientRegistration` 构造与 `Oauth2OidcProperties` 配置契约；SAML2 侧的 `SecurityUserSamlAuthenticationConverter`（它已是一个具名类，且 `Saml2AuthenticatedPrincipal.getAttributes()` 返回类型与 `OAuth2User` 不兼容，无法与本次的装配共用）；OIDC 端点发现与令牌交换的证书校验行为（豁免范围仍只限用户信息端点）。
- 依赖：Spring Security 5.7.11 的类结构是本次方案的前提约束 —— `OidcUserService` 与 `DefaultOAuth2UserService` 无继承关系，仅实现同一接口的不同泛型参数，且内部以组合方式使用后者。
