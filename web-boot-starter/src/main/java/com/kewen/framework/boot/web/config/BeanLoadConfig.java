package com.kewen.framework.boot.web.config;

import com.kewen.framework.boot.web.interceptor.tenant.DefaultTenantInterceptor;
import com.kewen.framework.boot.web.interceptor.tenant.NoTenantInterceptor;
import com.kewen.framework.boot.web.interceptor.tenant.TenantInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-12-05 10:21
 */
@Configuration
public class BeanLoadConfig {

    @Bean
    @ConditionalOnProperty(name = "com.kewen.framework.boot.web.tenant",havingValue = "true")
    public DefaultTenantInterceptor defaultTenantInterceptor(){
        return new DefaultTenantInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean(TenantInterceptor.class)
    public NoTenantInterceptor noTenantInterceptor(){
        return new NoTenantInterceptor();
    }
}
