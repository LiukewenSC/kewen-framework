package com.cu.framework.authentication.config;

import com.cu.framework.authentication.SecurityUserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cu.framework.authentication.filter.WebSecurityGlobalExceptionHandlerFilter;
import com.cu.framework.authentication.loginpage.LoginOptionsEndpoint;
import com.cu.framework.authentication.loginpage.LoginOptionsFactory;
import com.cu.framework.authentication.oauth2.Oauth2OidcProperties;
import com.cu.framework.authentication.password.properties.SecurityLoginProperties;
import com.cu.framework.authentication.response.LoginDeliveryResolver;
import com.cu.framework.authentication.extension.PropertiesUrlSecurityCustomizer;
import com.cu.framework.authentication.response.AuthenticationSuccessResultConverter;
import com.cu.framework.authentication.response.AuthenticationSuccessResultResolver;
import com.cu.framework.authentication.response.JsonAuthenticationSuccessHandler;
import com.cu.framework.authentication.response.NoneAuthenticationSuccessResultResolver;
import com.cu.framework.authentication.response.SecurityAuthenticationExceptionResolverHandler;
import com.cu.framework.authentication.response.SecurityAuthenticationSuccessHandler;
import com.cu.framework.authentication.response.SecurityUserAuthenticationSuccessResultConverter;
import com.cu.framework.authentication.properties.SsoLoginProperties;
import com.cu.framework.authentication.saml.properties.SamlProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties(SsoLoginProperties.class)
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
     * 默认密码编码器（Spring Security 的 DelegatingPasswordEncoder），容器中无 PasswordEncoder 时注册
     */
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    PasswordEncoder passwordEncoder() {
        // 自己不在Bean中添加这个系统默认也是这个
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


    /**
     * 登录方式清单装配器，从框架自身的配置属性取数
     */
    @Bean
    LoginOptionsFactory loginOptionsFactory(SecurityLoginProperties passwordProperties,
                                            Oauth2OidcProperties oauth2OidcProperties,
                                            SamlProperties samlProperties) {
        return new LoginOptionsFactory(passwordProperties, oauth2OidcProperties, samlProperties);
    }

    /**
     * 登录方式发现接口，未认证可访问，URL 由 @SecurityIgnore 收集进放行清单
     */
    @Bean
    LoginOptionsEndpoint loginOptionsEndpoint(LoginOptionsFactory loginOptionsFactory) {
        return new LoginOptionsEndpoint(loginOptionsFactory);
    }

    /**
     * 认证结果交付姿态判定器：三类单点登录共用同一个回跳目标，按请求命中的认证端点决定
     * 这次结果是返回 JSON 还是把浏览器整页送回前端
     */
    @Bean
    LoginDeliveryResolver loginDeliveryResolver(SsoLoginProperties ssoLoginProperties,
                                                Oauth2OidcProperties oauth2OidcProperties,
                                                SamlProperties samlProperties) {
        return new LoginDeliveryResolver(ssoLoginProperties, oauth2OidcProperties, samlProperties);
    }

    /**
     * 默认的登录/登出成功处理器（JSON 输出），容器中无 SecurityAuthenticationSuccessHandler 时注册
     */
    @Bean
    @ConditionalOnMissingBean(SecurityAuthenticationSuccessHandler.class)
    SecurityAuthenticationSuccessHandler securityAuthenticationSuccessHandler(
            AuthenticationSuccessResultResolver resultResolverProvider, ObjectMapper objectMapper, ObjectProvider<AuthenticationSuccessResultConverter> jsonSuccessResultConverter,
            LoginDeliveryResolver loginDeliveryResolver){
        return new JsonAuthenticationSuccessHandler(resultResolverProvider,objectMapper,jsonSuccessResultConverter,loginDeliveryResolver);
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
    SecurityAuthenticationExceptionResolverHandler authenticationCompositeHandler(List<HandlerExceptionResolver> handlerExceptionResolvers,
                                                                                 LoginDeliveryResolver loginDeliveryResolver){
        return new SecurityAuthenticationExceptionResolverHandler()
                .setHandlerExceptionResolvers(handlerExceptionResolvers)
                .setLoginDeliveryResolver(loginDeliveryResolver);
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

    @Bean
    SecurityUserContext securityUserContext() {
        return new SecurityUserContext();
    }
}
