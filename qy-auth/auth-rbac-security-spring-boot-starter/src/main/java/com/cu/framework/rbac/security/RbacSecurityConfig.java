package com.cu.framework.rbac.security;

import com.cu.framework.authentication.config.HttpSecurityCustomizer;
import com.cu.framework.authentication.properties.SecurityProperties;
import com.cu.framework.authentication.response.AuthenticationSuccessResultConverter;
import com.cu.framework.authentication.response.AuthenticationSuccessResultResolver;
import com.cu.framework.authentication.service.SecurityUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.session.SessionManagementFilter;

@Configuration
public class RbacSecurityConfig implements HttpSecurityCustomizer {
    @Autowired
    SecurityProperties securityProperties;
    @Autowired
    AuthenticationSuccessResultResolver resultResolverProvider ;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ObjectProvider<AuthenticationSuccessResultConverter> jsonSuccessResultConverter;

    @Autowired
    UserDetailsService userDetailsService;
    @Override
    public void customizer(HttpSecurity http) throws Exception {
        http.addFilterAfter(new AuthUserContextFilter(securityProperties.getCurrentUserUrl(),resultResolverProvider,objectMapper,jsonSuccessResultConverter,userDetailsService), SessionManagementFilter.class);
    }



    /**
     * 基于 RBAC 的用户详情服务，类路径存在 RbacSecurityUserDetailsService 时注册
     */
    /*@Bean(autowire = Autowire.BY_TYPE)
    @ConditionalOnClass(RbacSecurityUserDetailsService.class)
    SecurityUserDetailsService securityUserDetailsService(){
        return new RbacSecurityUserDetailsService();
    };*/

}
