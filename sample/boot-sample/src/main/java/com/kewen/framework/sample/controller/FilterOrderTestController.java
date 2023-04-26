package com.kewen.framework.sample.controller;

import com.kewen.framework.common.context.UserContext;
import com.kewen.framework.common.core.model.IUser;
import com.kewen.framework.common.core.model.Result;
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
        IUser iUser = UserContext.get();
        return Result.success(iUser);
    }
}
