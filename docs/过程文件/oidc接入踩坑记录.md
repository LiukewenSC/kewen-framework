# Spring Security OIDC 对接踩坑记录

> **项目版本**：Spring Boot 2.7.18 / Spring Security 5.7.x / Java 8

---

## 一、OIDC 与 OAuth2 在 Spring Security 中的关系

OIDC 基于 OAuth2 构建，本质是在 OAuth2 授权码模式上增加：
- `openid` scope（必须）
- `id_token`（JWT 格式，用于身份认证）
- UserInfo Endpoint（标准用户信息获取）

Spring Security 中 **OIDC 通过 `oauth2Login()` 统一支持**，无需额外调用 `openidLogin()`（已在 5.7 废弃）。

### 架构职责分层

| 层级 | 配置位置 | 说明 |
|------|---------|------|
| **IDP 端点 URL** | `ClientRegistration`（在 `Oauth2OidcProperties` 中配置） | 各 IDP 自己的 authorization_uri、token_uri、userinfo_uri 等 |
| **本地行为策略** | `http.oauth2Login()` 的 `.authorizationEndpoint()/.tokenEndpoint()/.userInfoEndpoint()` | 本机拦截路径、HTTP 客户端实现、SSL 校验等 |

两者是**正交**的：前者是"通讯录"（每个 IDP 的号码），后者是"电话机"（怎么打电话）。

---

## 二、配置项分类

### 1. 字段归属表

| 字段 | 归属 | 必填条件 | 说明 |
|------|------|---------|------|
| `registrationId` | 通用 | ✅ | 客户端注册唯一标识 |
| `clientId` | 通用 | ✅ | IDP 分配的客户端 ID |
| `clientSecret` | 通用 | ✅ | 客户端密钥 |
| `clientAuthenticationMethod` | 通用 | 默认 basic | 令牌端点认证方式 |
| `authorizationGrantType` | 通用 | 默认 authorization_code | 授权类型 |
| `redirectUriTemplate` | 通用 | 默认值可用 | 回调地址模板 |
| `scopes` | 通用 | OIDC 自动补 openid | 权限范围 |
| `clientName` | 通用 | 可选 | 显示名称 |
| `ignoreSsl` | 通用 | 默认 false | 仅测试环境用 |

| `oidcDiscoveryUri` | **OIDC 专用** | 与手动配置互斥 | IDP 基础地址，自动发现所有端点 |
| `configurationMetadata` | **OIDC 专用** | 系统自动填充 | Discovery 文档中的其他元数据 |

| `authorizationUri` | **OAuth2 手动配置** | 未配 oidcDiscoveryUri 时必填 | 授权端点 |
| `tokenUri` | **OAuth2 手动配置** | 未配 oidcDiscoveryUri 时必填 | 令牌端点 |
| `userInfoUri` | **OAuth2 手动配置** | 未配 oidcDiscoveryUri 时必填 | 用户信息端点 |
| `userInfoAuthenticationMethod` | **OAuth2 手动配置** | 可选，默认 header | 用户信息认证方式 |
| `userNameAttributeName` | **OAuth2 手动配置** | 未配 oidcDiscoveryUri 时必填 | 用户名属性 |

| `jwkSetUri` | 通用（OIDC 自动发现） | 可选 | JWT 签名验证用 |

### 2. 两种模式互斥

| 模式 | 配置方式 | 说明 |
|------|---------|------|
| **OIDC 自动发现** | 只配 `oidcDiscoveryUri` | 自动拉取 Discovery 文档获取所有端点 |
| **OAuth2 手动配置** | 配 `authorizationUri` + `tokenUri` + `userInfoUri` | 手动指定各端点 |

**不能同时使用**，代码根据 `oidcDiscoveryUri` 是否为空判断走哪条分支。

---

## 三、踩坑记录

### 问题 1：`authorizationRequestResolver` 是否需要显式配置

