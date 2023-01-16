package com.kewen.framework.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.kewen")
public class NacosConsumerSample {
    public static void main(String[] args) {

        ConfigurableApplicationContext run = SpringApplication.run(NacosConsumerSample.class, args);
    }
}
