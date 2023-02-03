package com.kewen.framework.sample.security.sso;

import com.kewen.framework.cloud.security.client.config.OauthClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author kewen
 * @descrpition
 * @since 2022-12-16 17:52
 */
@SpringBootApplication
//@ComponentScan("com.kewen")
public class SecuritySampleApp {
    public static void main(String[] args) {
        //启动类必须引用到配置@EnableOAuth2Sso的，可能跟springboot的启动顺序有关
        SpringApplication.run(new Class[]{SecuritySampleApp.class, OauthClientConfig.class}, args);
    }
}
