package com.cu.framework.sample.basic.controller;


import com.cu.framework.basic.model.Result;
import com.cu.framework.sample.basic.req.ValidatedReq;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author kewen
 * @since 2023-04-26
 */
@RestController
public class ValidatedTestController {
    /**
     * 参数校验测试接口，@Validated 触发对请求体 ValidatedReq 中各校验注解的检查，通过后返回成功
     * @param req 携带 NotNull / NotEmpty / AssertTrue 等校验规则的请求体
     * @return 成功响应
     */
    @PostMapping("/validated/hello")
    public Result<Void> hello(@Validated @RequestBody ValidatedReq req){
        return Result.success();
    }
}
