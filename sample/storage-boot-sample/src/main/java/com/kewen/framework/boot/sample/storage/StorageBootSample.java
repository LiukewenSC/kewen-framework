package com.kewen.framework.boot.sample.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 
 * @author kewen
 * @since 2023-04-24
 */
@SpringBootApplication
public class StorageBootSample {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(StorageBootSample.class, args);
    }
}
