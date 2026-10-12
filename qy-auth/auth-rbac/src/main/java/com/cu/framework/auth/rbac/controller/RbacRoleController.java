package com.cu.framework.auth.rbac.controller;

import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.rbac.model.RbacIdReq;
import com.cu.framework.auth.rbac.model.RbacPageConverter;
import com.cu.framework.auth.rbac.model.RbacPageReq;
import com.cu.framework.auth.rbac.model.RbacPageResult;
import com.cu.framework.auth.rbac.model.RbacResult;
import com.cu.framework.auth.rbac.mp.entity.SysRole;
import com.cu.framework.auth.rbac.mp.service.SysRoleMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rbac/role")
public class RbacRoleController {


    @Autowired
    SysRoleMpService sysRoleMpService;

    @GetMapping("/list")
    @AuthMenu(name = "角色列表")
    public RbacResult<List<SysRole>> listRole(){
        List<SysRole> list = sysRoleMpService.list();
        return RbacResult.success(list);
    }
    @GetMapping("/page")
    @AuthMenu(name = "角色分页")
    public RbacResult<RbacPageResult<SysRole>> pageRole(@Validated RbacPageReq req){
        RbacPageResult<SysRole> sysRoleRbacPageResult = RbacPageConverter.pageAndConvert(req, sysRoleMpService);
        return RbacResult.success(sysRoleRbacPageResult);
    }
    @PostMapping("/add")
    @AuthMenu(name = "添加角色")
    public RbacResult<Boolean> add(@RequestBody SysRole sysRole){
        boolean b = sysRoleMpService.save(sysRole);
        return RbacResult.success(b);
    }
    @PostMapping("/update")
    @AuthMenu(name = "修改角色")
    public RbacResult<Boolean> update(@RequestBody SysRole sysUser){
        if (sysUser.getId()==null){
            throw new RuntimeException("用户ID为空");
        }
        boolean b = sysRoleMpService.updateById(sysUser);
        return RbacResult.success(b);
    }
    @PostMapping("/delete")
    @AuthMenu(name = "删除角色")
    public RbacResult<Boolean> delete(@RequestBody @Validated RbacIdReq rbacIdReq){
        boolean b = sysRoleMpService.removeById(rbacIdReq.getId());
        return RbacResult.success(b);
    }
}
