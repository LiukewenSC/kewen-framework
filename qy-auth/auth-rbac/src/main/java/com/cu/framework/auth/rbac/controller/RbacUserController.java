package com.cu.framework.auth.rbac.controller;

import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.rbac.mp.entity.SysUser;
import com.cu.framework.auth.rbac.mp.service.SysUserMpService;
import com.cu.framework.basic.model.IdReq;
import com.cu.framework.basic.model.PageReq;
import com.cu.framework.basic.model.PageResult;
import com.cu.framework.basic.model.Result;
import com.cu.framework.basic.support.utils.MybatisPageExecutor;
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

    /**
     * 查询全部用户列表（GET /rbac/user/list，菜单权限"用户列表"）
     * @return 用户列表
     */
    @GetMapping("/list")
    @AuthMenu(name = "用户列表")
    public Result<List<SysUser>> listUser(){
        List<SysUser> list = sysUserMpService.list();
        return Result.success(list);
    }
    /**
     * 分页查询用户（GET /rbac/user/page，菜单权限"分页"）
     * @param pageReq 分页请求参数
     * @return 用户分页结果
     */
    @GetMapping("/page")
    @AuthMenu(name = "分页")
    public Result<PageResult<SysUser>> pageUser(@Validated PageReq pageReq){
        PageResult<SysUser> result = MybatisPageExecutor.pageAndConvert(pageReq,sysUserMpService);
        return Result.success(result);
    }
    /**
     * 新增用户（POST /rbac/user/add，菜单权限"新增"）
     * @param sysUser 待新增的用户
     * @return 是否新增成功
     */
    @PostMapping("/add")
    @AuthMenu(name = "新增")
    public Result<Boolean> add(@RequestBody SysUser sysUser){
        boolean b = sysUserMpService.save(sysUser);
        return Result.success(b);
    }
    /**
     * 按 ID 修改用户（POST /rbac/user/update），请求体未携带 ID 时抛出异常
     * @param sysUser 待更新的用户，ID 必填
     * @return 是否修改成功
     */
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody SysUser sysUser){
        if (sysUser.getId()==null){
            throw new RuntimeException("用户ID为空");
        }
        boolean b = sysUserMpService.updateById(sysUser);
        return Result.success(b);
    }
    /**
     * 按 ID 删除用户（POST /rbac/user/delete）
     * @param idReq 携带用户 ID 的请求体
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated IdReq idReq){
        boolean b = sysUserMpService.removeById(idReq.getId());
        return Result.success(b);
    }
}
