package com.kewen.framework.sample.cloud.alibaba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.kewen")
public class NacosProviderSample {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(NacosProviderSample.class, args);
    }
}
