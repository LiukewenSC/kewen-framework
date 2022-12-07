package com.kewen.framework.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author kewen
 * @descrpition
 * @since 2022-12-07 15:08
 */
@SpringBootApplication
public class CloudSampleApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(CloudSampleApp.class, args);
    }
}
