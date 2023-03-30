package com.kewen.framework.boot.web.listener;

import com.kewen.framework.base.common.factory.YmlPropertySourceFactory;
import com.kewen.framework.base.common.model.SpringConstant;
import com.kewen.framework.base.common.utils.BeanUtil;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.lang.reflect.Constructor;

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
        System.out.println("environmentPrepared");

    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        System.out.println("contextPrepared");

    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        System.out.println("contextLoaded");

    }

    @Override
    public void started(ConfigurableApplicationContext context) {
        System.out.println("started");

    }

    @Override
    public void running(ConfigurableApplicationContext context) {
        System.out.println("running");

    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        System.out.println("failed");

    }
}