**现象**：代码中配置了：
```java
.authorizationEndpoint(endpoint -> {
    endpoint.baseUri(oauth2OidcProperties.getSsoAuthorizationUri())
            .authorizationRequestRepository(authorizationRequestRepository())
            .authorizationRequestResolver(new DefaultOAuth2AuthorizationRequestResolver(
                    clientRegistrationRepository, oauth2OidcProperties.getSsoAuthorizationUri()));
})
```


**分析**：
- `baseUri` → 本机拦截路径前缀，与 `ClientRegistration.authorizationUri` 是两个不同概念
- `authorizationRequestRepository` → 默认就是 `HttpSessionOAuth2AuthorizationRequestRepository`，冗余
- `authorizationRequestResolver` → 传入的 `clientRegistrationRepository` 和 `baseUri` 已在其他地方配置，Spring Security 内部会自动组合，**冗余**

**结论**：可以简化为：
```java
.authorizationEndpoint(endpoint -> {
    endpoint.baseUri(oauth2OidcProperties.getSsoAuthorizationUri());
})
```


只有需要自定义请求构建逻辑（如加额外参数、改 state 生成方式）时才需要自己实现 `authorizationRequestResolver`。

### 问题 2：`Could not extract response: no suitable HttpMessageConverter found` + `content type [text/html]`

**现象**：启动时报错，`ClientRegistrations.fromIssuerLocation()` 失败，响应内容为 `text/html` 而非 `application/json`。

**根因**：配置了完整的 Discovery URL 而非 IDP 基础地址：
```yaml
# ❌ 错误
issuer-uri: http://localhost:9010/oidc/.well-known/openid-configuration
```


`fromIssuerLocation()` 内部会自动拼接 `/.well-known/openid-configuration`，导致实际请求：
```
http://localhost:9010/oidc/.well-known/openid-configuration/.well-known/openid-configuration
```

路径不存在 → IDP 返回 HTML 错误页 → 解析失败。

**正确配置**：
```yaml
# ✅ 正确
issuer-uri: http://localhost:9010/oidc
```


### 问题 3：`tokenEndpoint` / `userInfoEndpoint` 是否需要配置

**分析**：
- `tokenEndpoint().accessTokenResponseClient()` → 配置令牌交换用什么 HTTP 客户端发请求，**不配置 IDP 端点地址**。默认 `DefaultAuthorizationCodeTokenResponseClient` 即可
- `userInfoEndpoint().userService()` / `.oidcUserService()` → 配置获取用户信息的 HTTP 客户端实现（如跳过 SSL 校验）。如果不需要自定义 SSL，也可省略

**结论**：只有需要自定义 HTTP 行为（如跳过 SSL 校验、自定义错误处理）时才需要显式配置。

### 问题 4：`BadJwtException: Signed JWT rejected`

**现象**：
```
org.springframework.security.oauth2.jwt.BadJwtException: Signed JWT rejected: 
Another algorithm expected, or no matching key(s) found
    at org.springframework.security.oauth2.jwt.NimbusJwtDecoder.process(...)
    at OidcAuthorizationCodeAuthenticationProvider.getJwt()
```


**问题根因**：**非标准 OIDC Provider 的 JWKS 端点需要携带 `client_id` 参数才能返回对应公钥**

| 对比项 | 标准 OIDC | 你的 IdP |
|--------|-----------|----------|
| Discovery 返回的 `jwks_uri` | `http://localhost:9010/oidc/jwks` | 相同 |
| 实际可用的 JWKS URL | 同上（直接访问即可） | 必须拼接：`http://localhost:9010/oidc/jwks?client_id=xxx` |
| `RemoteJWKSet` 默认行为 | 直接访问 `jwks_uri` | 访问 `jwks_uri`（不带参数）→ 返回空密钥列表 → `selectKeys()` 返回空 |

**核心机制**：

Spring Security 的 `NimbusJwtDecoder` 内部使用 nimbus-jose-jwt 的 `RemoteJWKSet` 拉取 JWKS：

