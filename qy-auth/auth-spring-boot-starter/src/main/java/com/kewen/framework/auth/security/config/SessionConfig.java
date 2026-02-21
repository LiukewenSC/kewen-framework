package com.kewen.framework.auth.security.config;


import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSONObject;
import com.kewen.framework.auth.security.handler.SessionRepositoryEventAspect;
import com.kewen.framework.auth.security.properties.RememberMeProperties;
import com.kewen.framework.auth.security.properties.SessionProperties;
import com.kewen.framework.basic.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;
import com.kewen.framework.auth.security.extension.HeaderCookieHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author kewen
 * @since 2024-08-26
 */
@Configuration
@EnableConfigurationProperties(value = {SessionProperties.class, RememberMeProperties.class})
public class SessionConfig {
    private static final Logger log = LoggerFactory.getLogger(SessionConfig.class);
    /**
     * 使用 Header方式获取sessionID
     * @return
     */
    /**
     * 组合 Session ID 解析器：优先从 Header（Authorization）中获取 token，
     * 没有则回退到 Cookie 方式，实现 token 和 session 双模式兼容
     */
    @Bean
    HttpSessionIdResolver sessionIdResolver() {
        return new HeaderCookieHttpSessionIdResolver("Authorization");
    }

    @Bean
    @ConditionalOnProperty(name = "kewen.security.remember-me.enabled", havingValue = "true")
    RememberMeServices springSessionRememberMeServices(RememberMeProperties rememberMe) {
        SpringSessionRememberMeServices services = new SpringSessionRememberMeServices();
        services.setRememberMeParameterName(rememberMe.getRememberParameter());
        services.setValiditySeconds(rememberMe.getValiditySeconds());
        return services;
    }

    @Bean
    SessionRepositoryEventAspect findByIndexNameSessionRepositoryAspect(){
        return new SessionRepositoryEventAspect();
    }

    /**
     * 创建spring-session在spring中的会话管理
     * @param repository
     * @return
     */
    /*@Bean
    SessionRegistry springSessionBackedSessionRegistry(FindByIndexNameSessionRepository repository) {
        return new SpringSessionBackedSessionRegistry(repository);
    }*/


    @Bean
/*
    public HttpSecurityCustomizer HttpSecurityCustomizer(SessionRegistry sessionRegistry, SessionProperties session) {
*/
    public HttpSecurityCustomizer HttpSecurityCustomizer( SessionProperties session) {

        return http -> {
            try {
                http.sessionManagement()
                        .maximumSessions(session.getMaximumSessions())
                        //.sessionRegistry(sessionRegistry)
                        .expiredSessionStrategy(event -> {
                            HttpServletResponse response = event.getResponse();
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            ServletUtil.write(
                                    response,
                                    JSONObject.toJSONString(Result.failed(HttpServletResponse.SC_UNAUTHORIZED,"登录状态失效，请重新登录")),
                                    "application/json;charset=utf-8");
                        })
                        .maxSessionsPreventsLogin(session.isMaxSessionsPreventsLogin());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

}
