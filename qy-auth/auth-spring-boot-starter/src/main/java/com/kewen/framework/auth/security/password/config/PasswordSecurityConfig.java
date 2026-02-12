package com.kewen.framework.auth.security.password.config;

import com.kewen.framework.auth.security.config.HttpSecurityCustomizer;
import com.kewen.framework.auth.security.password.properties.SecurityLoginProperties;
import com.kewen.framework.auth.security.response.SecurityAuthenticationExceptionResolverHandler;
import com.kewen.framework.auth.security.response.SecurityAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author kewen
 * @descrpition
 * @since 2023-02-23
 */
@Configuration
@EnableConfigurationProperties({SecurityLoginProperties.class})
public class PasswordSecurityConfig implements HttpSecurityCustomizer {

    @Autowired
    SecurityLoginProperties loginProperties;

    @Autowired
    SecurityAuthenticationSuccessHandler successHandler;

    @Autowired
    SecurityAuthenticationExceptionResolverHandler exceptionResolverHandler;

    @Override
    public void customizer(HttpSecurity http) throws Exception {
        configure(http);
    }

    protected void configure(HttpSecurity http) throws Exception {
        http.apply(new JsonLoginAuthenticationFilterConfigurer<>())
                .loginProcessingUrl(loginProperties.getLoginUrl())
                .usernameParameter(loginProperties.getUsernameParameter())
                .passwordParameter(loginProperties.getPasswordParameter())
                //.authenticationDetailsSource()  在认证前封装的Authentication中添加详细信息，如从request中拿到的ip,等信息
                .successHandler(successHandler)
                .failureHandler(exceptionResolverHandler)
                .and()
        ;
    }

}
