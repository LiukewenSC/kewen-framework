package com.cu.framework.authentication.config;


import com.cu.framework.authentication.handler.AuthResponseBodyResultResolver;
import com.cu.framework.authentication.handler.AuthenticationExceptionAdviceHandler;
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
    AuthenticationExceptionAdviceHandler exceptionAdviceHandler(){
        return new AuthenticationExceptionAdviceHandler();
    }

}
