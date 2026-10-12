package com.cu.framework.sample.controller;

import com.cu.framework.basic.model.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 2026/02/14
 *
 * @author kewen
 * @since 4.24.0-SP01
 */
@RestController
@RequestMapping
public class HelloController {
    /**
     * 示例接口，返回固定问候语，用于验证接入 IDaaS 单点登录后应用可正常响应请求
     * @return 固定字符串 hello
     */
    @RequestMapping("/hello")
    public Result<String> hello() {
        return Result.success("hello");
    }
    /**
     * 根路径示例接口，返回固定字符串 index
     * @return 固定字符串 index
     */
    @RequestMapping("/")
    public Result<String> hello2() {
        return Result.success("index");
    }
}
