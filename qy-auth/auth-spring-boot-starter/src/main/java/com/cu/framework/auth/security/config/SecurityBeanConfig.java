package com.cu.framework.auth.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cu.framework.auth.security.filter.WebSecurityGlobalExceptionHandlerFilter;
import com.cu.framework.auth.security.extension.PropertiesUrlSecurityCustomizer;
import com.cu.framework.auth.security.response.AuthenticationSuccessResultConverter;
import com.cu.framework.auth.security.response.AuthenticationSuccessResultResolver;
import com.cu.framework.auth.security.response.JsonAuthenticationSuccessHandler;
import com.cu.framework.auth.security.response.NoneAuthenticationSuccessResultResolver;
import com.cu.framework.auth.security.response.SecurityAuthenticationExceptionResolverHandler;
import com.cu.framework.auth.security.response.SecurityAuthenticationSuccessHandler;
import com.cu.framework.auth.security.response.SecurityUserAuthenticationSuccessResultConverter;
import com.cu.framework.auth.security.service.RbacSecurityUserDetailsService;
import com.cu.framework.auth.security.service.SecurityUserDetailsService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.List;

@Configuration
public class SecurityBeanConfig {

    /**
     * 在SpringSecurity之前的过滤器，最后保底处理异常，保证每个异常都会抛出，不会走 Tomcat的 /error重定向
     */
    @Bean
    @ConditionalOnBean(HandlerExceptionResolver.class)
    WebSecurityGlobalExceptionHandlerFilter webSecurityGlobalExceptionHandlerFilter(HandlerExceptionResolver handlerExceptionResolver){
        return new WebSecurityGlobalExceptionHandlerFilter(handlerExceptionResolver);
    }


    @Bean(autowire = Autowire.BY_TYPE)
    @ConditionalOnClass(RbacSecurityUserDetailsService.class)
    SecurityUserDetailsService securityUserDetailsService(){
        return new RbacSecurityUserDetailsService();
    };
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    PasswordEncoder passwordEncoder() {
        // 自己不在Bean中添加这个系统默认也是这个
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


    @Bean
    @ConditionalOnMissingBean(SecurityAuthenticationSuccessHandler.class)
    SecurityAuthenticationSuccessHandler securityAuthenticationSuccessHandler(
            AuthenticationSuccessResultResolver resultResolverProvider, ObjectMapper objectMapper, ObjectProvider<AuthenticationSuccessResultConverter> jsonSuccessResultConverter){
        return new JsonAuthenticationSuccessHandler(resultResolverProvider,objectMapper,jsonSuccessResultConverter);
    }
    @Bean
    SecurityUserAuthenticationSuccessResultConverter securityUserJsonSuccessResultConverter() {
        return new SecurityUserAuthenticationSuccessResultConverter();
    }


    @Bean
    SecurityAuthenticationExceptionResolverHandler authenticationCompositeHandler(List<HandlerExceptionResolver> handlerExceptionResolvers){
        return new SecurityAuthenticationExceptionResolverHandler()
                .setHandlerExceptionResolvers(handlerExceptionResolvers);
    }
    @Bean
    PropertiesUrlSecurityCustomizer permitUrlContainer(){
        return new PropertiesUrlSecurityCustomizer();
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationSuccessResultResolver.class)
    AuthenticationSuccessResultResolver authenticationSuccessResultResolver(){
        return new NoneAuthenticationSuccessResultResolver();
    }
}
