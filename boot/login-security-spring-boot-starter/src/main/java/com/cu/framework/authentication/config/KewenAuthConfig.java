package com.cu.framework.authentication.config;


import com.cu.framework.authentication.handler.SecurityResponseBodyResultResolver;
import com.cu.framework.authentication.handler.AuthenticationExceptionAdviceHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * 
 * @author kewen
 * @since 2024-08-29
 */
@AutoConfiguration
public class KewenAuthConfig {

    @Bean
    SecurityResponseBodyResultResolver authResponseBodyResultResolver(){
        return new SecurityResponseBodyResultResolver();
    }
    @Bean
    AuthenticationExceptionAdviceHandler exceptionAdviceHandler(){
        return new AuthenticationExceptionAdviceHandler();
    }

}
