package com.kewen.framework.boot.web.config;

import com.kewen.framework.boot.web.interceptor.tenant.TenantInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @descrpition web配置
 * @author kewen
 * @since 2022-12-05 9:36
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    TenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addWebRequestInterceptor(tenantInterceptor);
    }





}
