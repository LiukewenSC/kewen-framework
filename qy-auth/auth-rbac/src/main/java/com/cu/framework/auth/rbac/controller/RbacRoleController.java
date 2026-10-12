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

/**
 * 角色管理接口（/rbac/role），提供角色的列表、分页、新增、修改与删除，方法上的 @AuthMenu 用于生成菜单权限标识
 * @author kewen
 * @since 2026-02-12
 */
@RestController
@RequestMapping("/rbac/role")
public class RbacRoleController {


    @Autowired
    SysRoleMpService sysRoleMpService;

    /**
     * 查询全部角色列表（GET /rbac/role/list，菜单权限"角色列表"）
     * @return 角色列表
     */
    @GetMapping("/list")
    @AuthMenu(name = "角色列表")
    public RbacResult<List<SysRole>> listRole(){
        List<SysRole> list = sysRoleMpService.list();
        return RbacResult.success(list);
    }
    /**
     * 分页查询角色（GET /rbac/role/page，菜单权限"角色分页"）
     * @param req 分页请求参数
     * @return 角色分页结果
     */
    @GetMapping("/page")
    @AuthMenu(name = "角色分页")
    public RbacResult<RbacPageResult<SysRole>> pageRole(@Validated RbacPageReq req){
        RbacPageResult<SysRole> sysRoleRbacPageResult = RbacPageConverter.pageAndConvert(req, sysRoleMpService);
        return RbacResult.success(sysRoleRbacPageResult);
    }
    /**
     * 新增角色（POST /rbac/role/add，菜单权限"添加角色"）
     * @param sysRole 待新增的角色
     * @return 是否新增成功
     */
    @PostMapping("/add")
    @AuthMenu(name = "添加角色")
    public RbacResult<Boolean> add(@RequestBody SysRole sysRole){
        boolean b = sysRoleMpService.save(sysRole);
        return RbacResult.success(b);
    }
    /**
     * 按 ID 修改角色（POST /rbac/role/update，菜单权限"修改角色"），请求体未携带 ID 时抛出异常
     * @param sysUser 待更新的角色，ID 必填
     * @return 是否修改成功
     */
    @PostMapping("/update")
    @AuthMenu(name = "修改角色")
    public RbacResult<Boolean> update(@RequestBody SysRole sysUser){
        if (sysUser.getId()==null){
            throw new RuntimeException("用户ID为空");
        }
        boolean b = sysRoleMpService.updateById(sysUser);
        return RbacResult.success(b);
    }
    /**
     * 按 ID 删除角色（POST /rbac/role/delete，菜单权限"删除角色"）
     * @param rbacIdReq 携带角色 ID 的请求体
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @AuthMenu(name = "删除角色")
    public RbacResult<Boolean> delete(@RequestBody @Validated RbacIdReq rbacIdReq){
        boolean b = sysRoleMpService.removeById(rbacIdReq.getId());
        return RbacResult.success(b);
    }
}
