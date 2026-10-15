package com.cu.framework.sample.basic.controller;


import com.cu.framework.basic.model.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author kewen
 * @since 2023-04-26
 */
@RestController
public class FilterOrderTestController {

    /**
     * 过滤器执行顺序测试接口，请求经过各过滤器后仅返回成功结果，无业务逻辑
     * @return 成功响应
     */
    @RequestMapping("/filter/hello")
    public Result<?> filter(){
        return Result.success();
    }
}
