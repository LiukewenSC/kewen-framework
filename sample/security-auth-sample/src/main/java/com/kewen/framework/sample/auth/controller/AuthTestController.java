package com.kewen.framework.sample.auth.controller;

import com.kewen.framework.auth.sys.model.SysUserInfo;
import com.kewen.framework.common.core.model.Result;
import com.kewen.framework.auth.context.AuthUserContext;
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
        SysUserInfo authUserInfo = AuthUserContext.get();
        return Result.success(authUserInfo);
    }
}
