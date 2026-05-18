# Spring Security 5.7 中实现 SAML2 SP Metadata 端点

## 概述

在 Spring Security 5.7（对应 Spring Boot 2.7.x）中，虽然不像 Spring Security 6.x 那样提供了自动的 SP Metadata 端点配置（如 `saml2Metadata()` DSL），但我们可以通过手动配置 `Saml2MetadataFilter` 来实现相同的功能。

## 实现原理

### SAML2 Metadata 的作用

SAML2 Metadata 是 SAML 联盟（SAML Federation）中的核心概念，它用于在服务提供者（SP）和身份提供者（IdP）之间交换配置信息。Metadata 包含了：

- **SP 端点信息**：Assertion Consumer Service (ACS) URL、Single Logout Service URL 等
- **证书信息**：用于签名和加密的 X.509 证书
- **支持的绑定方式**：HTTP-Redirect、HTTP-POST、HTTP-Artifact 等
- **Entity ID**：SP 的唯一标识符

通过暴露 SP metadata 端点，IdP 可以自动获取 SP 的配置信息，简化了 SAML 联盟的配置过程。

### Spring Security 5.7 中的实现机制

在 Spring Security 5.7 中，SAML2 Service Provider 功能基于 OpenSAML 库实现。虽然 Spring Security 6.x 提供了更高级的 DSL 配置（如 `saml2Metadata()`），但在 5.7 版本中，核心组件已经存在，只是需要手动配置。

#### 核心组件

1. **Saml2MetadataFilter**
   - 位置：`org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter`
   - 作用：拦截 metadata 请求，生成并返回 SAML2 XML metadata
   - 默认处理路径：`/saml2/service-provider-metadata/{registrationId}`

2. **RelyingPartyRegistrationResolver**
   - 位置：`org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver`
   - 作用：根据 registrationId 解析对应的 SP 注册信息
   - 实现类：`DefaultRelyingPartyRegistrationResolver`

3. **Saml2MetadataResolver**
   - 位置：`org.springframework.security.saml2.provider.service.metadata.Saml2MetadataResolver`
   - 作用：将 RelyingPartyRegistration 对象转换为符合 SAML2 标准的 XML metadata
   - 实现类：`OpenSamlMetadataResolver`

#### 工作流程

```
HTTP 请求: GET /saml2/service-provider-metadata/keycloak
    ↓
Saml2MetadataFilter 拦截请求
    ↓
从 URL 中提取 registrationId (keycloak)
    ↓
RelyingPartyRegistrationResolver 解析 registrationId
    ↓
获取对应的 RelyingPartyRegistration 对象
    ↓
Saml2MetadataResolver 生成 XML metadata
    ↓
返回 SAML2 EntityDescriptor XML 响应
```

#### 为什么 Spring Security 5.7 默认不添加此过滤器？

Spring Security 团队在设计时考虑了以下因素：

1. **安全性**：Metadata 端点暴露了 SP 的内部配置信息，包括端点 URL 和证书，应该由开发者明确决定是否暴露
2. **灵活性**：不同的部署场景可能需要不同的 metadata 暴露方式（公开、认证访问、静态文件等）
3. **向后兼容**：在早期版本中，metadata 通常通过手动方式提供给 IdP

因此，Spring Security 5.7 提供了所有必要的组件，但需要开发者主动配置和添加过滤器。

### Metadata XML 示例

配置成功后，访问 metadata 端点将返回类似如下的 XML：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<md:EntityDescriptor 
    xmlns:md="urn:oasis:names:tc:SAML:2.0:metadata"
    entityID="http://localhost:8080/saml2/service-provider/keycloak">
    
    <md:SPSSODescriptor 
        AuthnRequestsSigned="true"
        WantAssertionsSigned="true"
        protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol">
        
        <md:KeyDescriptor use="signing">
            <ds:KeyInfo xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
                <ds:X509Data>
                    <ds:X509Certificate>MIID...</ds:X509Certificate>
                </ds:X509Data>
            </ds:KeyInfo>
        </md:KeyDescriptor>
        
        <md:AssertionConsumerService 
            Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
            Location="http://localhost:8080/login/saml2/sso/keycloak"
            index="0"/>
            
        <md:SingleLogoutService 
            Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"
            Location="http://localhost:8080/logout/saml2/slo"/>
            
    </md:SPSSODescriptor>
