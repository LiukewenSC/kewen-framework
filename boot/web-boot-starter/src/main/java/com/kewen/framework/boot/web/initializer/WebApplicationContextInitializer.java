package com.kewen.framework.boot.web.initializer;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @descrpition 
 * @author kewen
 * @since 2023-01-03 16:41
 */
@Component
public class WebApplicationContextInitializer implements ApplicationContextInitializer {

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

        ConfigurableListableBeanFactory beanFactory = configurableApplicationContext.getBeanFactory();

    }
}
