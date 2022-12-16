package com.kewen.framework.cloud.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.oauth2.config.annotation.configuration.ClientDetailsServiceConfiguration;

/**
 * @author kewen
 * @descrpition
 * @since 2022-12-07 15:59
 */
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class CloudSecurityServer {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(CloudSecurityServer.class, args);
    }
}
