package com.cu.framework.tenant.config;


import com.cu.framework.tenant.TenantRequestFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * 租户对于feign的支持，需要有feign的前提下才开启
 * @author kewen
 * @since 2024-08-30
 */
@AutoConfiguration
@ConditionalOnProperty(name = "cu.tenant.open",havingValue = "true")
public class TenantConfig {
    @Bean
    TenantRequestFilter tenantRequestFilter() {
        return new TenantRequestFilter();
    }

}
