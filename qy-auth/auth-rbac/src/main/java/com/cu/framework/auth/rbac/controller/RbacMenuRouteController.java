package com.cu.framework.auth.rbac.controller;

import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.rbac.composite.SysAuthMenuComposite;
import com.cu.framework.auth.rbac.model.RbacResult;
import com.cu.framework.auth.rbac.model.resp.MenuRouteResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/menu/route")
@AuthMenu(name = "菜单路由相关接口")
public class RbacMenuRouteController {

    @Autowired
    SysAuthMenuComposite sysAuthMenuComposite;

    /**
     * 获取所有菜单路由
     * @return
     */
    @GetMapping("/tree")
    public RbacResult<List<MenuRouteResp>> routeTrees(){
        List<MenuRouteResp> menuTree = sysAuthMenuComposite.getRouteAuthMenuTree(true);
        return RbacResult.success(menuTree);
    }

}
