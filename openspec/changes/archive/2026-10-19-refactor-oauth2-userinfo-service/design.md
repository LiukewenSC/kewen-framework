# Design

## Context

动机见 proposal.md - Why。本节只记录塑造方案的现状与约束。

当前 userinfo 链路的实际形态（`login-spring-boot-starter/.../oauth2/Oauth2Config.java`）：

```
oauth2Login 的 userInfoEndpoint.userService(...)
    └─ mappedUserService()        private，lambda：userRequest -> assembler.assemble(内层.loadUser(userRequest))
         └─ oauth2UserService()   @Bean，   lambda：userRequest -> resolveUserService(map, id).loadUser(userRequest)
              └─ map: registrationId -> DefaultOAuth2UserService（ignore-ssl 的换了 RestOperations，其余共享 SECURE_FALLBACK）

oidcUserService(...)
    └─ SecurityUserOidcUserService extends OidcUserService，loadUser 里 assemble(super.loadUser(...))
         └─ setOauth2UserService(oauth2UserService())   复用上面那个路由 lambda，取的是"不装配"的原始声明
```

约束一：Spring Security 5.7.11 中 `OidcUserService` 与 `DefaultOAuth2UserService` **没有继承关系**。

```java
public class DefaultOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User>
public class OidcUserService         implements OAuth2UserService<OidcUserRequest, OidcUser>
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService = new DefaultOAuth2UserService();
    public final void setOauth2UserService(...)
```

同名接口、不同泛型参数，两者互不可赋值；`OidcUserService` 是**组合**了一个 `DefaultOAuth2UserService`。`OidcUserRequest extends OAuth2UserRequest`。

约束二：`OidcUserService.loadUser` 只在 `shouldRetrieveUserInfo(userRequest)` 为真时才调用内部的 `routingOauth2UserService`（userinfo URI 为空、或 access token 的 scope 不含 profile/email/address/phone 时整体跳过）。跳过后主体完全由 ID Token 声明构成。

约束三：`DefaultOAuth2UserService.setRestOperations` 是 `public final`，且 RestOperations 是实例字段。per-client 的 SSL 差异**只能靠多个实例**表达，无法由单个子类实例内部切换。

约束四：装配点分三处（`SsoSecurityUserConverter` / `SecurityUserOidcUserService` / `SecurityUserSamlAuthenticationConverter`）是 `docs/说明文档/已废弃实现与决策记录.md` 第六节在 2026-10-18 定下的结论，不是历史遗留。SAML 那一处不能并入本次统一：`Saml2AuthenticatedPrincipal.getAttributes()` 返回 `Map<String, List<Object>>`，与 `OAuth2AuthenticatedPrincipal` 的 `Map<String, Object>` 在同一类内无法兼容。

## Goals / Non-Goals

**Goals:**

- 两个 lambda 归零，"谁发 HTTP"与"谁换主体"各由一个类名表达。
- `Oauth2Config` 不再持有 SSL 兜底策略与查表逻辑，只剩 `@Bean` 声明、`ClientRegistration` 构造与过滤器链接线。
- 装配逻辑收成一个方法。

**Non-Goals:**

- 不改变任何可观察行为，因此不产生 spec 增量（`.openspec.yaml` 已置 `skip_specs: true`）。
- 不改造 per-client SSL 的表达方式（见约束三，改成单一实例动态切换要动 `RestOperations` 的构造时机，属于另一次变更）。
- 不动 SAML2 侧、不动 `ignore-ssl` 的配置契约与告警措辞。
- 不新增第三方依赖，不升级 Spring Security。

## Decisions

### D1：拆成 `RoutingOAuth2UserService` + `SecurityUserOAuth2UserService`，而不是把装配塞进 `DefaultOAuth2UserService` 的子类

**采用（具名类，路由与装配各一个）：**

```
RoutingOAuth2UserService            implements OAuth2UserService<OAuth2UserRequest, OAuth2User>
   持有 registrationId -> 具体 service 的映射 + SECURE_FALLBACK
   loadUser = resolve(registrationId).loadUser(userRequest)          ← 只发 HTTP，不认识 SecurityUser

SecurityUserOAuth2UserService       implements OAuth2UserService<OAuth2UserRequest, OAuth2User>
   组合一个 RoutingOAuth2UserService + SecurityUserSsoAssembler
   loadUser = assembler.assemble(router.loadUser(userRequest))        ← OAuth2 端点用这个

SecurityUserOidcUserService         extends OidcUserService            （保持现状）
   setOauth2UserService(未装配的 RoutingOAuth2UserService)
   loadUser = assembler.assemble(super.loadUser(userRequest))
```

`RoutingOAuth2UserService` 收编 `Oauth2Config` 现有的 `SECURE_FALLBACK`、`buildUserServiceMap()`、`resolveUserService()` 三个成员。`SecurityUserOidcUserService` 与 `SecurityUserOAuth2UserService` 以类型名对称，一眼能看出各协议链路末端各有一次装配。

**为什么装配必须在路由之外**：若让 `RoutingOAuth2UserService` 自己装配（即用户最初设想的"在 `DefaultOAuth2UserService` 这一层就用自有实现并顺手装配"），OIDC 链路会连累两次：

1. 该实例同时是 `OidcUserService` 的内部委托，`OidcUserService` 只取它的 `getAttributes()` 再另造一个 `DefaultOidcUser`，于是外层装配仍要保留 —— 本地账号库**每次登录查两遍**。
2. 内层装配抛的 `LOCAL_USER_NOT_FOUND` 会抢在 `OidcUserService` 的 `sub` 一致性校验之前发生，失败原因被排序变化掩盖。
3. 由约束二，`shouldRetrieveUserInfo` 为假时内层根本不执行，装配无论如何都得留在外层 —— 于是装配逻辑变成"里外各一份"，与本变更的目标相反。

