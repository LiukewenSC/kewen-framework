package com.kewen.framework.common.core.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Properties;

public class YmlUtilsTest {

    @Test
    public void parse() {
        Map map = YmlUtils.parse("classpath:application.yml", "spring.datasource", Map.class);
        Assert.assertEquals("pword",map.get("password"));
    }
    @Test
    public void parse2Properties(){
        Properties properties = YmlUtils.parse2Properties("classpath:application.yml");
        String password = properties.getProperty("spring.datasource.password");
        Assert.assertEquals("pword",password);
    }

}