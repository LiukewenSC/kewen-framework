package com.kewen.framework.sample.auth.controller;

import com.kewen.framework.base.common.model.Result;
import com.kewen.framework.base.auth.context.AuthUserContext;
import com.kewen.framework.boot.auth.sys.SysAuthUserInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author kewen
 * @since 2023-04-10
 */
@RestController
public class AuthTestController {

    @GetMapping("/hello")
    public Result hello(){
        SysAuthUserInfo authUserInfo = AuthUserContext.get();
        return Result.success(authUserInfo);
    }
}
