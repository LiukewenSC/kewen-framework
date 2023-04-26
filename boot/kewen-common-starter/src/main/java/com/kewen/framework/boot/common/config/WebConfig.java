package com.kewen.framework.boot.common.config;

import com.kewen.framework.common.context.UserContext;
import com.kewen.framework.common.web.exception.ExceptionHandlerAdvance;
import com.kewen.framework.common.web.filter.RequestPersistanceFilter;
import com.kewen.framework.common.web.filter.RequestPrintFilter;
import com.kewen.framework.common.web.filter.TrackingLogFilter;
import com.kewen.framework.common.web.filter.support.RequestParamPersistentHandler;
import com.kewen.framework.common.web.interceptor.TenantInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
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

    /**
     * 请求日志打印过滤器
     * @return
     */
    @Bean
    RequestPrintFilter requestLogFilter(){
        return new RequestPrintFilter();
    }
    @Bean
    RequestPersistanceFilter requestPersistanceFilter(ObjectProvider<RequestParamPersistentHandler> objectProvider){
        return new RequestPersistanceFilter(objectProvider);
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
    TrackingLogFilter traceLogFilter(){
        return new TrackingLogFilter();
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

        //租户拦截
        registry.addWebRequestInterceptor(tenantInterceptor());

    }





}
