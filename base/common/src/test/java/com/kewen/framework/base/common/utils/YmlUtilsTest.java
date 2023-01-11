package com.kewen.framework.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

public class YmlUtilsTest {

    @Test
    public void parse() {
        Map parse = YmlUtils.parse("classpath:application.yml", "spring.datasource", Map.class);
        System.out.println(parse);
    }
    @Test
    public void parse2Properties(){
        Properties properties = YmlUtils.parse2Properties("classpath:application.yml");
        String password = properties.getProperty("spring.datasource.password");
        Assert.assertEquals("pword",password);
    }

}