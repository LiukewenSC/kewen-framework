package com.cu.framework.sample;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * IDaaS 单点登录示例工程（SP 端）的启动类，演示以 SAML 服务提供方身份接入 IDaaS，
 * IdP 注册信息与 SP 签名密钥库在 application.yml 的 cu.auth 配置中声明
 * @author kewen
 * @since 2026-02-14
 */
@SpringBootApplication
public class IdaasSpBootSampleApp {
    /**
     * 示例工程入口
     * @param args 启动参数
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(IdaasSpBootSampleApp.class, args);
    }
}