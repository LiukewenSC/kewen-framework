# Proposal

## Why

账号密码登录后进入安全上下文的是 `SecurityUser`，而 OAuth2 / OIDC / SAML2 登录后进入安全上下文的是各协议自带的主体类型（`DefaultOAuth2User` / `DefaultOidcUser` / `DefaultSaml2AuthenticatedPrincipal`）。为了弥合这个差异，框架维护了一层"实体转换"：同一个 `Authentication` 要经三个转换器分别产出响应体与当前用户。

这层转换带来两个问题：

- 两条路径对同一份数据的取法不一致。SSO 的响应体取自身份源声明，而 `getCurrentUser()` 却回查本地库——同一个用户的展示信息和权限判定可能来自两个地方；且 SSO 下每次取当前用户都打一次库，账密却不打。
- 它从未真正跑通。按现有 sample 配置（映射字段为 `sub`，而 `sub` 是身份源侧的数字 ID，本地按 `username` 查库），SSO 登录成功后调用 `getCurrentUser()` 必然抛 `UsernameNotFoundException`；登录响应看起来正常，是因为它压根没走本地库。

现在正是收敛的时机：登录页与三种协议的接入都刚落地，还没有外部业务依赖这层转换器。

## What Changes

- 三种认证协议认证成功后，写入安全上下文的主体统一为框架的 `SecurityUser`；协议原生数据由 `SecurityUser` 的两个协议子类型承载，业务侧不再需要按协议分支。
- 单点登录"身份源主体 → 本地账号"的映射提前到 userinfo 拉取 / 断言校验阶段完成，映射结果复用现有 `SecurityUserDetailsService`，权限一律由本系统提供，与账密登录同源。
- **BREAKING** `SecurityUser.gender` 与登录响应里的 `gender` 由 `Integer` 改为 `String`：`OidcUser` 的父接口带来 `String getGender()`，不改类型就无法让 `SecurityUser` 的子类型实现它（详见 design D1b）。库里仍是 `tinyint`，转换只在 RBAC 用户详情服务一处。
- **BREAKING** 删除 `AuthenticationSuccessResultConverter` 接口及其三个实现与对应的 Bean 注册；需要定制映射的业务改为实现 `SecurityUserDetailsService`。
- 本地不存在对应账号时认证失败并给出明确错误码，沿用既有失败交付姿态回跳前端。
- 取当前登录用户不再每次查库；随之而来的语义变化是权限成为登录时快照，改权限需重新登录生效（与账密登录现有行为一致）。
- 单点登录会话的认证对象权限集合不再包含 `SCOPE_*` 与协议自带的 authority 对象，只包含本系统的权限码。
- **BREAKING** 升级后既有 SSO 会话不再被判定为已登录，需要重新登录（项目处于开发阶段，不做旧会话兼容）。
- sample 的 `user-name-attribute-name` 从 `sub` 改为身份源中真正等于本地用户名的声明字段。

## Capabilities

### New Capabilities

- `identity/authentication-principal`: 约束三种认证协议产出的认证主体类型、身份源主体到本地账号的映射责任归属、权限来源，以及协议原生数据在主体上的留存要求。

### Modified Capabilities

无。`identity/oauth2-login` 约束的是用户信息端点的证书校验，本次只替换该链路上的主体装配，校验行为与 `ignore-ssl` 分发不变；`identity/login-outcome-posture` 约束的是交付姿态（JSON 还是 302），本次不动判定依据与响应形态，只改响应体数据的来源。

## Impact

- `login-spring-boot-starter`：`oauth2`（用户服务、配置、新增子类型）、`saml`（认证转换器、新增子类型）、`response`（成功处理器与结果投影）、`SecurityUserContext`、`model/SecurityUser`、`config/SecurityBeanConfig`。
- 删除 4 个类：转换接口 + 账密/OAuth2/SAML 三个实现。新增 2 个协议子类型、1 个装配器、1 个 OIDC 用户服务、1 个 SAML 组装转换器、1 个错误码常量类。
- 跨模块影响：`../../../../auth` 的 `RbacSecurityUserDetailsService` 需要把库里 `Integer` 的 gender 转成 String。
- 对外契约：`LoginSuccessResult` 的字段名与含义不变，前端不需改动；`cu.security.login.oauth2.clients[*].user-name-attribute-name` 的含义变为"用于查本地账号的字段"。
- 配置与示例：`sample/login-boot-sample` 的 OAuth2/OIDC 客户端条目，以及 SAML 侧对身份源 NameID 取值的要求。
- 测试：`LoginDeliveryPostureTest`（构造入参含转换器提供者）、`Oauth2UserServiceWiringTest`。
- 文档：`docs/说明文档/已废弃实现与决策记录.md` 需要改写——它第一节记录的"不在认证链路里做转换"这条决策，其替代方案正是本次要删除的转换器层，而本 change 的方向与之相反（冲突的处理方式见 design.md）。
- 不引入新依赖，不改 Spring Security 版本。
