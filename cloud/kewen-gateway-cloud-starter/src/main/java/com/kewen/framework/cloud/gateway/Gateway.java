package com.kewen.framework.cloud.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @descrpition 网关启动器
 * @author kewen
 * @since 2022-12-05 15:01
 */
@SpringBootApplication
@EnableDiscoveryClient
public class Gateway {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(Gateway.class,args);
    }
}
