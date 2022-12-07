package com.kewen.framework.cloud.security.config;

import com.kewen.framework.cloud.security.service.SysUserDetailService;
import com.kewen.framework.cloud.security.service.service.SysUserInfoService;
import com.kewen.framework.cloud.security.service.service.SysUserRoleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author kewen
 * @descrpition
 * @since 2022-12-07 17:25
 */
@Configuration
public class BeanLoadConfig {

    @Bean
    public UserDetailsService userDetailService(SysUserInfoService sysUserInfoService, SysUserRoleService sysUserRoleService){
        return new SysUserDetailService(sysUserInfoService, sysUserRoleService);
    }
    @Bean
    PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }

}