</md:EntityDescriptor>
```

## 配置步骤

### 1. 创建 RelyingPartyRegistrationRepository

首先需要配置 SP 的注册信息：

```java
@Bean
public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
    // 构建 RelyingPartyRegistration 列表
    List<RelyingPartyRegistration> registrations = buildRegistrations();
    return new InMemoryRelyingPartyRegistrationRepository(registrations);
}
```

### 2. 配置 Saml2MetadataFilter

在 `HttpSecurity` 配置中添加 `Saml2MetadataFilter`：

```java
@Override
public void customizer(HttpSecurity http) throws Exception {
    // 配置 SAML2 登录
    http.saml2Login()
        .relyingPartyRegistrationRepository(relyingPartyRegistrationRepository())
        .successHandler(successHandler)
        .failureHandler(exceptionResolverHandler);
    
    // 创建 Saml2MetadataFilter
    Saml2MetadataFilter saml2MetadataFilter = new Saml2MetadataFilter(
        new DefaultRelyingPartyRegistrationResolver(relyingPartyRegistrationRepository()),
        new OpenSamlMetadataResolver()
    );
    
    // 将过滤器添加到 SAML2 认证过滤器之前
    http.addFilterBefore(saml2MetadataFilter, Saml2WebSsoAuthenticationFilter.class);
}
```

### 3. 端点访问

配置完成后，SP metadata 端点的默认访问路径为：

```
/saml2/service-provider-metadata/{registrationId}
```

其中 `{registrationId}` 是你在 `RelyingPartyRegistration` 中设置的注册 ID。

例如，如果你的 registrationId 为 `keycloak`，则访问路径为：

```
/saml2/service-provider-metadata/keycloak
```

## 完整示例

以下是完整的配置示例：

```java
@Configuration
@EnableConfigurationProperties({SamlProperties.class})
public class SamlConfig implements HttpSecurityCustomizer {

    @Autowired
    private SamlProperties samlProperties;

    @Override
    public void customizer(HttpSecurity http) throws Exception {
        // 配置 SAML2 认证提供者
        AuthenticationProvider samlAuthenticationProvider = new OpenSamlAuthenticationProvider();
        http.authenticationProvider(samlAuthenticationProvider);
        
        // 配置 SAML2 登录
        http.saml2Login()
            .relyingPartyRegistrationRepository(relyingPartyRegistrationRepository())
            .successHandler(successHandler)
            .failureHandler(exceptionResolverHandler)
            .loginProcessingUrl(samlProperties.getLoginProcessingUrl())
            .and()
            .saml2Logout(saml2Logout ->
                saml2Logout
                    .logoutUrl("/logout")
                    .logoutRequest().logoutUrl("/logout/saml2/slo")
                    .and()
                    .logoutResponse().logoutUrl("/logout/saml2/slo")
            );
            
        // 添加 SAML SP Metadata 过滤器
        // Spring Security 默认不添加 SP 的 metadata 过滤器
        // 默认路径为 /saml2/service-provider-metadata/{registrationId}
        Saml2MetadataFilter saml2MetadataFilter = new Saml2MetadataFilter(
            new DefaultRelyingPartyRegistrationResolver(relyingPartyRegistrationRepository()),
            new OpenSamlMetadataResolver()
        );
        http.addFilterBefore(saml2MetadataFilter, Saml2WebSsoAuthenticationFilter.class);
    }

    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
        List<RelyingPartyRegistration> registrations = buildRegistrations();
        return new InMemoryRelyingPartyRegistrationRepository(registrations);
    }
    
    // 其他配置方法...
}
```

## 注意事项

1. **过滤器顺序**：确保将 `Saml2MetadataFilter` 添加到 `Saml2WebSsoAuthenticationFilter` 之前，以保证 metadata 请求能够被正确处理。

2. **Registration ID**：每个 SP 配置都需要一个唯一的 registrationId，这将作为 metadata 端点 URL 的一部分。

3. **安全性**：metadata 端点通常应该是公开访问的，以便 IdP 可以获取 SP 的配置信息。确保在安全配置中允许对该端点的匿名访问。

4. **依赖版本**：确保使用与 Spring Security 5.7 兼容的 OpenSAML 版本。

## 总结

虽然 Spring Security 5.7 没有提供像 6.x 版本那样便捷的 `saml2Metadata()` DSL 配置，但通过手动配置 `Saml2MetadataFilter`，我们完全可以实现相同的功能。这种方式更加灵活，允许开发者根据项目需求进行定制化配置。

关键点：
- 使用 `Saml2MetadataFilter` 处理 metadata 请求
- 使用 `DefaultRelyingPartyRegistrationResolver` 解析注册信息
- 使用 `OpenSamlMetadataResolver` 生成 metadata XML
- 将过滤器正确添加到过滤器链中
