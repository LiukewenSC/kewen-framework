package com.kewen.framework.cloud.eureka.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-12-05 14:29
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServer {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(EurekaServer.class);
    }
}
