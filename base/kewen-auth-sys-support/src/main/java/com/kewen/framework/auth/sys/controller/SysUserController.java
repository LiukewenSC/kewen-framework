package com.kewen.framework.auth.sys.controller;

import com.kewen.framework.auth.sys.model.req.UserEditReq;
import com.kewen.framework.auth.sys.model.req.UserPageReq;
import com.kewen.framework.auth.sys.model.resp.UserResp;
import com.kewen.framework.auth.sys.mp.entity.SysRole;
import com.kewen.framework.auth.sys.service.SysUserService;
import com.kewen.framework.common.core.model.IdReq;
import com.kewen.framework.common.core.model.PageResult;
import com.kewen.framework.common.core.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author kewen
 * @since 2023-04-19
 */
@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    SysUserService sysUserService;
    @GetMapping("/pageUser")
    public Result pageUser(@Validated UserPageReq req) {
        PageResult<UserResp> page = sysUserService.pageUser(req);
        return Result.success(page);
    }
    @PostMapping("/addUser")
    public Result addUser( @RequestBody UserEditReq req) {
        sysUserService.addUser(req);
        return Result.success();
    }
    @PostMapping("/updateUser")
    public Result updateUser( @RequestBody UserEditReq req) {
        sysUserService.updateUser(req);
        return Result.success();
    }
    @PostMapping("/deleteUser")
    public Result deleteUser(@Validated @RequestBody IdReq id) {
        sysUserService.deleteUser(id.getId());
        return Result.success();
    }


    @GetMapping("/pageRole")
    public Result pageRole(@Validated UserPageReq req) {
        PageResult<SysRole> page = sysUserService.pageRole(req);
        return Result.success(page);
    }

    @PostMapping("/addRole")
    public Result addRole( @RequestBody SysRole req) {
        sysUserService.addRole(req);
        return Result.success();
    }
    @PostMapping("/updateRole")
    public Result updateRole( @RequestBody SysRole req) {
        sysUserService.updateRole(req);
        return Result.success();
    }
    @PostMapping("/deleteRole")
    public Result deleteRole(@Validated @RequestBody IdReq id) {
        sysUserService.deleteRole(id.getId());
        return Result.success();
    }

}
