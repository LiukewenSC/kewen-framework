package com.cu.framework.auth.security.config;


import com.cu.framework.auth.security.handler.AuthResponseBodyResultResolver;
import com.cu.framework.auth.security.handler.ExceptionAdviceHandler;
import com.cu.framework.auth.security.handler.RbacResultAdviceHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * @author kewen
 * @since 2024-08-29
 */
@Configuration
public class KewenAuthConfig {

    @Bean
    AuthResponseBodyResultResolver authResponseBodyResultResolver(){
        return new AuthResponseBodyResultResolver();
    }
    @Bean
    ExceptionAdviceHandler exceptionAdviceHandler(){
        return new ExceptionAdviceHandler();
    }
    @Bean
    RbacResultAdviceHandler rbacResultAdviceHandler(){
        return new RbacResultAdviceHandler();
    }
}
