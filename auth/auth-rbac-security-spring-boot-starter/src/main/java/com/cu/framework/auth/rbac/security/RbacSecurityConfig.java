package com.cu.framework.auth.rbac.security;


import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.rbac.composite.SysUserComposite;
import com.cu.framework.authentication.service.SecurityUserDetailsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RbacSecurityConfig {

    /**
     * 基于 RBAC 的用户详情服务，使用方自己提供了 SecurityUserDetailsService 时不注册
     */
    @Bean
    @ConditionalOnMissingBean(SecurityUserDetailsService.class)
    RbacSecurityUserDetailsService securityUserDetailsService(SysUserComposite sysUserComposite) {
        RbacSecurityUserDetailsService service = new RbacSecurityUserDetailsService();
        service.setSysUserComposite(sysUserComposite);
        return service;
    }

    /**
     * 当前登录用户上下文，使用方自己实现 AuthCurrentUserContextHolder 时不注册
     */
    @Bean
    @ConditionalOnMissingBean(AuthCurrentUserContextHolder.class)
    AuthCurrentUserContextHolder rbacSecurityCurrentUserAuthContextHolder(){
        RbacSecurityAuthCurrentUserContextHolder holder = new RbacSecurityAuthCurrentUserContextHolder();
        return holder;
    }

    /**
     * 当前登录用户接口，使用方自己提供同类型端点时不注册
     */
    @Bean
    @ConditionalOnMissingBean(RbacCurrentUserEndpoint.class)
    RbacCurrentUserEndpoint rbacCurrentUserEndpoint() {
        return new RbacCurrentUserEndpoint();
    }

}
