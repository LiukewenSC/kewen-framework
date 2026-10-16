package com.cu.framework.auth.sample;


import com.cu.framework.boot.basic.config.MybatisPlusConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 权限框架完整用法示例（auth-boot-sample）的启动类，演示 @AuthMenu、@AuthDataRange 等权限注解在会议室业务中的接入方式
 *
 * 排除MybatisPlusConfig，再AuthBootSampleConfig中配置MybatisPlus
 * @author kewen
 * @since 2026-02-13
 */
@SpringBootApplication(exclude = {MybatisPlusConfig.class})
public class AuthRbacBootSampleApp {
    /**
     * 示例工程入口
     * @param args 启动参数
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(AuthRbacBootSampleApp.class, args);
    }
}