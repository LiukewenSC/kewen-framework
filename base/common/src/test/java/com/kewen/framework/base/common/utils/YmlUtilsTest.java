package com.kewen.framework.base.common.utils;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class YmlUtilsTest {

    @Test
    public void parse() {
        Map parse = YmlUtils.parse("classpath:application.yml", "spring.datasource", Map.class);
        System.out.println(parse);
    }
}