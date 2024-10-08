package com.kewen.framework.boot.common.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * spring 启动过程的打印
 * @author kewen
 * @since 2023-01-03 17:09
 */
@Slf4j
public class PrintSpringApplicationRunListener implements SpringApplicationRunListener {

    private final SpringApplication application;
    private final String[] args;

    public PrintSpringApplicationRunListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }

    @Override
    public void starting() {
        log.info("SpringApplication starting");
    }
    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        log.info("SpringApplication environmentPrepared");
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        log.info("SpringApplication contextPrepared");
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        log.info("SpringApplication contextLoaded");
    }

    @Override
    public void started(ConfigurableApplicationContext context) {
        log.info("SpringApplication started");
    }

    @Override
    public void running(ConfigurableApplicationContext context) {
        log.info("SpringApplication running");
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        log.info("SpringApplication failed");
    }
}
