# Tasks

## 1. 路由逻辑收进具名类

- [x] 1.1 新建 `com.cu.framework.authentication.oauth2.RoutingOAuth2UserService`（`implements OAuth2UserService<OAuth2UserRequest, OAuth2User>`），把 `Oauth2Config` 的 `SECURE_FALLBACK`、`buildUserServiceMap()`、`resolveUserService()` 三个成员迁入，`loadUser` 只做“按 registrationId 查表再委托”，不含任何装配；`SECURE_FALLBACK` 保持共享单例（迁入后不得改成每次 `new`）；验证 `mvn -q -pl login-security-spring-boot-starter -am compile` 通过
- [x] 1.2 `Oauth2Config.oauth2UserService()` 的 `public @Bean` 改为返回 `RoutingOAuth2UserService`，Bean 的返回类型与“只路由不装配”语义与现状一致；验证 `Oauth2Config` 中不再出现 `buildUserServiceMap` / `resolveUserService` / `SECURE_FALLBACK`，且 `mappedUserService()` 尚未删除（编译仍通过）
- [x] 1.3 删除 `src/test/java/.../oauth2/Oauth2UserServiceWiringTest.java` 并按 `RoutingOAuth2UserService` 重写，覆盖 design D3 列出的三条语义：`ignore-ssl` 默认为执行证书校验、豁免只作用于显式配置的客户端不串用、`registration-id` 未匹配时落到标准校验实例；“是否被替换过 RestOperations”的判定改用转换器数量反推（`RestOperationBuilder` 构造的 `RestTemplate` 只有 2 个消息转换器），不得再用 `assertEquals(DefaultOAuth2UserService.class, service.getClass())` 这类精确类比对；验证 `mvn -q -pl login-security-spring-boot-starter test -Dtest=*RoutingOAuth2UserService*` 通过，且三条断言各自独立可失败（临时改动实现能分别使对应断言变红）

## 2. 装配点收敛

- [x] 2.1 `SsoSecurityUserConverter` 的 `assemble(OAuth2User)` 与 `assemble(OidcUser)` 合并为单个 `assemble(OAuth2User)`，ID Token 与 UserInfo 用 `instanceof OidcUser` 取出，`loadLocalUser` 与 `LOCAL_USER_NOT_FOUND` 错误码语义不变；验证编译通过，且 `SecurityUserOidcUserService` 与 `Oauth2Config` 的调用点无残留的两参数重载引用
- [x] 2.2 删除 `SsoSecurityUserConverterTest` 并重写为针对合并后单方法的用例，三条：纯 OAuth2 主体装配后 `getIdToken()` 为 null 且权限来自本地账号、OIDC 主体装配后 ID Token 实例原样保留（`assertSame`）且 `attributes` 未丢、本地无账号时抛 `OAuth2AuthenticationException` 且错误码为 `SsoErrorCodes.LOCAL_USER_NOT_FOUND`；沿用现有手写 stub（`OnlyAdminUserDetailsService` 风格）不引入新测试依赖；验证 `mvn -q -pl login-security-spring-boot-starter test -Dtest=SecurityUserSsoAssemblerTest` 通过
- [x] 2.3 新建 `SecurityUserOAuth2UserService`（`implements OAuth2UserService<OAuth2UserRequest, OAuth2User>`），组合一个 `RoutingOAuth2UserService` 与 `SsoSecurityUserConverter`，`loadUser` = 委托路由结果再装配一次；按 design D2.1 **不注册为 Bean**；验证用一个计数型 stub 断言单次 `loadUser` 只触发一次本地账号查询（这是 D1 否决“装配塞进路由类”的直接守卫）

## 3. OIDC 链路与配置类收敛

- [x] 3.1 `Oauth2Config.oidcUserService()` 的 `setOauth2UserService(...)` 改为注入**未装配**的 `RoutingOAuth2UserService`，`SecurityUserOidcUserService` 自身不改（仍 `extends OidcUserService`，沿用其 ID Token/nonce/sub 校验），装配仍只在其 `loadUser` 外层发生一次；验证代码走查确认 OIDC 路径上不存在两处 `assemble` 调用，且不存在把 `SecurityUserOAuth2UserService` 交给 `setOauth2UserService` 的写法
- [x] 3.2 删除 `Oauth2Config` 的 `mappedUserService()` 私有 lambda，`userInfoEndpoint.userService(...)` 改接 `SecurityUserOAuth2UserService`；`Oauth2Config` 收敛为只含 `@Bean` 声明、`ClientRegistration` 构造与过滤器链接线，两个 lambda 归零；验证 `Oauth2Config.java` 中不再出现 `-> {` 形式内联的 `OAuth2UserService` lambda，`userRequest ->` 出现次数为 0
- [x] 3.3 补齐新类的类注释与 `@author kewen` / `@since` git 创建日期，注释只写能从代码确证的内容；把 design D1 里“OIDC 不能继承 OAuth2 service（单继承 + `OidcUserService` 是组合而非继承）”和 D1“装配必须在路由之外（会查两遍库、抢 `sub` 校验顺序、userinfo 被跳过时失效）”这两条结论，各追加一行指向到 `docs/说明文档/已废弃实现与决策记录.md` 第六节现有条目之下；验证文档新增段落与 design.md 表述不矛盾，且不改动该文档任何既有小节

## 4. 集成检查

- [x] 4.1 `mvn -q -pl login-security-spring-boot-starter test` 全绿；`Oauth2SecurityUserTest` 等未涉及用例无回归
- [x] 4.2 用 `sample/login-boot-sample` 起本地服务，账密登录与 OIDC（本地 IDP，端口 9010）各走一次，确认登录成功、`getCurrentUser()` 返回本地账号权限、响应体字段集合与重构前一致；SSL 豁免路径无法实测（决策文档第二节已注明运行时未验证），只确认未配置 `ignore-ssl` 时行为不变，不要为此放宽校验或改动默认值
  - 实跑记录（JDK8 + jar 直启，8080 由本次构建的新代码提供）：账密 `POST /login` 返回 `admin/管理员/[ROLE_ADMIN,ROLE_USER]`；`demo-oauth2`（走新增的 `SecurityUserOAuth2UserService`）与 `demo-oidc`（走 `SecurityUserOidcUserService`）各完成一次浏览器授权码流程，回调后 `/api/me` 均为 `admin/管理员/[ROLE_ADMIN,ROLE_USER]`，即权限来自本地账号而非 IDP 的 `SCOPE_read`/`SCOPE openid`。两条协议都 landed 在 `result-url`（`/login.html`），`loginState` 由 `login.js` 读取后 `replaceState` 抹掉，属既有行为。
  - 本任务原文只要求账密 + OIDC，实际把 `demo-oauth2` 也跑了 —— 新增类只服务纯 OAuth2 端点，不跑就等于没验。
  - 仍未运行时验证的部分：`ignore-ssl` 豁免路径（照旧只有 `RoutingOAuth2UserServiceTest` 的接线证据）；本系统无对应账号时 SSO 失败路径（由 `SsoSecurityUserConverterTest` / `SecurityUserOAuth2UserServiceTest` 覆盖，未在浏览器里复现，避免动 IDP 上他人的会话）。
- [x] 4.3 `git diff --stat` 确认变更只落在 `login-security-spring-boot-starter` 的 oauth2 包与其测试、`docs/说明文档/已废弃实现与决策记录.md` 的追加段落；未触碰 SAML2 侧、`Oauth2OidcProperties` 配置契约、令牌端点与 OIDC 端点发现的证书校验路径
