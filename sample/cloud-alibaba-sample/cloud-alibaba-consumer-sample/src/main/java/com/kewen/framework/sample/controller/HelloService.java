package com.kewen.framework.sample.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.kewen.framework.common.core.model.Result;
import com.kewen.framework.sample.feign.HelloFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RefreshScope
public class HelloService {

    @Autowired
    private HelloFeign helloFeign;

    @Value(value = "${testvalue}")
    private String testValue;

    public String testValue() {
        log.info("testValue is "+testValue);
        return testValue;
    }

    public String hello() throws InterruptedException {
        sleep(1000);
        Result<String> hello = helloFeign.hello();
        return hello.getData();
    }

    @SentinelResource(value = "flowRule", blockHandler = "flowRuleFallback")
    public String flowRule() {
        Result<String> hello = helloFeign.hello();
        sleep(5000);
        return hello.getData();
    }

    @SentinelResource(value = "degradeRule", blockHandler = "degradeRuleFallback")
    public String degradeRule() {
        Result<String> hello = helloFeign.hello();
        sleep(1000);
        return hello.getData();
    }

    public String degradeRuleFallback(BlockException e) {
        return "服务被降级了 : " + e.getMessage();
    }

    public String flowRuleFallback(BlockException e) {
        return "服务被限流了 : " + e.getMessage();
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
