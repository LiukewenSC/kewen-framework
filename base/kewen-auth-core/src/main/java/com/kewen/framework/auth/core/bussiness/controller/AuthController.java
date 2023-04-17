package com.kewen.framework.auth.core.bussiness.controller;

import com.kewen.framework.auth.core.model.AuthUserInfo;
import com.kewen.framework.auth.core.context.AuthUserContext;
import com.kewen.framework.common.core.model.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @PostMapping("/currentUser")
    public Result currentUser(){
        AuthUserInfo authUserInfo = AuthUserContext.get();
        return Result.success(authUserInfo);
    }
}
