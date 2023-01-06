package com.kewen.framework.sample.service;

import org.junit.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

/**
 * @descrpition 
 * @author kewen
 * @since 2023-01-06 10:33
 */
public class TestYamlPropertiesFactoryBean {

    /**
     * 测试YamlPropertiesFactoryBean的可用性，事实证明可用的
     */
    @Test
    public void t1(){
        YamlPropertiesFactoryBean bean = new YamlPropertiesFactoryBean();
        ClassPathResource resource = new ClassPathResource("application.yml");
        bean.setResources(resource);
        Properties properties = bean.getObject();

        System.out.println(properties);
    }
}
