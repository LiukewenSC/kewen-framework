package com.kewen.framework.idaas.oidc;

import com.kewen.framework.auth.security.config.HttpSecurityCustomizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * 2026/02/22
 *
 * @author kewen
 * @since 1.0
 */
public class OidcConfig implements HttpSecurityCustomizer {
    @Override
    public void customizer(HttpSecurity http) throws Exception {
        http
                .openidLogin(openidLogin -> {
                    openidLogin
                            .loginPage("/login")
                            .authenticationUserDetailsService(new OidcUserDetailsService());
                });
    }
}
