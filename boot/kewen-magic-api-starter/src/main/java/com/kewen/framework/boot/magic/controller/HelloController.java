package com.kewen.framework.boot.magic.controller;

import com.kewen.framework.common.core.model.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kewen
 * @since 2023-05-06
 */
@RestController
public class HelloController {

    @RequestMapping("/hello/hello")
    public Result hello(){
        return Result.success("hello");
    }

    @RequestMapping("/magic/user/all")
    public Result allUser(){
        return Result.success("hello");
    }


}
