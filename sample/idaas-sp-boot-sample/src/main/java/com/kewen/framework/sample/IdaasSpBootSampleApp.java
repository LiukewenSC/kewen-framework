package com.kewen.framework.sample;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class IdaasSpBootSampleApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(IdaasSpBootSampleApp.class, args);
    }
}