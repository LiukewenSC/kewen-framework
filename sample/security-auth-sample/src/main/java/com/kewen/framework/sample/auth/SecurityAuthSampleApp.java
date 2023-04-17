package com.kewen.framework.sample.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(
//        exclude = SecurityAutoConfiguration.class
)
public class SecurityAuthSampleApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(SecurityAuthSampleApp.class, args);
    }
}