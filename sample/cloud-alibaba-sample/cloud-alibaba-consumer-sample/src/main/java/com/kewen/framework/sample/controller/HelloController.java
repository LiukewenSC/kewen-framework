package com.kewen.framework.sample.controller;

import com.kewen.framework.common.core.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/hello")
@Slf4j
public class HelloController {

    @Autowired
    HelloService helloService;
    @Autowired
    HelloService2 helloService2;

    ExecutorService executorService = Executors.newFixedThreadPool(5);



    @RequestMapping("/testValue")
    public Result<String> testValue() throws InterruptedException {
        return Result.success(
                helloService.testValue()
        );
    }
    @RequestMapping("/testValue2")
    public Result<String> testValue2() throws InterruptedException {
        return Result.success(
                helloService2.getTestKewenValue()
        );
    }
    @RequestMapping("/hello")
    public Result<String> hello() throws InterruptedException {
        return Result.success(
                helloService.hello()
        );
    }
    @RequestMapping("/flowRule")
    public Result flowRule() throws InterruptedException, ExecutionException {
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            helloService.sleep(1000);
            final int count=i+1;
            Future<String> submit = executorService.submit(() -> {
                System.out.println("flowRule请求第 " + count + " 次");
                return helloService.flowRule();
            });
            futures.add(i,submit);
        }
        List<String> collect = futures.stream().map(f -> {
            try {
                return f.get();
            } catch (Exception e) {
                log.error("失败:"+e.getMessage());
                return e.getMessage();
            }
        }).collect(Collectors.toList());
        return Result.success( collect);
    }
    @RequestMapping("/degradeRule")
    public Result degradeRule() throws InterruptedException, ExecutionException {
        List<String> collect = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            final int count =i;
            executorService.submit(()->{
                System.out.println("degradeRule请求第 " + (count+1) + " 次");
                collect.add(helloService.degradeRule());
            });
        }
        helloService.sleep(10000);
        return Result.success( collect);
    }
    //@RequestMapping("/degradeRule")
    public Result degradeRule1() throws InterruptedException, ExecutionException {
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            //helloService.sleep(1000);
            final int count=i+1;
            Future<String> submit = executorService.submit(() -> {
                System.out.println("degradeRule请求第 " + count + " 次");
                return helloService.degradeRule();
            });
            futures.add(i,submit);
        }
        List<String> collect = futures.stream().map(f -> {
            try {
                return f.get();
            } catch (Exception e) {
                log.error("失败:"+e.getMessage());
                return e.getMessage();
            }
        }).collect(Collectors.toList());
        return Result.success( collect);
    }
}