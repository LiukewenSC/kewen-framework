package com.kewen.framework.auth.sample.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

/**
 * 
 * @author kewen
 * @since 2024-08-26
 */
@Configuration
public class SessionConfig {
    /**
     * 使用 Header方式获取sessionID
     * @return
     */
    @Bean
    HttpSessionIdResolver sessionIdResolver() {
        return new HeaderHttpSessionIdResolver("Authorization");
    }
}
