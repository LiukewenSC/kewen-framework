package com.cu.framework.sample.storage;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 文件存储模块示例工程（storage-boot-sample）的启动类，演示引入 storage-spring-boot-starter
 * 并通过 cu.storage 配置七牛云存储的接入方式
 * @author kewen
 * @since 2024-09-30
 */
@SpringBootApplication
public class StorageBootSample {
    /**
     * 示例工程入口
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(StorageBootSample.class, args);
    }
}