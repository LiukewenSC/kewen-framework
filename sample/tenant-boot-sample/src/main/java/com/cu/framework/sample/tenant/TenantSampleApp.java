package com.cu.framework.sample.tenant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 多租户模块示例工程（tenant-boot-sample）的启动类，演示引入 tenant-spring-boot-starter
 * 并通过 cu.tenant.open 开启多租户支持的接入方式
 * @author kewen
 * @since 2024-08-30
 */
@SpringBootApplication
public class TenantSampleApp {
    /**
     * 示例工程入口
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(TenantSampleApp.class, args);
    }
}