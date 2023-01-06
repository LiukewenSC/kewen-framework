package com.kewen.framework.sample.extension;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @descrpition 
 * @author kewen
 * @since 2023-01-06 9:53
 */
@Component
public class TestExtension implements BeanFactoryAware, ApplicationContextAware, InitializingBean, DisposableBean {

    BeanFactory beanFactory;




    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("执行 setBeanFactory");

        this.beanFactory=beanFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("执行 setApplicationContext");
    }


    @PostConstruct
    private void init(){
        System.out.println("执行 @PostConstruct");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("执行InitializingBean");
    }

    @Override
    public void destroy() throws Exception {

    }
}
