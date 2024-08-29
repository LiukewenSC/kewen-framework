package com.kewen.framework.auth.config;


import com.kewen.framework.auth.handler.AuthResponseBodyResultResolver;
import com.kewen.framework.auth.handler.ExceptionAdviceHandler;
import com.kewen.framework.auth.handler.RabcResultAdviceHandler;
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
