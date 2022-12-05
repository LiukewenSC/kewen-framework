package com.kewen.framework.boot.authority.biz.controller;

import com.kewen.framework.boot.authority.biz.model.req.LoginReq;
import com.kewen.framework.boot.authority.biz.model.resp.LoginResp;
import com.kewen.framework.boot.authority.biz.service.LoginService;
import com.kewen.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kewen
 * @descrpition
 * @since 2022-11-25 21:26
 */
@RestController
@RequestMapping("/login")
@Slf4j
public class LoginController {

    @Autowired
    private LoginService loginService;




    @PostMapping("/login")
    public Result<LoginResp> login(@RequestBody LoginReq loginReq){

        LoginResp login = loginService.login(loginReq);

        return Result.success(login);
    }
}
