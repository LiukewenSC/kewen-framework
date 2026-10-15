package com.cu.framework.rbac.security;


import com.cu.framework.auth.core.AuthCurrentUserContextHolder;

import com.cu.framework.auth.rbac.composite.SysUserComposite;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RbacSecurityConfig {

    /**
     * 基于 RBAC 的用户详情服务
     */
    @Bean
    RbacSecurityUserDetailsService securityUserDetailsService(SysUserComposite sysUserComposite){
        RbacSecurityUserDetailsService service = new RbacSecurityUserDetailsService();
        service.setSysUserComposite(sysUserComposite);
        return service;
    };
    @Bean
    AuthCurrentUserContextHolder rbacSecurityCurrentUserAuthContextHolder(RbacSecurityUserDetailsService securityUserDetailsService){
        RbacSecurityAuthCurrentUserContextHolder holder = new RbacSecurityAuthCurrentUserContextHolder();
        holder.setRbacSecurityUserDetailsService(securityUserDetailsService);
        return holder;
    };

}
