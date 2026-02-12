package com.kewen.framework.auth.security.config;


import com.kewen.framework.auth.security.handler.AuthResponseBodyResultResolver;
import com.kewen.framework.auth.security.handler.ExceptionAdviceHandler;
import com.kewen.framework.auth.security.handler.RabcResultAdviceHandler;
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
    RabcResultAdviceHandler rabcResultAdviceHandler(){
        return new RabcResultAdviceHandler();
    }
}
