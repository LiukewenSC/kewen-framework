package com.cu.framework.sample.basic.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * SpringApplicationRunListener 扩展点示例，通过 spring.factories 注册，
 * 演示监听 Spring Boot 启动生命周期各阶段（本例仅在 starting 阶段打印日志，其余回调为空实现）
 * @author kewen
 * @since 2024-08-29
 */
@Slf4j
public class SpringBootLifeCycleListener implements SpringApplicationRunListener, Ordered {

    /** 正在启动的 SpringApplication 实例，由框架按约定构造器签名注入 */
    private final SpringApplication application;

    /** 启动参数，由框架按约定构造器签名注入 */
    private final String[] args;

    /**
     * 约定构造器，Spring Boot 通过反射以 (SpringApplication, String[]) 签名实例化本监听器
     * @param application 正在启动的 SpringApplication 实例
     * @param args 启动参数
     */
    public SpringBootLifeCycleListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }

    /**
     * 启动流程开始时回调，打印当前监听器类名标识进入 starting 阶段
     * @param bootstrapContext 启动引导上下文
     */
    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        log.info("----"+this.getClass().getName()+" : "+"starting");
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {

    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {

    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {

    }

    @Override
    public void started(ConfigurableApplicationContext context) {

    }

    @Override
    public void running(ConfigurableApplicationContext context) {

    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
