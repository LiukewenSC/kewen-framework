package com.cu.framework.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 * 上下文配置见 {@link com.cu.framework.auth.rbac.security.RbacSecurityAuthCurrentUserContextHolder}
 */
@SpringBootApplication
public class AuthRbacSecurityBootSampleApp {
    public static void main(String[] args) {
        SpringApplication.run(AuthRbacSecurityBootSampleApp.class, args);
    }
}