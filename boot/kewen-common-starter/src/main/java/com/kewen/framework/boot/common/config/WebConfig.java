package com.kewen.framework.boot.common.config;

import com.kewen.framework.common.context.UserContext;
import com.kewen.framework.common.web.exception.ExceptionHandlerAdvance;
import com.kewen.framework.common.web.filter.RequestLogFilter;
import com.kewen.framework.common.web.interceptor.TenantInterceptor;
import com.kewen.framework.common.web.interceptor.TraceInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @descrpition web配置
 * @author kewen
 * @since 2022-12-05 9:36
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 请求日志打印过滤器
     * @return
     */
    @Bean
    RequestLogFilter requestLogFilter(){
        return new RequestLogFilter();
    }

    /**
     * 异常统一返回增强
     * @return
     */
    @Bean
    ExceptionHandlerAdvance exceptionHandlerAdvance(){
        return new ExceptionHandlerAdvance();
    }


    /**
     * 用户上下文
     * @return
     */
    @Bean
    UserContext userContext(){
        return new UserContext();
    }
    /**
     * 交易流水号拦截器
     * @return
     */
    @Bean
    TraceInterceptor traceInterceptor(){
        return new TraceInterceptor();
    }

    /**
     * 租户拦截器
     * @return
     */
    @Value("${kewen.base.tenant.open:false}")
    Boolean isOpenTenant;
    @Bean
    public TenantInterceptor tenantInterceptor(){
        return new TenantInterceptor(isOpenTenant);
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //日志拦截
        registry.addWebRequestInterceptor(traceInterceptor());
        //租户拦截
        registry.addWebRequestInterceptor(tenantInterceptor());

    }





}
