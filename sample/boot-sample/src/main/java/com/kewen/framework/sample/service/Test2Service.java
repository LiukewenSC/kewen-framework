package com.kewen.framework.sample.service;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @descrpition 
 * @author kewen
 * @since 2023-01-05 16:54
 */
@Component
public class Test2Service implements InitializingBean {



    public Test2Service(ObjectProvider<TestService> testServices) {
        //TestService ifAvailable = testServices.getIfAvailable();
        List<TestService> collect = testServices.stream().collect(Collectors.toList());
        TestService testServices1 = testServices.orderedStream().findFirst().get();
    }

    @PostConstruct
    public void hello(){
        System.out.println("Test2Service hello");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("执行Test2Service afterProperties");
    }
}
