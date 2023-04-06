package com.kewen.framework.cloud.security.config;

import com.kewen.framework.cloud.security.service.MemorySecurityUserDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class MemorySecurityBeanLoadConfig {


    @Bean
    PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public MemorySecurityUserDetailService memorySecurityUserDetailService(){
        return new MemorySecurityUserDetailService();
    }

}
