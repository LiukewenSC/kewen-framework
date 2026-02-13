package com.kewen.framework.sample.controller;

import com.kewen.framework.basic.model.Result;
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
    @RequestMapping("/hello")
    public Result hello() {
        return Result.success("hello");
    }
    @RequestMapping("/")
    public Result hello2() {
        return Result.success("index");
    }
}
