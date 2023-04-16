package com.kewen.framework.boot.auth.web;

import com.kewen.framework.base.authority.model.resp.LoginReq;
import com.kewen.framework.base.authority.model.resp.LoginResp;
import com.kewen.framework.boot.auth.web.service.LoginService;
import com.kewen.framework.base.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kewen
 * @descrpition
 * @since 2022-11-25 21:26
 */
@RestController
@Slf4j
public class LoginController {

    @Autowired
    private LoginService loginService;


    @PostMapping("${kewen.auth.login-endpoint}")
    public Result<LoginResp> login(@RequestBody LoginReq loginReq){

        LoginResp login = loginService.login(loginReq);

        return Result.success(login);
    }
}
