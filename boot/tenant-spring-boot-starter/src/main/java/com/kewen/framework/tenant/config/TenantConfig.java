package com.kewen.framework.tenant.config;


import com.kewen.framework.tenant.TenantRequestFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 租户对于feign的支持，需要有feign的前提下才开启
 * @author kewen
 * @since 2024-08-30
 */
@Configuration
@ConditionalOnProperty(name = "kewen.tenant.open",havingValue = "true")
public class TenantConfig {
    @Bean
    TenantRequestFilter tenantRequestFilter() {
        return new TenantRequestFilter();
    }

}
