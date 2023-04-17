package com.kewen.framework.sample.cloud.alibaba.controller;

import com.kewen.framework.common.core.model.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class TestController {

    @RequestMapping("/hello")
    public Result<String> hello(){
        return Result.success("hello");
    }
}
