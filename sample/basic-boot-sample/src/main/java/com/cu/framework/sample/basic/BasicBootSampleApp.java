package com.cu.framework.sample.basic;

import com.cu.framework.boot.basic.config.MybatisPlusConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 基础模块示例工程（basic-boot-sample）的启动类，演示请求参数校验、Jackson 日期序列化、异步方法、
 * SpringApplicationRunListener 生命周期监听等基础能力的接入方式
 * @author kewen
 * @since 2024-08-29
 */
@SpringBootApplication(exclude = MybatisPlusConfig.class)
public class BasicBootSampleApp {
    /**
     * 示例工程入口
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BasicBootSampleApp.class, args);
    }
}