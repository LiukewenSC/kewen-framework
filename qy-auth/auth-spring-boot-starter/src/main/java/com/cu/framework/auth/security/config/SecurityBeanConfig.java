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

/**
 * 认证相关默认 Bean 的注册配置：全局异常处理过滤器、RBAC 用户详情服务、密码编码器、
 * JSON 登录成功处理器、安全异常解析处理器、放行 URL 收集器等；
 * 多数 Bean 带 @ConditionalOnMissingBean 保护，业务工程可自行定义同类型 Bean 覆盖
 * @author kewen
 * @since 2026-02-12
 */
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


    /**
     * 基于 RBAC 的用户详情服务，类路径存在 RbacSecurityUserDetailsService 时注册
     */
    @Bean(autowire = Autowire.BY_TYPE)
    @ConditionalOnClass(RbacSecurityUserDetailsService.class)
    SecurityUserDetailsService securityUserDetailsService(){
        return new RbacSecurityUserDetailsService();
    };
    /**
     * 默认密码编码器（Spring Security 的 DelegatingPasswordEncoder），容器中无 PasswordEncoder 时注册
     */
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    PasswordEncoder passwordEncoder() {
        // 自己不在Bean中添加这个系统默认也是这个
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


    /**
     * 默认的登录/登出成功处理器（JSON 输出），容器中无 SecurityAuthenticationSuccessHandler 时注册
     */
    @Bean
    @ConditionalOnMissingBean(SecurityAuthenticationSuccessHandler.class)
    SecurityAuthenticationSuccessHandler securityAuthenticationSuccessHandler(
            AuthenticationSuccessResultResolver resultResolverProvider, ObjectMapper objectMapper, ObjectProvider<AuthenticationSuccessResultConverter> jsonSuccessResultConverter){
        return new JsonAuthenticationSuccessHandler(resultResolverProvider,objectMapper,jsonSuccessResultConverter);
    }
    /**
     * 默认的认证成功结果转换器，将 principal 为 SecurityUser 的认证结果转换为返回体
     */
    @Bean
    SecurityUserAuthenticationSuccessResultConverter securityUserJsonSuccessResultConverter() {
        return new SecurityUserAuthenticationSuccessResultConverter();
    }


    /**
     * 安全异常解析处理器，注入容器中全部 HandlerExceptionResolver，用于统一解析认证/访问异常
     */
    @Bean
    SecurityAuthenticationExceptionResolverHandler authenticationCompositeHandler(List<HandlerExceptionResolver> handlerExceptionResolvers){
        return new SecurityAuthenticationExceptionResolverHandler()
                .setHandlerExceptionResolvers(handlerExceptionResolvers);
    }
    /**
     * 放行 URL 收集器，汇总 cu.auth.permit-url 配置与 @SecurityIgnore 注解标记的 URL
     */
    @Bean
    PropertiesUrlSecurityCustomizer permitUrlContainer(){
        return new PropertiesUrlSecurityCustomizer();
    }

    /**
     * 默认的认证成功结果解析器（原样返回数据不做包装），容器中无 AuthenticationSuccessResultResolver 时注册
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticationSuccessResultResolver.class)
    AuthenticationSuccessResultResolver authenticationSuccessResultResolver(){
        return new NoneAuthenticationSuccessResultResolver();
    }
}
