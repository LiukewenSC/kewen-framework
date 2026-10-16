package com.cu.framework.auth.rbac.controller;

import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.entity.BaseAuth;
import com.cu.framework.auth.rbac.composite.SysAuthMenuComposite;
import com.cu.framework.auth.rbac.model.MenuTypeConstant;
import com.cu.framework.auth.rbac.model.req.MenuApiSaveReq;
import com.cu.framework.auth.rbac.model.resp.MenuApiAndAuthResp;
import com.cu.framework.auth.rbac.model.resp.MenuApiResp;
import com.cu.framework.basic.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * API 菜单管理接口（/menu/api），提供菜单 API 树查询与菜单更新，类上 @AuthMenu 名称为"菜单路径相关接口"
 * @author kewen
 * @since 2026-02-12
 */
@RestController
@RequestMapping("/menu/api")
@AuthMenu(name = "菜单路径相关接口")
public class RbacMenuApiController {

    @Autowired
    SysAuthMenuComposite sysAuthMenuComposite;
    @Autowired
    AuthCurrentUserContextHolder authCurrentUserContextHolder;


    /**
     * 获取全部 API 菜单树（GET /menu/api/tree），包含已删除的菜单
     * @return API 菜单树，节点上附带各自的权限集合体
     */
    @GetMapping("/tree")
    public Result<List<MenuApiAndAuthResp>> menuTree(){
        List<MenuApiAndAuthResp> menuTree = sysAuthMenuComposite.getApiAuthMenuTree(true);
        return Result.success(menuTree);
    }
    /**
     * 更新菜单 API 及其权限集合体（POST /menu/api/update）；
     * 权限类型为 OWNER（基于自身权限）时要求权限集合体非空，否则抛出异常
     * @param req 菜单保存请求，继承 SysMenuApi 并携带权限集合体
     * @return 操作结果，data 恒为 null
     */
    @PostMapping("/update")
    public Result<List<MenuApiResp>> updateMenu(@RequestBody MenuApiSaveReq req){
        if (isOwnerWithoutAuthObject(req)){
            throw new RuntimeException("这样谁都没有权限查看了");
        }
        Collection<BaseAuth> auths = authCurrentUserContextHolder.getAuths();
        sysAuthMenuComposite.updateMenu(req);
        return Result.success(null);
    }

    /**
     * OWNER（基于自身权限）类型的菜单若不给权限集合体，等于谁都没有查看权限。
     * <p>authType 与 OWNER 的比较必须按值而非按引用，Integer 的 == 只在 -128~127 缓存区间内成立。
     * @param req 菜单保存请求
     * @return 需要拒绝该请求时为 true
     */
    static boolean isOwnerWithoutAuthObject(MenuApiSaveReq req) {
        return Objects.equals(req.getAuthType(), MenuTypeConstant.AUTH_TYPE.OWNER)
                && (req.getAuthObject() == null || req.getAuthObject().isEmpty());
    }
}
