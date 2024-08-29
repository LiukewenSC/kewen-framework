package com.kewen.framework.boot.common.config;


import com.kewen.framework.common.core.filter.EarlyRequestFilter;
import com.kewen.framework.common.core.filter.EarlyRequestFilterProxy;
import com.kewen.framework.common.logger.PersistentRequestFilter;
import com.kewen.framework.common.logger.TraceRequestFilter;
import com.kewen.framework.common.logger.trace.TraceIdProcessor;
import com.kewen.framework.common.tenant.TenantRequestFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * @author kewen
 * @since 2024-08-27
 */
@Configuration
public class EarlyFilterConfig {

    /**
     * 交易流水号拦截器
     * @return
     */
    @Bean
    TraceRequestFilter traceRequestFilter(ObjectProvider<TraceIdProcessor> traceIdProcessor){
        return new TraceRequestFilter(traceIdProcessor);
    }
    /**
     * 租户过滤器
     * @return
     */
    @Bean
    TenantRequestFilter tenantInfoFilter(){
        return new TenantRequestFilter();
    }

    /**
     * 请求过滤器，保存请求到上下文中
     * @return
     */
    @Bean
    PersistentRequestFilter requestInfoFilter(){
        return new PersistentRequestFilter();
    }


    @Bean
    EarlyRequestFilterProxy earlyRequestFilterProxy(ObjectProvider<EarlyRequestFilter> earlyRequestFilters){
        return new EarlyRequestFilterProxy(earlyRequestFilters);
    }

}
