package com.kewen.framework.boot.web.config;

import com.kewen.framework.boot.web.interceptor.tenant.TenantInterceptor;
import com.kewen.framework.boot.web.interceptor.trace.TraceInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
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

    @Autowired(required = false)
    TenantInterceptor tenantInterceptor;
    @Autowired
    TraceInterceptor traceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //日志拦截
        registry.addWebRequestInterceptor(traceInterceptor);

        //租户拦截，配置了需要租户才做拦截
        if (tenantInterceptor !=null){
            registry.addWebRequestInterceptor(tenantInterceptor);
        }
    }





}
