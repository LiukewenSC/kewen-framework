# IDaaS 认证接入文档

本文档详细介绍 qy-idaas 模块提供的 OAuth2 和 SAML 2.0 两种认证协议的配置方法。

## 目录
- [概述](#概述)
- [OAuth2 认证配置](#oauth2-认证配置)
- [SAML 认证配置](#saml-认证配置)
- [常见问题](#常见问题)

---

## 概述

qy-idaas 模块基于 Spring Security 框架，提供企业级单点登录（SSO）解决方案，支持以下认证协议：

- **OAuth2**：基于授权码模式（Authorization Code）的标准 OAuth2 认证
- **SAML 2.0**：基于 SAML 协议的企业级单点登录

### 核心端点说明

| 协议 | 端点类型 | 默认路径 | 说明 |
|------|---------|---------|------|
| OAuth2 | 授权端点 | `/oauth2/authorize` | 发起 OAuth2 认证请求 |
| OAuth2 | 回调端点 | `/login/oauth2/code/{registrationId}` | OAuth2 认证成功回调 |
| SAML | 登录端点 | `/login/saml2/sso/{registrationId}` | SAML SP 发起认证请求 |
| SAML | 登出端点 | `/logout/saml2/slo` | SAML 单点登出 |

---

## OAuth2 认证配置

### 配置详解

OAuth2 认证基于 **授权码模式（Authorization Code）**，适用于 Web 应用的安全认证。

#### 完整配置示例

```yaml
kewen:
  security:
    login:
      oauth2:
        # OAuth2 全局配置
        authorization-uri: /oauth2/authorize          # 发起认证的端点（默认值）
        login-processing-url: /login/oauth2/code/{registrationId}  # 回调处理端点（默认值）
        
        # 客户端配置列表（支持多个 OAuth2 提供商）
        clients:
          - registration-id: idp4-test                # 必填：客户端注册ID（唯一标识）
            client-id: your-client-id                 # 必填：从 IdP 获取的客户端ID
            client-secret: your-client-secret         # 必填：从 IdP 获取的客户端密钥
            client-name: IDP4 Test Provider           # 可选：客户端名称（用于日志）
            
            # 认证相关配置
            client-authentication-method: basic       # 客户端认证方式（默认：basic）
            authorization-grant-type: authorization_code  # 授权类型（默认：authorization_code）
            
            # 端点配置（必填项）
            authorization-uri: https://idp.example.com/oauth/authorize    # 授权端点
            token-uri: https://idp.example.com/oauth/token                # 令牌端点
            user-info-uri: https://idp.example.com/api/userinfo           # 用户信息端点
            
            # 用户信息配置
            user-name-attribute-name: sub             # 必填：用户名对应的属性名
            user-info-authentication-method: header   # 用户信息认证方式（默认：header）
            
            # 回调配置
            redirect-uri-template: "{baseUrl}/login/oauth2/code/{registrationId}"  # 回调地址模板（默认值）
            
            # 授权范围
            scopes:                                   # 必填：申请的权限范围
              - read
              - profile
            
            # 高级配置
            jwk-set-uri: https://idp.example.com/.well-known/jwks.json  # JWK Set URI（用于JWT验证）
            ignore-ssl: false                         # 是否忽略SSL证书验证（默认：false）
```

#### 配置项说明

| 配置项 | 必填 | 默认值 | 说明 |
|-------|------|--------|------|
| `registration-id` | ✅ | - | 客户端注册ID，用于标识不同的 OAuth2 提供商，在URL中使用 |
| `client-id` | ✅ | - | OAuth2 客户端ID，由 IdP 分配 |
| `client-secret` | ✅ | - | OAuth2 客户端密钥，由 IdP 分配 |
| `client-name` | ❌ | - | 客户端显示名称，用于日志和调试 |
| `client-authentication-method` | ❌ | `basic` | 客户端认证方式，可选：`basic`、`post`、`none` |
| `authorization-grant-type` | ❌ | `authorization_code` | 授权类型，目前仅支持授权码模式 |
| `authorization-uri` | ✅ | - | IdP 的授权端点URL，用户在此进行认证 |
| `token-uri` | ✅ | - | IdP 的令牌端点URL，用于获取 access_token |
| `user-info-uri` | ✅ | - | IdP 的用户信息端点URL，用于获取用户详细信息 |
| `user-name-attribute-name` | ✅ | - | 用户信息中用作用户名的属性名，如：`sub`、`email`、`username` |
| `user-info-authentication-method` | ❌ | `header` | 访问用户信息时的认证方式，可选：`header`、`form`、`query` |
| `redirect-uri-template` | ❌ | `{baseUrl}/login/oauth2/code/{registrationId}` | 认证成功后的回调地址模板 |
| `scopes` | ✅ | - | 申请的权限范围列表，常见值：`read`、`profile`、`email` |
| `jwk-set-uri` | ❌ | - | JSON Web Key Set 端点，用于验证 JWT 令牌 |
| `ignore-ssl` | ❌ | `false` | 是否忽略 SSL 证书验证（**仅测试环境使用**） |

### OAuth2 认证流程

```
┌─────────┐          ┌─────────┐          ┌─────────┐
│  浏览器  │          │  SP应用  │          │  IdP    │
└────┬────┘          └────┬────┘          └────┬────┘
     │                    │                    │
     │  1. 访问受保护资源   │                    │
     │───────────────────>│                    │
     │                    │                    │
     │  2. 重定向到授权端点 │                    │
     │<───────────────────│                    │
     │                    │                    │
     │  3. 跳转到 IdP 登录 │                    │
     │───────────────────────────────────────>│
     │                    │                    │
     │  4. 用户登录授权    │                    │
     │<───────────────────────────────────────│
     │                    │                    │
     │  5. 返回授权码      │                    │
     │───────────────────>│                    │
     │                    │                    │
     │  6. 用授权码换取令牌 │                    │
     │                    │───────────────────>│
     │                    │                    │
     │  7. 返回 access_token                   │
     │                    │<───────────────────│
     │                    │                    │
     │  8. 获取用户信息    │                    │
     │                    │───────────────────>│
     │                    │                    │
     │  9. 返回用户信息    │                    │
     │                    │<───────────────────│
     │                    │                    │
     │  10. 创建Session，返回登录结果          │
     │<───────────────────│                    │
```

### 多客户端配置

如果需要支持多个 OAuth2 提供商，可以配置多个客户端：

```yaml
kewen:
  security:
    login:
      oauth2:
        clients:
          # 第一个 OAuth2 提供商
          - registration-id: idp4-test
            client-id: client-id-1
            client-secret: secret-1
            authorization-uri: https://idp4-test.example.com/oauth/authorize
            token-uri: https://idp4-test.example.com/oauth/token
            user-info-uri: https://idp4-test.example.com/api/userinfo
            user-name-attribute-name: sub
            scopes:
              - read
          
          # 第二个 OAuth2 提供商
          - registration-id: github
            client-id: github-client-id
            client-secret: github-client-secret
            authorization-uri: https://github.com/login/oauth/authorize
            token-uri: https://github.com/login/oauth/access_token
            user-info-uri: https://api.github.com/user
            user-name-attribute-name: login
            scopes:
              - user:email
```

### 发起 OAuth2 认证

用户访问以下 URL 即可发起 OAuth2 认证：

```
https://your-app.com/oauth2/authorize/{registration-id}
```

例如：
```
https://your-app.com/oauth2/authorize/idp4-test
```

---

## SAML 认证配置

### 配置详解

SAML 2.0 是企业级单点登录的标准协议，支持 IdP 发起和 SP 发起两种认证模式。

#### 完整配置示例

```yaml
kewen:
  security:
    login:
      saml:
        # SAML 全局配置
        login-processing-url: /login/saml2/sso/{registrationId}  # SAML 回调端点（默认值）
        logout-request-url: /logout/saml2/slo                    # 登出请求端点（默认值）
        logout-response-url: /logout/saml2/slo                   # 登出响应端点（默认值）
        
        # SP 签名密钥库配置（可选，用于签名 AuthnRequest 和 LogoutRequest）
        sp-signing-keystore:
          keystore-resource: classpath:saml/sp-keystore.jks      # 密钥库文件路径
          keystore-password: changeit                            # 密钥库密码（默认值）
          key-alias: sp-signing                                  # 密钥别名（默认值）
          key-password: changeit                                 # 密钥密码（默认值）
        
        # SAML 注册配置列表（支持多个 IdP）
        registrations:
          - registration-id: idp4-test-registration              # 必填：注册ID（唯一标识）
            sp-entity-id: my-app-sp-entity-id                    # 必填：SP Entity ID
            
            # 方式一（推荐）：使用 IdP Metadata 自动解析
            metadata-resource: classpath:saml/metadata/idp-metadata.xml
            
            # 方式二（已废弃）：手动配置
            # 注意：当前版本仅支持 metadata 方式
```

#### 配置项说明

| 配置项 | 必填 | 默认值 | 说明 |
|-------|------|--------|------|
| **全局配置** ||||
| `login-processing-url` | ❌ | `/login/saml2/sso/{registrationId}` | SAML 断言消费服务（ACS）端点 |
| `logout-request-url` | ❌ | `/logout/saml2/slo` | SAML 单点登出请求端点 |
| `logout-response-url` | ❌ | `/logout/saml2/slo` | SAML 单点登出响应端点 |
| **SP 签名密钥库** ||||
| `sp-signing-keystore.keystore-resource` | ❌ | - | JKS 密钥库文件路径，用于存储 SP 私钥 |
| `sp-signing-keystore.keystore-password` | ❌ | `changeit` | 密钥库密码 |
| `sp-signing-keystore.key-alias` | ❌ | `sp-signing` | 私钥别名 |
| `sp-signing-keystore.key-password` | ❌ | `changeit` | 私钥密码 |
| **注册配置** ||||
| `registration-id` | ✅ | - | 注册ID，用于标识不同的 IdP 配置 |
| `sp-entity-id` | ✅ | `registration-id` 的值 | SP Entity ID，本应用的身份标识，需与 IdP 侧配置一致 |
| `metadata-resource` | ✅ | `classpath:saml/idp-metadata.xml` | IdP 的 metadata.xml 文件路径 |

### SAML 认证流程

#### SP 发起模式（推荐）

```
┌─────────┐          ┌─────────┐          ┌─────────┐
│  浏览器  │          │  SP应用  │          │  IdP    │
└────┬────┘          └────┬────┘          └────┬────┘
     │                    │                    │
     │  1. 访问受保护资源   │                    │
     │───────────────────>│                    │
     │                    │                    │
     │  2. 返回 AuthnRequest（SAML请求）        │
     │<───────────────────│                    │
     │                    │                    │
     │  3. 提交 SAMLRequest 到 IdP             │
     │───────────────────────────────────────>│
     │                    │                    │
     │  4. IdP 验证并展示登录页                 │
     │<───────────────────────────────────────│
     │                    │                    │
     │  5. 用户登录       │                    │
     │                    │                    │
     │  6. 返回 SAMLResponse（包含断言）        │
     │<───────────────────────────────────────│
     │                    │                    │
     │  7. 提交 SAMLResponse 到 SP             │
     │───────────────────>│                    │
     │                    │                    │
     │  8. 验证签名和断言  │                    │
     │                    │                    │
     │  9. 创建Session，返回登录结果            │
     │<───────────────────│                    │
```

#### IdP 发起模式

```
┌─────────┐          ┌─────────┐          ┌─────────┐
│  浏览器  │          │  IdP    │          │  SP应用  │
└────┬────┘          └────┬────┘          └────┬────┘
     │                    │                    │
     │  1. 直接访问 IdP   │                    │
     │───────────────────>│                    │
     │                    │                    │
     │  2. IdP 登录       │                    │
     │                    │                    │
     │  3. 返回 SAMLResponse                  │
     │<───────────────────│                    │
     │                    │                    │
     │  4. 提交到 SP ACS 端点                  │
     │───────────────────────────────────────>│
     │                    │                    │
     │  5. 验证并创建Session                   │
     │<───────────────────────────────────────│
```

### 如何获取 IdP Metadata

IdP Metadata 是一个 XML 文件，包含了 IdP 的配置信息（端点URL、证书等）。获取方式：

1. **从 IdP 管理员处获取**：联系 IdP 管理员下载 metadata.xml
2. **从 IdP 端点下载**：通常位于 `https://idp-domain.com/saml/metadata`
3. **验证证书有效期**：metadata 中包含的证书有有效期，过期需要更新

#### Metadata 文件示例结构

```xml
<?xml version="1.0" encoding="UTF-8"?>
<EntityDescriptor entityID="https://idp.example.com/saml"
                  xmlns="urn:oasis:names:tc:SAML:2.0:metadata">
    <IDPSSODescriptor protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol">
        <KeyDescriptor use="signing">
            <ds:KeyInfo xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
                <ds:X509Data>
                    <ds:X509Certificate>
                        <!-- IdP 签名证书（Base64编码） -->
                        MIIC...（很长的字符串）
                    </ds:X509Certificate>
                </ds:X509Data>
            </ds:KeyInfo>
        </KeyDescriptor>
        
        <!-- 单点登录服务端点 -->
        <SingleSignOnService 
            Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
            Location="https://idp.example.com/sso/saml"/>
        
        <!-- 单点登出服务端点 -->
        <SingleLogoutService 
            Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
            Location="https://idp.example.com/logout/saml"/>
    </IDPSSODescriptor>
</EntityDescriptor>
```

### 多 IdP 配置

支持配置多个 IdP，用户可以选择使用哪个 IdP 登录：

```yaml
kewen:
  security:
    login:
      saml:
        sp-signing-keystore:
          keystore-resource: classpath:saml/sp-keystore.jks
          keystore-password: changeit
          key-alias: sp-signing
          key-password: changeit
        
        registrations:
          # IdP 1: 测试环境
          - registration-id: idp4-test
            sp-entity-id: my-app-test
            metadata-resource: classpath:saml/idp4-test-metadata.xml
          
          # IdP 2: 生产环境
          - registration-id: idp4-prod
            sp-entity-id: my-app-prod
            metadata-resource: classpath:saml/idp4-prod-metadata.xml
          
          # IdP 3: 合作伙伴的 IdP
          - registration-id: partner-idp
            sp-entity-id: my-app-partner
            metadata-resource: classpath:saml/partner-idp-metadata.xml
```

### 发起 SAML 认证

#### SP 发起模式

用户访问以下 URL 即可发起 SAML 认证：

```
https://your-app.com/login/saml2/sso/{registration-id}
```

例如：
```
https://your-app.com/login/saml2/sso/idp4-test
```

#### IdP 发起模式

用户直接在 IdP 端选择访问你的应用，IdP 会自动将用户重定向到 SP 的 ACS 端点。

### SP 签名配置（可选）

如果需要 SP 对 AuthnRequest 和 LogoutRequest 进行签名（某些 IdP 要求），需要配置密钥库：

#### 1. 生成 JKS 密钥库

```bash
keytool -genkeypair \
  -alias sp-signing \
  -keyalg RSA \
  -keysize 2048 \
  -validity 3650 \
  -keystore sp-keystore.jks \
  -storepass changeit \
  -keypass changeit
```

#### 2. 导出证书给 IdP

```bash
keytool -exportcert \
  -alias sp-signing \
  -keystore sp-keystore.jks \
  -storepass changeit \
  -file sp-certificate.crt
```

#### 3. 将证书提供给 IdP 管理员

将导出的 `sp-certificate.crt` 提供给 IdP 管理员，用于验证 SP 的签名。

---

## 常见问题

### OAuth2 相关

### 1. OAuth2 相关

#### Q: `InsufficientAuthenticationException` 异常？

**A**: 表示未认证访问受保护资源。检查：
- 是否正确配置了 OAuth2 客户端信息
- 是否正确跳转到 IdP 进行认证
- Session 是否过期

#### Q: SSL 证书验证失败？

**A**: 测试环境可以临时关闭 SSL 验证（**生产环境严禁使用**）：

```yaml
kewen:
  security:
    login:
      oauth2:
        clients:
          - registration-id: test-idp
            ignore-ssl: true  # 仅测试环境
```

#### Q: 如何自定义回调地址？

**A**: 修改 `redirect-uri-template`：

```yaml
redirect-uri-template: "{baseUrl}/custom/oauth2/callback/{registrationId}"
```

注意：需要在 IdP 侧配置相同的回调地址。

#### Q: 支持哪些 OAuth2 授权类型？

**A**: 当前版本仅支持 `authorization_code`（授权码模式），这是最安全的 OAuth2 流程。

### 2. SAML 相关

#### Q: SAML Response 验证失败？

**A**: 常见原因：
- **Entity ID 不匹配**：检查 `sp-entity-id` 是否与 IdP 配置的 Audience 一致
- **证书过期**：检查 IdP metadata 中的证书是否过期
- **时间同步问题**：确保 SP 和 IdP 服务器时间同步（误差应 < 5分钟）

#### Q: 如何调试 SAML 问题？

**A**: 开启 Spring Security 调试日志：

```yaml
logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.security.saml2: DEBUG
```

#### Q: 是否支持 SAML 加密？

**A**: 当前版本支持签名，暂不支持加密断言。如需加密功能，请联系框架维护者。

#### Q: Metadata 更新后需要重启应用吗？

**A**: 是的，metadata 在应用启动时加载。更新后需要重启应用才能生效。

### 3. 通用问题

#### Q: 如何同时配置 OAuth2 和 SAML？

**A**: 直接在配置文件中同时配置两种方式：

```yaml
kewen:
  security:
    login:
      oauth2:
        clients:
          - registration-id: oauth2-idp
            # ... OAuth2 配置
      
      saml:
        registrations:
          - registration-id: saml-idp
            # ... SAML 配置
```

用户可以访问不同的端点选择认证方式。

#### Q: 如何实现自定义登录页面？

**A**: 创建控制器处理登录请求：

```java
@Controller
public class LoginController {
    
    @GetMapping("/login")
    public String loginPage() {
        return "custom-login";  // 返回自定义登录页面
    }
}
```

#### Q: 如何排除某些接口不需要认证？

**A**: 使用 `@SecurityIgnore` 注解：

```java
@RestController
@SecurityIgnore  // 整个 Controller 都不需要认证
public public PublicController {
    
    @GetMapping("/public-api")
    @SecurityIgnore  // 或者单个方法
    public String publicApi() {
        return "public";
    }
}
```

或者在配置文件中配置：

```yaml
kewen:
  auth:
    permit-url: /api/public/**;/health;/actuator/**
```

#### Q: Session 存储在哪里？

**A**: 默认存储在服务器内存中。如果需要分布式 Session，可以配置 Redis：

```xml
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-data-redis</artifactId>
</dependency>
```

```yaml
spring:
  session:
    store-type: redis
  redis:
    host: localhost
    port: 6379
```

---

## 更新日志

- **v4.24.0-SP01**: 初始版本，支持 OAuth2 和 SAML 2.0 认证
- **v1.0**: IDaaS 模块独立发布
