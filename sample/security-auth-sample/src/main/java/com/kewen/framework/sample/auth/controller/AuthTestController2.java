package com.kewen.framework.sample.auth.controller;

import com.kewen.framework.auth.context.AuthUserContext;
import com.kewen.framework.auth.sys.model.SysUserInfo;
import com.kewen.framework.boot.auth.security.annotation.SecurityIgnore;
import com.kewen.framework.common.core.model.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author kewen
 * @since 2023-04-10
 */
@RestController
@RequestMapping("/hello2")
@SecurityIgnore
public class AuthTestController2 {

    @GetMapping("/hello")
    public Result hello(){
        return Result.success("hello no auth");
    }

    @GetMapping("/authHello")
    public Result authHello(){
        SysUserInfo authUserInfo = AuthUserContext.get();
        return Result.success(authUserInfo);
    }
    @GetMapping("/ignore")
    public Result ignore(){
        SysUserInfo authUserInfo = AuthUserContext.get();
        return Result.success(authUserInfo);
    }

}