```java
// RemoteJWKSet 默认逻辑
JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(jwkSetUri));
List<JWK> keys = jwkSource.get(JWKSelector.from(header), context);

// JWSVerificationKeySelector.selectKeys() 匹配条件：
// 1. JWT header 的 alg 必须在期望集合中（RS256）
// 2. 用 JWKMatcher 过滤 JWKS：
//    - 如果 JWT header 有 kid，只匹配 kid 相同的 JWK
//    - 如果 JWT header 有 alg，只匹配 alg 相同的 JWK
//    - 只匹配 use="sig" 的 JWK
```


**由于 `jwkSetUri` 不带 `client_id`，Provider 返回空 JWKS，导致 `selectKeys()` 找不到任何匹配密钥。**

**错误排查路径**：
1. **误判方向 1**：以为是 Spring Security 版本 API 问题（尝试用 `OidcIdTokenDecoderFactory.setJwtDecoderFactory()`）
    - **失败原因**：该方法是 Spring Security 5.8+ 才引入的，5.7.x 不存在
2. **误判方向 2**：尝试匿名内部类覆盖 `OidcAuthorizationCodeAuthenticationProvider.getJwt()`
    - **失败原因**：方法签名和基类不匹配，5.7.x 不允许这种扩展方式
3. **误判方向 3**：在 `createOidcClientRegistration` 中硬编码拼接 `client_id`
    - **问题**：会影响所有其他 IdP 的对接，破坏框架通用性
4. **正确方案**：手动配置 `jwk-set-uri` 覆盖自动发现结果

**最终解决方案**（配置层面解决，不改代码）：

```yaml
kewen:
  security:
    login:
      oauth2:
        clients:
          - registration-id: kewen-local-oidc-registration
            protocol-type: oidc
            client-id: client_7f7c827b91224a9d
            client-secret: b54258d7b3e643b489859dec2eec95aaa16c1c701fd4470182b3d0e9926426c1
            issuer-uri: http://localhost:9010/oidc
            # 非标准 IdP：手动覆盖 jwk-set-uri
            jwk-set-uri: http://localhost:9010/oidc/jwks?client_id=client_7f7c827b91224a9d
```


**为什么这样改最好：**

| 优点 | 说明 |
|------|------|
| 不需要改代码 | 保持框架通用性，不影响其他 IdP |
| 配置即文档 | 其他开发者一眼就能看出这个 IdP 的特殊性 |
| 兼容所有版本 | 不依赖 Spring Security 版本（5.7+、6.x 都适用） |
| 避免 URL 编码问题 | 框架内部会正确处理 URL 拼接 |

**验证方法**：
启动应用后，在日志中可以看到：
```
[OIDC] Overriding JWKS URI: http://localhost:9010/oidc/jwks -> http://localhost:9010/oidc/jwks?client_id=client_7f7c827b91224a9d
```

或者直接终端验证：
```bash
# 不带参数（返回空或不完整的 JWKS）
curl http://localhost:9010/oidc/jwks

# 带参数（返回正确的公钥）
curl http://localhost:9010/oidc/jwks?client_id=client_7f7c827b91224a9d
```

### 问题 5：`invalid_nonce`

**现象**：
```
OAuth2AuthenticationException: [invalid_nonce] Nonce mismatch
```


**问题根因**：**OIDC 规范要求**：当授权请求携带 `nonce` 参数时，ID Token 必须原样返回相同的 `nonce` 声明。

**你的 IdP 是非标准实现**：没有返回 `nonce` 声明，导致 Spring Security 的 `OidcIdTokenValidator` 校验失败。

**Nonce 的完整流程**：

1. **发起授权请求时**：`OidcAuthorizationRequestResolver` 生成随机 `nonce`，存入 `OAuth2AuthorizationRequest.additionalParameters`，并保存到 **Session**
2. **IDP 回调时**：从 Session 取出 `OAuth2AuthorizationRequest`，获取保存的 `nonce`
3. **验证 ID Token**：对比 ID Token Claims 中的 `nonce` 与保存的 `nonce`
4. **不匹配或缺失时**：抛出 `invalid_nonce` 异常