**备选并否决**：让 `SecurityUserOidcUserService extends SecurityUserOAuth2UserService`。受约束一 + Java 单继承限制，`SecurityUserOidcUserService` 必须继承 `OidcUserService` 才能保住 ID Token 校验、nonce 校验与声明合并，无法同时继承 OAuth2 侧的类。真要继承，就得自己重写 OIDC 校验逻辑，是安全能力的净损失。

### D2：`SsoSecurityUserConverter` 保留类名，两个 `assemble` 重载合并为一个

`OidcUser extends OAuth2User`，因此单方法即可覆盖两条协议：

```java
public Oauth2SecurityUser assemble(OAuth2User source) {
    SecurityUser localUser = loadLocalUser(source.getName());
    OidcIdToken idToken = null;
    OidcUserInfo userInfo = null;
    if (source instanceof OidcUser) {
        idToken = ((OidcUser) source).getIdToken();
        userInfo = ((OidcUser) source).getUserInfo();
    }
    return Oauth2SecurityUser.of(localUser, source.getAttributes(), idToken, userInfo);
}
```

类名保留是因为决策文档第六节按这个名字记录了装配点，改名会让那条结论失去指向。这里删不掉这个类：受 D1 的"必须在链路末端各装配一次"约束，它是两个 Service 类之间唯一能共享的载体（两个类各自已经继承了/实现了不同的协议类型，没有公共父类可放）。

### D2.1：容器里 `OAuth2UserService` 类型的 Bean 保持只有一个，且仍是**不装配**的那个

现状是 `oauth2UserService()` 这个 `public @Bean` 返回"只路由、不装配"的 lambda，`mappedUserService()` 是 private 工厂、不入容器（其 javadoc 写明"不注册为 Bean：容器里 `OAuth2UserService` 类型的实例保持单一"）。

重构后维持这一容器契约：

- `oauth2UserService()` 仍是 `public @Bean`，返回类型与语义不变（`RoutingOAuth2UserService`，只路由不装配）——外部应用若注入过这个 Bean，拿到的行为与今天一致。
- `SecurityUserOAuth2UserService` 与今天的 `mappedUserService()` 一样是 private 工厂产出，**不注册为 Bean**，只接到 `userInfoEndpoint.userService(...)` 上。

否决的备选：把装配后的那个设为 Bean。那会让同类型出现两个 Bean（路由 + 装配），注入 `OAuth2UserService` 的地方产生歧义，且改变了已入容器的 Bean 行为。

### D3：测试删除重写，但判定手法保留

`Oauth2UserServiceWiringTest`、`SsoSecurityUserConverterTest` 删除后按新类结构重写。保留原测试"不发真实 HTTP 请求"的判定思路：`RestOperationBuilder` 构造的 `RestTemplate` 只装了 2 个消息转换器，默认的是全套，据此反推 `RestOperations` 是否被替换。原测试 `assertEquals(DefaultOAuth2UserService.class, service.getClass())` 这类**精确类比对**要改成按"是否被替换过 RestOperations"判定，否则映射到 `RoutingOAuth2UserService` 内部的具体实例后失效。

三条必须继续成立的语义（已是 `identity/oauth2-login` 的既有 requirement，非本次新增）：

- `ignore-ssl` 默认值为执行证书校验；
- `ignore-ssl` 只作用于显式配置的那个客户端，不串到同应用内其他身份源；
- `registration-id` 未匹配时落到标准校验实例，失败方向是"登录不上"，不是"悄悄不校验"。

### D4：豁免范围不变

`RoutingOAuth2UserService` 只管 userinfo 端点。令牌交换仍用 `DefaultAuthorizationCodeTokenResponseClient`，OIDC 端点发现仍走 Spring 自带 `RestTemplate`（决策文档第二节记为"有意的不对称"）。迁移 SSL 逻辑时不要把 `ignore-ssl` 顺手接到这两处。

## Risks / Trade-offs

- [SSL 接线只有测试这一层证据，从未运行时验证] → 决策文档第二节"验证状态"明确写着运行时未验证，`Oauth2UserServiceWiringTest` 是**唯一**证据。删重写期间该能力处于"暂时无证据"状态：新测试必须先通过再提交，且不得把"测不过就改断言"当成出路；断言与实现冲突时以实现语义为准并停下来核对 `identity/oauth2-login` 的场景描述。
- [类数量不降反升] → 已知代价。收益是 lambda 归零与配置类职责收敛，`SsoSecurityUserConverter` 因 D2 必须保留。若判定"文件数必须减少"才算简洁，本方案不满足，应放弃变更。
- [多一层包装：`SecurityUserOAuth2UserService` 只做一次委托] → 接受。它承载的是"装配只发生一次"这条不变式，删掉它就会退回 D1 里被否决的形态。
- [`SECURE_FALLBACK` 从 `Oauth2Config` 的 `static final` 迁入新类] → 保持共享单例语义，不要改成每次查表 `new`，否则"未配置→安全实例"失去唯一指向，也更容易被误改成"回落任一已配置实例"。
- [`instanceof OidcUser` 合并重载] → 纯 OAuth2 客户端走到 `instanceof` 为假的一支，行为与原 `assemble(OAuth2User)` 完全一致；风险仅在于漏传 idToken/userInfo，由重写后的 OIDC 装配测试守住。

## Migration Plan

纯代码重构，无数据、无配置、无对外契约迁移。发布即常规构建。回滚：`git revert` 本次提交，无残留状态。
