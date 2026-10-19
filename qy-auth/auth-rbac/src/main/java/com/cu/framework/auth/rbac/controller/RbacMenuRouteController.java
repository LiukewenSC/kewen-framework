package com.cu.framework.auth.rbac.controller;

import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.AuthMenuDoc;
import com.cu.framework.auth.rbac.composite.SysAuthMenuComposite;
import com.cu.framework.auth.rbac.model.resp.MenuRouteResp;
import com.cu.framework.basic.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单路由管理接口（/menu/route），类上 @AuthMenu 名称为"菜单路由相关接口"
 * @author kewen
 * @since 2026-02-12
 */
@RestController
@RequestMapping("/menu/route")
@AuthMenu
@AuthMenuDoc(name = "菜单路由相关接口")
public class RbacMenuRouteController {

    @Autowired
    SysAuthMenuComposite sysAuthMenuComposite;

    /**
     * 获取所有菜单路由
     * @return
     */
    @GetMapping("/tree")
    public Result<List<MenuRouteResp>> routeTrees(){
        List<MenuRouteResp> menuTree = sysAuthMenuComposite.getRouteAuthMenuTree(true);
        return Result.success(menuTree);
    }

}