**排查步骤**：

#### 步骤 1：打印 ID Token 的 Claims

在 [jwt.io](https://jwt.io) 或使用命令行解码：
```bash
# 解码 ID Token（不验证签名）
echo "YOUR_ID_TOKEN" | cut -d'.' -f2 | base64 -d | jq
```

查看 `nonce` 字段是否存在：
```json
{
  "sub": "1234567890",
  "iss": "http://localhost:9010/oidc",
  "aud": "client_7f7c827b91224a9d",
  "exp": 1234567890,
  "iat": 1234567890,
  "nonce": "xxxxx"  ← 这个字段是否存在？
}
```


#### 步骤 2：检查 Session 是否丢失

在浏览器 Network 面板查看：

1. 发起授权请求时的 `Set-Cookie: JSESSIONID=xxx`
2. IDP 回调时是否携带了 `Cookie: JSESSIONID=xxx`

**如果没有携带**：说明是跨域/代理/负载均衡导致的 Session 丢失（不是 `nonce` 本身的问题）。

#### 步骤 3：在 Debugger 中对比

在 `OidcAuthorizationCodeAuthenticationProvider.authenticate()` 打断点：
```java
// Evaluate Expression
OAuth2AuthorizationRequest authRequest = ...; // 从 Session 获取
String savedNonce = authRequest.getAdditionalParameters().get("nonce");

Jwt jwt = ...; // 解析后的 ID Token
String tokenNonce = jwt.getClaim("nonce");

System.out.println("Saved nonce:    " + savedNonce);
System.out.println("Token nonce:    " + tokenNonce);
```


**最终解决方案**：

#### 方案 1：让 IdP 返回 `nonce`（推荐）

修改 IdP 的 ID Token 生成逻辑，确保授权请求携带 `nonce` 时，ID Token 必须原样返回。

#### 方案 2：禁用 `nonce` 生成（仅调试，不安全）

自定义 `OAuth2AuthorizationRequestResolver`，不生成 `nonce`：
```java
// 自定义 AuthorizationRequestResolver
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
        if (req != null) {
            Map<String, Object> additionalParams = new HashMap<>(req.getAdditionalParameters());
            additionalParams.remove("nonce");  // 移除 nonce
            return OAuth2AuthorizationRequest.from(req)
                    .additionalParameters(additionalParams)
                    .build();
        }
        return null;
    }
}
```


**⚠️ 生产环境不建议这样做**：会引入重放攻击风险。

---

## 四、配置示例

### OIDC 自动发现（推荐）

```yaml
kewen:
  security:
    login:
      oauth2:
        clients:
          - registration-id: my-oidc-idp
            client-id: your-client-id
            client-secret: your-client-secret
            oidc-discovery-uri: http://localhost:9010/oidc  # ← 注意这里是基础地址
            scopes:
              - openid
              - profile
              - email
            client-name: My OIDC Provider
```


### OAuth2 手动配置

```yaml
kewen:
  security:
    login:
      oauth2:
        clients:
          - registration-id: my-oauth2-idp
            client-id: your-client-id
            client-secret: your-client-secret
            authorization-uri: https://idp.example.com/authorize
            token-uri: https://idp.example.com/token
            user-info-uri: https://idp.example.com/userinfo
            user-name-attribute-name: login
            scopes:
              - read
              - profile
```


---

## 五、经验总结

### 5.1 非标准 OIDC Provider 的对接策略

| 问题 | 标准 OIDC 行为 | 非标准 IdP 问题 | 解决思路 |
|------|----------------|----------------|----------|
| **Discovery URL** | 配置基础地址即可 | 容易配置成完整 URL | 只配置基础地址，框架自动拼接 |
| **JWKS 端点** | `jwks_uri` 直接访问即可 | 需要额外参数（如 `client_id`） | 手动配置 `jwk-set-uri` 覆盖自动发现 |
| **Nonce 校验** | ID Token 必须包含 `nonce` | 不返回 `nonce` | 让 IdP 修复或临时禁用校验 |
| **Discovery 文档** | 符合 OIDC 规范 | 可能缺少某些字段 | 手动配置缺失的端点 |

### 5.2 Spring Security OIDC 配置最佳实践

1. **优先使用 `issuer-uri` 自动发现**：减少手动配置，保持标准化
2. **非标准 IdP 用配置覆盖**：通过 `jwk-set-uri`、`authorization-uri` 等手动配置项覆盖自动发现结果
3. **避免修改框架代码**：配置即文档，便于维护和交接
4. **保留默认行为**：不要破坏其他 IdP 的对接逻辑
5. **注意配置项的互斥性**：`oidcDiscoveryUri` 与手动端点配置不能同时使用

### 5.3 调试技巧

| 调试目标 | 方法 |
|----------|------|
| **查看 Discovery 文档** | `curl http://localhost:9010/oidc/.well-known/openid-configuration \| jq` |
| **查看 JWKS 内容** | `curl http://localhost:9010/oidc/jwks?client_id=xxx \| jq` |
| **解码 ID Token** | [jwt.io](https://jwt.io) 或 `echo TOKEN \| cut -d'.' -f2 \| base64 -d \| jq` |
| **检查 Session** | 浏览器 Network 面板对比请求的 `JSESSIONID` |
| **查看 Spring Security 内部状态** | 在 `OidcAuthorizationCodeAuthenticationProvider.authenticate()` 打断点 |
| **开启调试日志** | `logging.level.org.springframework.security=DEBUG` |

### 5.4 排查清单

当 OIDC 自动发现失败时，按以下步骤排查：

1. **确认 `oidcDiscoveryUri` 是 IDP 基础地址**，不是完整 Discovery URL
2. **用 curl 验证 Discovery 端点**：
   ```bash
   curl -v http://localhost:9010/oidc/.well-known/openid-configuration
   ```

3. **检查 IDP Discovery 文档中 `issuer` 字段是否与配置一致**
4. **检查是否需要 SSL 跳过**（测试环境 `ignoreSsl: true`）
5. **验证 JWKS 端点是否可访问**（特别是非标准 IdP 可能需要额外参数）
6. **检查 ID Token 是否包含 `nonce` 声明**

---

## 六、配置文件示例

```yaml
kewen:
  security:
    login:
      oauth2:
        # 本地 OAuth2/OIDC 登录授权端点的基础 URI
        sso-authorization-uri: /oauth2/authorize
        # 登录回调处理端点
        login-processing-url: /login/oauth2/code/{registrationId}
        
        clients:
          # 标准 OIDC Provider（自动发现）
          - registration-id: standard-oidc
            protocol-type: oidc
            client-id: xxx
            client-secret: yyy
            issuer-uri: https://standard-idp.example.com/oidc
            
          # 非标准 OIDC Provider（手动覆盖 JWKS）
          - registration-id: custom-oidc
            protocol-type: oidc
            client-id: client_7f7c827b91224a9d
            client-secret: b54258d7b3e643b489859dec2eec95aaa16c1c701fd4470182b3d0e9926426c1
            issuer-uri: http://localhost:9010/oidc
            # 非标准：需要拼接 client_id
            jwk-set-uri: http://localhost:9010/oidc/jwks?client_id=client_7f7c827b91224a9d
```


---

## 七、参考资料

- [OpenID Connect Core 1.0 - ID Token](https://openid.net/specs/openid-connect-core-1_0.html#IDToken)
- [Spring Security OAuth2 Client](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/index.html)
- [Nimbus JOSE + JWT](https://connect2id.com/products/nimbus-jose-jwt)
- [OIDC Discovery 规范](https://openid.net/specs/openid-connect-discovery-1_0.html)

---

*最后更新时间：2026-05-10*
