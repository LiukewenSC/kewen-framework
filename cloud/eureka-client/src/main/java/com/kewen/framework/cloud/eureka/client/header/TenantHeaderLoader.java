package com.kewen.framework.cloud.eureka.client.header;

import com.kewen.framework.boot.web.interceptor.tenant.TenantContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

/**
 * 租户加载
 *
 *  此处需要 @Configuration ， 若加@Component拦截不生效
 * @author liukewen
 * @since 2022/9/2
 */
@Configuration
public class TenantHeaderLoader implements RequestInterceptor {

    private static final String TENANT_ID ="tenantId";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        //添加租户
        Long tenantId = TenantContext.get();
        if (tenantId !=null){
            requestTemplate.header(TENANT_ID,tenantId.toString());
        }
    }
}