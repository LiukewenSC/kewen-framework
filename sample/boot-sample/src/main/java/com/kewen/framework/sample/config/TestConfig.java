package com.kewen.framework.sample.config;

import com.kewen.framework.sample.controller.TestService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {


    @Bean
    public TestService test4Service(){
        TestService service = new TestService();
        return service;
    }

}
