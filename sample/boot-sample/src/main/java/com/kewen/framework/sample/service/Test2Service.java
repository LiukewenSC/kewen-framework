package com.kewen.framework.sample.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * @descrpition 
 * @author kewen
 * @since 2023-01-05 16:54
 */
@Component
public class Test2Service {



    public Test2Service(ObjectProvider<TestService> testServices) {
        TestService ifAvailable = testServices.getIfAvailable();

    }

    public void hello(){
        System.out.println("hello");
    }

}
