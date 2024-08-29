package com.kewen.framework.sample.basic.controller;


import com.kewen.framework.basic.model.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author kewen
 * @since 2023-04-26
 */
@RestController
public class FilterOrderTestController {

    @RequestMapping("/filter/hello")
    public Result filter(){
        //IUser iUser = UserContext.get();
        return Result.success();
    }
}
