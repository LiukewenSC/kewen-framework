package com.cu.framework.boot.magic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * magic-api starter 的示例启动类，用于本地演示 magic-api 接口
 * @author kewen
 * @since 2024-08-29
 */
@SpringBootApplication
public class SampleMagicApiApp {
    /**
     * 应用启动入口
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SampleMagicApiApp.class,args);
    }
}