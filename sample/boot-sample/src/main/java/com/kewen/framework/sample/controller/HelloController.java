package com.kewen.framework.sample.controller;

import com.kewen.framework.common.core.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Autowired
    Test2Service test2Service;

    @RequestMapping("/uucs/hello")
    public Result hello(){
        return Result.success(test2Service.hello());
    }
}
