package com.cu.framework.boot.magic.controller;


import com.cu.framework.basic.model.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kewen
 * @since 2023-05-06
 */
@RestController
public class HelloController {

    /**
     * 映射 /hello/hello，返回固定文本 hello 的成功结果
     * @return 成功结果，数据为字符串 hello
     */
    @RequestMapping("/hello/hello")
    public Result hello(){
        return Result.success("hello");
    }

    /**
     * 映射 /magic/user/all，当前实现只返回固定文本 hello，并非真实用户数据
     * @return 成功结果，数据为字符串 hello
     */
    @RequestMapping("/magic/user/all")
    public Result allUser(){
        return Result.success("hello");
    }


}
