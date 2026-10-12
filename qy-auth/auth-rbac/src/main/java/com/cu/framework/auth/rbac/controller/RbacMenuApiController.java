package com.cu.framework.auth.rbac.controller;

import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.AuthUserContext;
import com.cu.framework.auth.core.entity.BaseAuth;
import com.cu.framework.auth.rbac.composite.SysAuthMenuComposite;
import com.cu.framework.auth.rbac.model.MenuTypeConstant;
import com.cu.framework.auth.rbac.model.RbacResult;
import com.cu.framework.auth.rbac.model.req.MenuApiSaveReq;
import com.cu.framework.auth.rbac.model.resp.MenuApiAndAuthResp;
import com.cu.framework.auth.rbac.model.resp.MenuApiResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/menu/api")
@AuthMenu(name = "菜单路径相关接口")
public class RbacMenuApiController {

    @Autowired
    SysAuthMenuComposite sysAuthMenuComposite;


    @GetMapping("/tree")
    public RbacResult<List<MenuApiAndAuthResp>> menuTree(){
        List<MenuApiAndAuthResp> menuTree = sysAuthMenuComposite.getApiAuthMenuTree(true);
        return RbacResult.success(menuTree);
    }
    @PostMapping("/update")
    public RbacResult<List<MenuApiResp>> updateMenu(@RequestBody MenuApiSaveReq req){
        if (req.getAuthType()== MenuTypeConstant.AUTH_TYPE.OWNER && (req.getAuthObject()==null || req.getAuthObject().isEmpty())){
            throw new RuntimeException("这样谁都没有权限查看了");
        }
        Collection<BaseAuth> auths = AuthUserContext.getAuths();
        sysAuthMenuComposite.updateMenu(req);
        return RbacResult.success(null);
    }
}
