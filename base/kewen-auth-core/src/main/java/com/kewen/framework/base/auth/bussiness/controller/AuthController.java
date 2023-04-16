package com.kewen.framework.base.auth.bussiness.controller;

import com.kewen.framework.base.auth.AuthUserInfo;
import com.kewen.framework.base.auth.context.AuthUserContext;
import com.kewen.framework.base.common.model.Result;
import org.springframework.web.bind.annotation.PostMapping;

public class AuthController {
    @PostMapping("/currentUser")
    public Result currentUser(){
        AuthUserInfo authUserInfo = AuthUserContext.get();
        return Result.success(authUserInfo);
    }
}
