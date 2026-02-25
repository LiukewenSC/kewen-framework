package com.kewen.framework.idaas.oidc;

import com.kewen.framework.auth.security.config.HttpSecurityCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 2026/02/22
 *
 * @author kewen
 * @since 1.0
 */
@Configuration
public class OidcConfig implements HttpSecurityCustomizer {

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void customizer(HttpSecurity http) throws Exception {
        http
                .openidLogin(openidLogin -> {
                    openidLogin
                            .loginPage("/login")
                            .authenticationUserDetailsService(
                                    new UserDetailsByNameServiceWrapper<>(userDetailsService)
                            );
                });
    }
}
