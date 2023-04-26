package com.kewen.framework.sample.controller;

import com.kewen.framework.common.core.model.Result;
import com.kewen.framework.sample.req.ValidatedReq;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author kewen
 * @since 2023-04-26
 */
@RestController
public class ValidatedTestController {
    @PostMapping("/validated/hello")
    public Result hello(@Validated @RequestBody ValidatedReq req){
        return Result.success();
    }
}
