package com.cu.framework.authentication.password.config;

import com.cu.framework.authentication.config.HttpSecurityCustomizer;
import com.cu.framework.authentication.password.properties.SecurityLoginProperties;
import com.cu.framework.authentication.response.SecurityAuthenticationExceptionResolverHandler;
import com.cu.framework.authentication.response.SecurityAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * @author kewen
 * @descrpition
 * @since 2023-02-23
 */
@AutoConfiguration
@EnableConfigurationProperties({SecurityLoginProperties.class})
public class PasswordSecurityConfig implements HttpSecurityCustomizer {

    @Autowired
    SecurityLoginProperties loginProperties;

    @Autowired
    SecurityAuthenticationSuccessHandler successHandler;

    @Autowired
    SecurityAuthenticationExceptionResolverHandler exceptionResolverHandler;

    /**
     * HttpSecurityCustomizer 入口，将 JSON 登录配置应用到 HttpSecurity
     */
    @Override
    public void customizer(HttpSecurity http) throws Exception {
        configure(http);
    }

    /**
     * 注册 JsonLoginAuthenticationFilterConfigurer：登录 URL、用户名/密码字段名取自 SecurityLoginProperties，
     * 登录成功由 successHandler 输出 JSON，失败由 exceptionResolverHandler 解析异常
     */
    protected void configure(HttpSecurity http) throws Exception {
        http.with(new JsonLoginAuthenticationFilterConfigurer<>(), configurer -> configurer
                .loginProcessingUrl(loginProperties.getLoginUrl())
                .usernameParameter(loginProperties.getUsernameParameter())
                .passwordParameter(loginProperties.getPasswordParameter())
                //.authenticationDetailsSource()  在认证前封装的Authentication中添加详细信息，如从request中拿到的ip,等信息
                .successHandler(successHandler)
                .failureHandler(exceptionResolverHandler));
    }

}
