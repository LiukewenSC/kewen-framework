package com.cu.framework.authentication.config;


import com.alibaba.fastjson.JSONObject;
import com.cu.framework.authentication.handler.SessionRepositoryEventAspect;
import com.cu.framework.authentication.properties.RememberMeProperties;
import com.cu.framework.authentication.properties.SessionProperties;
import com.cu.framework.basic.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;
import com.cu.framework.authentication.extension.HeaderCookieHttpSessionIdResolver;
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

    /**
     * 基于 Spring Session 的 RememberMeServices，仅当 cu.security.remember-me.enabled=true 时注册；
     * remember-me 参数名与 token 有效期取自 RememberMeProperties 配置
     */
    @Bean
    @ConditionalOnProperty(name = "cu.security.remember-me.enabled", havingValue = "true")
    RememberMeServices springSessionRememberMeServices(RememberMeProperties rememberMe) {
        SpringSessionRememberMeServices services = new SpringSessionRememberMeServices();
        services.setRememberMeParameterName(rememberMe.getRememberParameter());
        services.setValiditySeconds(rememberMe.getValiditySeconds());
        return services;
    }

    /**
     * 注册 spring-session 事件切面，使 SessionRepository 的创建/删除操作能发布会话事件
     * @deprecated 该功能已集成到 SessionRepositoryWrapperPostProcessor 中
     */
    //@Bean
    @Deprecated
    SessionRepositoryEventAspect findByIndexNameSessionRepositoryAspect(){
        return new SessionRepositoryEventAspect();
    }

    /**
     * 会话仓库包装处理器：增强 JabcSessionRepository 功能，用于在会话仓库中添加会话创建/删除事件发布功能
     * @return
     */
    @Bean
    @ConditionalOnBean(value = {JdbcIndexedSessionRepository.class})
    SessionRepositoryWrapperPostProcessor sessionRepositoryWrapperPostProcessor() {
        return new SessionRepositoryWrapperPostProcessor();
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


    /**
     * 会话并发控制定制器：按 SessionProperties 配置最大会话数与超限是否阻止登录，
     * 会话过期时返回 401 及“登录状态失效，请重新登录”的 JSON 响应
     */
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
                            try {
                                HttpServletResponse response = event.getResponse();
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=utf-8");
                                response.getWriter().write(
                                        JSONObject.toJSONString(Result.failed(HttpServletResponse.SC_UNAUTHORIZED, "登录状态失效，请重新登录")));
                            } catch (Exception ex) {
                                throw new IllegalStateException(ex);
                            }
                        })
                        .maxSessionsPreventsLogin(session.isMaxSessionsPreventsLogin());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

}
