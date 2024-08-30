package com.kewen.framework.boot.basic.config;



import com.kewen.framework.basic.filter.EarlyRequestFilter;
import com.kewen.framework.basic.filter.EarlyRequestFilterProxy;
import com.kewen.framework.basic.logger.RequestLoggerFilter;
import com.kewen.framework.basic.logger.TraceRequestFilter;
import com.kewen.framework.basic.logger.trace.TraceIdProcessor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
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
     * 请求过滤器，保存请求到上下文中
     * @return
     */
    @Bean
    RequestLoggerFilter requestInfoFilter(){
        return new RequestLoggerFilter();
    }


    @Bean
    EarlyRequestFilterProxy earlyRequestFilterProxy(ObjectProvider<EarlyRequestFilter> earlyRequestFilters){
        return new EarlyRequestFilterProxy(earlyRequestFilters);
    }

}
