package com.kewen.framework.boot.web.config;

import com.kewen.framework.boot.web.filter.RequestLogInterceptor;
import com.kewen.framework.boot.web.interceptor.tenant.DefaultTenantInterceptor;
import com.kewen.framework.boot.web.interceptor.trace.TraceInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-12-05 10:21
 */
@Configuration
public class WebBeanLoadConfig {

    /**
     * 租户拦截器
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "com.kewen.framework.boot.web.tenant",havingValue = "true")
    public DefaultTenantInterceptor defaultTenantInterceptor(){
        return new DefaultTenantInterceptor();
    }

    /**
     * 日志拦截器
     * @return
     */
    @Bean
    TraceInterceptor traceInterceptor(){
        return new TraceInterceptor();
    }

    /**
     * 请求日志打印
     * @return
     */
    @Bean
    RequestLogInterceptor requestLogInterceptor(){
        return new RequestLogInterceptor();
    }
}
