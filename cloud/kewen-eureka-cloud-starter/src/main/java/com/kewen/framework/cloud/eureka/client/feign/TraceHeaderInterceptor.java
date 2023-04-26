package com.kewen.framework.cloud.eureka.client.feign;

import com.kewen.framework.common.context.ContextConstant;
import com.kewen.framework.common.context.TraceContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 流水号加载
 *  此处需要 @Configuration ， 若加@Component拦截不生效
 * @author liukewen
 * @since 2022/9/2
 */
@Configuration
public class TraceHeaderInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        //添加流水号
        String traceId = TraceContext.get();
        if (!StringUtils.isEmpty(traceId)){
            requestTemplate.header(ContextConstant.TRACE_ID_KEY,traceId);
        }
    }
}