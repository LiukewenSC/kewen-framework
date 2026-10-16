package com.cu.framework.auth.rbac.controller;

import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.entity.BaseAuth;
import com.cu.framework.auth.rbac.composite.SysAuthMenuComposite;
import com.cu.framework.auth.rbac.extension.RbacCurrentUserService;
import com.cu.framework.auth.rbac.model.req.UpdatePasswordReq;
import com.cu.framework.auth.rbac.model.resp.MenuRouteResp;
import com.cu.framework.basic.model.Result;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

/**
 * @author kewen
 * @since 2024-05-11
 */
@RestController
@RequestMapping("/currentUser")
public class RbacCurrentUserController {

    @Autowired
    SysAuthMenuComposite sysAuthMenuComposite;

    @Autowired
    RbacCurrentUserService rbacCurrentUserService;
    @Autowired
    AuthCurrentUserContextHolder authCurrentUserContextHolder;

    /**
     * 获取当前登录人有权限的路由菜单树（GET /currentUser/routeMenus），基于当前用户权限集合过滤
     * @return 路由菜单树
     */
    @GetMapping("/routeMenus")
    public Result<List<MenuRouteResp>> routeTrees(){
        Collection<BaseAuth> auths = authCurrentUserContextHolder.getAuths();
        List<MenuRouteResp> menuTree = sysAuthMenuComposite.getRouteAuthMenuTree(auths);
        return Result.success(menuTree);
    }
    /**
     * 当前登录人修改自己的密码（POST /currentUser/updatePassword），委托给容器中的 RbacCurrentUserService 处理
     * @param req 修改密码请求，携带旧密码与新密码
     * @return 操作结果
     */
    @PostMapping("/updatePassword")
    public Result<Void> updatePassword(@RequestBody @Validated UpdatePasswordReq req){
        rbacCurrentUserService.updatePassword(authCurrentUserContextHolder.getUser(),req.getOldPassword(), req.getNewPassword());
        return Result.success();
    }
}