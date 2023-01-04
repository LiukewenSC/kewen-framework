package com.kewen.framework.boot.web.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @descrpition 
 * @author kewen
 * @since 2023-01-03 17:09
 */
public class WebSpringApplicationRunListener implements SpringApplicationRunListener {

    private final SpringApplication application;
    private final String[] args;

    public WebSpringApplicationRunListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }

    @Override
    public void starting() {
        System.out.println("starting");
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        System.out.println("environment");

    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        System.out.println("context");

    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        System.out.println("context");

    }

    @Override
    public void started(ConfigurableApplicationContext context) {
        System.out.println("context");

    }

    @Override
    public void running(ConfigurableApplicationContext context) {
        System.out.println("context");

    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        System.out.println("context");
        System.out.println("exception");

    }
}
