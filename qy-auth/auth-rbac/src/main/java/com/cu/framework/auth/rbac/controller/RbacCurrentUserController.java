package com.cu.framework.auth.rbac.controller;

import com.cu.framework.auth.core.AuthUserContext;
import com.cu.framework.auth.core.entity.CurrentUser;
import com.cu.framework.auth.core.entity.BaseAuth;
import com.cu.framework.auth.rbac.composite.SysAuthMenuComposite;
import com.cu.framework.auth.rbac.extension.RbacCurrentUserService;
import com.cu.framework.auth.rbac.model.RbacResult;
import com.cu.framework.auth.rbac.model.req.UpdatePasswordReq;
import com.cu.framework.auth.rbac.model.resp.MenuRouteResp;
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
public class RbacCurrentUserController implements ApplicationContextAware {

    @Autowired
    SysAuthMenuComposite sysAuthMenuComposite;

    RbacCurrentUserService rbacCurrentUserService;

    @GetMapping("/routeMenus")
    public RbacResult<List<MenuRouteResp>> routeTrees(){
        Collection<BaseAuth> auths = AuthUserContext.getAuths();
        List<MenuRouteResp> menuTree = sysAuthMenuComposite.getRouteAuthMenuTree(auths);
        return RbacResult.success(menuTree);
    }
    @PostMapping("/updatePassword")
    public RbacResult<Void> updatePassword(@RequestBody @Validated UpdatePasswordReq req){
        rbacCurrentUserService.updatePassword(AuthUserContext.getCurrentUser(),req.getOldPassword(), req.getNewPassword());
        return RbacResult.success();
    }









    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ObjectProvider<RbacCurrentUserService> beanProvider = applicationContext.getBeanProvider(RbacCurrentUserService.class);
        RbacCurrentUserService ifAvailable = beanProvider.getIfAvailable();
        //没有注入RbacCurrentUserService就不支持
        if(ifAvailable != null){
            rbacCurrentUserService = ifAvailable;
        } else {
            rbacCurrentUserService = new UnsupportedRbacCurrentUserService();
        }
    }

    /**
     * 不支持的当前登录服务
     */
    static class UnsupportedRbacCurrentUserService implements RbacCurrentUserService {

        @Override
        public void updatePassword(CurrentUser currentUser, String oldPassword, String newPassword) {
            throw new UnsupportedOperationException("Not supported updatePassword");

        }
    }
}