package com.kewen.framework.sample.controller;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HelloService2 {

    @NacosValue(value = "${kewen.test-value}",autoRefreshed = true)
    private String testKewenValue;

    public String getTestKewenValue(){
        log.info("testKewenValue is :"+testKewenValue);
        return testKewenValue;
    }
}
