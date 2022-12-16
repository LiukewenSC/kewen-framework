package com.kewen.framework.sample.security.sso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author kewen
 * @descrpition
 * @since 2022-12-16 17:52
 */
@SpringBootApplication
@EnableOAuth2Sso
public class SecuritySampleApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(SecuritySampleApp.class, args);
    }
}
