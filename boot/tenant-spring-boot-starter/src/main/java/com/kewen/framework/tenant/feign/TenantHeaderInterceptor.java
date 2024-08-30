package com.kewen.framework.tenant.feign;

import com.kewen.framework.tenant.TenantConstant;
import com.kewen.framework.tenant.TenantContext;
import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * 租户加载
 *
 *  此处需要 @Configuration ， 若加@Component拦截不生效
 * @author liukewen
 * @since 2022/9/2
 */
@Configuration
@ConditionalOnProperty(name = "kewen.tenant.open",havingValue = "true")
@ConditionalOnClass(Feign.class)
public class TenantHeaderInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        //添加租户
        Long tenantId = TenantContext.get();
        if (tenantId !=null){
            requestTemplate.header(TenantConstant.TENANT_ID,tenantId.toString());
        }
    }
}