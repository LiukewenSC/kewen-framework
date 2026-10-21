package com.cu.framework.auth.rbac.controller;

import com.cu.framework.auth.rbac.composite.model.SimpleAuthObject;
import com.cu.framework.auth.rbac.composite.model.User;
import com.cu.framework.auth.rbac.model.MenuTypeConstant;
import com.cu.framework.auth.rbac.model.req.MenuApiSaveReq;
import org.junit.Assert;
import org.junit.Test;

/**
 * @descrpition 菜单更新接口的 OWNER 权限集合体校验分支
 * @author kewen
 * @since 2026-10-13
 */
public class RbacMenuApiControllerTest {

    private MenuApiSaveReq req(Integer authType, SimpleAuthObject authObject) {
        MenuApiSaveReq req = new MenuApiSaveReq();
        req.setAuthType(authType);
        req.setAuthObject(authObject);
        return req;
    }

    private SimpleAuthObject authObjectWithOneUser() {
        SimpleAuthObject object = new SimpleAuthObject();
        object.addUsers(new User(1L, "用户1"));
        return object;
    }

    @Test
    public void authTypeNullIsNotOwner() {
        Assert.assertFalse("authType 为空时不应命中 OWNER 分支，也不得抛 NPE",
                RbacMenuApiController.isOwnerWithoutAuthObject(req(null, null)));
        Assert.assertFalse(RbacMenuApiController.isOwnerWithoutAuthObject(
                req(null, new SimpleAuthObject())));
    }

    @Test
    public void ownerWithMissingAuthObjectIsRejected() {
        Assert.assertTrue(RbacMenuApiController.isOwnerWithoutAuthObject(
                req(MenuTypeConstant.AUTH_TYPE.OWNER.getCode(), null)));
        Assert.assertTrue("权限集合体为空集合同样应被拒绝",
                RbacMenuApiController.isOwnerWithoutAuthObject(
                        req(MenuTypeConstant.AUTH_TYPE.OWNER.getCode(), new SimpleAuthObject())));
    }

    @Test
    public void ownerWithAuthObjectIsAccepted() {
        Assert.assertFalse(RbacMenuApiController.isOwnerWithoutAuthObject(
                req(MenuTypeConstant.AUTH_TYPE.OWNER.getCode(), authObjectWithOneUser())));
    }

    @Test
    public void parentTypeNeverRejected() {
        Assert.assertFalse(RbacMenuApiController.isOwnerWithoutAuthObject(
                req(MenuTypeConstant.AUTH_TYPE.PARENT.getCode(), null)));
        Assert.assertFalse(RbacMenuApiController.isOwnerWithoutAuthObject(
                req(MenuTypeConstant.AUTH_TYPE.PARENT.getCode(), new SimpleAuthObject())));
    }
}
