package com.cu.framework.sample.authcore;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * auth-core 独立验证示例的启动类。
 * <p>
 * 本示例只引入 auth-core-spring-boot-starter：权限模型、菜单规则、权限存储全部由示例自己实现，
 * 不依赖任何权限模型层的代码、数据表或框架建表脚本，用来端到端证明核心层的接缝成立。
 *
 * @author kewen
 * @since 2026-10-20
 */
@SpringBootApplication
@MapperScan("com.cu.framework.sample.authcore.mapper")
public class AuthCoreBootSampleApp {

    /**
     * 启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthCoreBootSampleApp.class, args);
    }
}
