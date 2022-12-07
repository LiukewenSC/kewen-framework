package com.kewen.framework.sample.controller;

import com.kewen.framework.base.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kewen
 * @descrpition 测试类
 * @since 202
 * 2-12-07 15:29
 */
@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @RequestMapping("/test")
    public Result<String> test(){
        log.info("测试 hello world ");
        return Result.success("hello world");
    }
}
