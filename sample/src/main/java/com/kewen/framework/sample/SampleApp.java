package com.kewen.framework.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @descrpition 测试启动类
 * @author kewen
 * @since 2022-12-05 9:54
 */
@SpringBootApplication
public class SampleApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(SampleApp.class);
    }
}
