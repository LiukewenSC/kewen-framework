package com.kewen.framework.idaas.oidc;

import com.kewen.framework.auth.security.config.HttpSecurityCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * OIDC 配置
 * openidLogin() 在 Spring Security 5.7 中已废弃并移除，现代应用应使用 OAuth2/OIDC 替代 OpenID 2.0
 *
 * @author kewen
 * @since 1.0
 */
@Configuration
public class OidcConfig implements HttpSecurityCustomizer {

    @Override
    public void customizer(HttpSecurity http) throws Exception {
        // OpenID 2.0 (openidLogin) 已在 Spring Security 5.7 中废弃
        // 项目已通过 Oauth2Config 和 SamlConfig 配置了 OAuth2 和 SAML 认证，无需额外配置
    }
}
