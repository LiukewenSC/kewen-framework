package com.kewen.framework.sample.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @descrpition 安全测试类
 * @author kewen
 * @since 2023-02-28
 */
@SpringBootApplication
public class SecuritySample {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(SecuritySample.class, args);
    }
}
