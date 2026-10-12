package com.cu.framework.auth.rbac.controller;

import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.rbac.model.RbacIdReq;
import com.cu.framework.auth.rbac.model.RbacPageConverter;
import com.cu.framework.auth.rbac.model.RbacPageReq;
import com.cu.framework.auth.rbac.model.RbacPageResult;
import com.cu.framework.auth.rbac.model.RbacResult;
import com.cu.framework.auth.rbac.mp.entity.SysUser;
import com.cu.framework.auth.rbac.mp.service.SysUserMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
/**
 * 用户
 *
 * @author kewen
 * @since 2024-07-29
 */
@RestController
@RequestMapping("/rbac/user")
@AuthMenu(name = "用户相关")
public class RbacUserController {

    @Autowired
    SysUserMpService sysUserMpService;

    @GetMapping("/list")
    @AuthMenu(name = "用户列表")
    public RbacResult<List<SysUser>> listUser(){
        List<SysUser> list = sysUserMpService.list();
        return RbacResult.success(list);
    }
    @GetMapping("/page")
    @AuthMenu(name = "分页")
    public RbacResult<RbacPageResult<SysUser>> pageUser(@Validated RbacPageReq rbacPageReq){
        RbacPageResult<SysUser> result = RbacPageConverter.pageAndConvert(rbacPageReq,sysUserMpService);
        return RbacResult.success(result);
    }
    @PostMapping("/add")
    @AuthMenu(name = "新增")
    public RbacResult<Boolean> add(@RequestBody SysUser sysUser){
        boolean b = sysUserMpService.save(sysUser);
        return RbacResult.success(b);
    }
    @PostMapping("/update")
    public RbacResult<Boolean> update(@RequestBody SysUser sysUser){
        if (sysUser.getId()==null){
            throw new RuntimeException("用户ID为空");
        }
        boolean b = sysUserMpService.updateById(sysUser);
        return RbacResult.success(b);
    }
    @PostMapping("/delete")
    public RbacResult<Boolean> delete(@RequestBody @Validated RbacIdReq rbacIdReq){
        boolean b = sysUserMpService.removeById(rbacIdReq.getId());
        return RbacResult.success(b);
    }
}
