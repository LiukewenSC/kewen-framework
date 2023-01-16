package com.kewen.framework.sample.extension;

import com.kewen.framework.sample.controller.TestService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 * @descrpition 
 * @author kewen
 * @since 2023-01-06 10:29
 */
@Component
public class TestFactoryBean implements FactoryBean<TestService> {


    @Override
    public TestService getObject() throws Exception {
        return new TestService();
    }

    @Override
    public Class<?> getObjectType() {
        return TestService.class;
    }
}
