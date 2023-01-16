package com.kewen.framework.sample.controller;

import com.kewen.framework.base.common.model.Result;
import com.kewen.framework.sample.feign.HelloFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/hello")
public class HelloController {

    @Autowired
    HelloFeign helloFeign;

    @RequestMapping("/hello")
    public Result<String> hello(){
        Result<String> hello = helloFeign.hello();
        return Result.success(hello.getData());
    }
}