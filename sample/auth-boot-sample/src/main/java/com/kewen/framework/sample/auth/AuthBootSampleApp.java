package com.kewen.framework.sample.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AuthBootSampleApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(AuthBootSampleApp.class, args);
    }
}