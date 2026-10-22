package com.cu.framework.rbac.security;

import com.cu.framework.auth.rbac.composite.SysUserComposite;
import com.cu.framework.auth.rbac.composite.model.DeptWithParents;
import com.cu.framework.auth.rbac.composite.model.ParentDept;
import com.cu.framework.auth.rbac.composite.model.User;
import com.cu.framework.auth.rbac.composite.model.UserAuthInfo;
import com.cu.framework.auth.rbac.model.UserAuthObject;
import com.cu.framework.auth.rbac.mp.entity.SysUser;
import com.cu.framework.auth.rbac.mp.entity.SysUserCredential;
import com.cu.framework.authentication.model.SecurityUser;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 认证主体装载在权限体缺失与凭证缺失这两种状态下的对外结果。
 * <p>
 * 仓库内不引入 mock 库，桩直接实现 {@link SysUserComposite} 的四个方法
 *
 * @author kewen
 * @since 2026-10-22
 */
public class RbacSecurityUserDetailsServiceTest {

    /** 只按预置结果回答用户权限对象的桩 */
    private static class StubComposite implements SysUserComposite {

        private final UserAuthObject userAuthObject;

        private StubComposite(UserAuthObject userAuthObject) {
            this.userAuthObject = userAuthObject;
        }

        @Override
        public List<ParentDept> listAncestorDepts(Long deptId) {
            return Collections.emptyList();
        }

        @Override
        public SysUserCredential getUserCredentialByUsername(String username) {
            return null;
        }

        @Override
        public UserAuthObject getUserAuthObjectByUsername(String username) {
            return userAuthObject;
        }

        @Override
        public boolean updatePassword(String username, String encryptedNewPassword) {
            return false;
        }
    }

    private static UserAuthObject userAuthObject(boolean withCredential, UserAuthInfo authInfo) {
        UserAuthObject object = new UserAuthObject();
        object.setSysUser(new SysUser().setId(7L).setName("使用者").setUsername("user7"));
        if (withCredential) {
            SysUserCredential credential = new SysUserCredential();
            credential.setPassword("{noop}pw");
            credential.setEnabled(true);
            object.setSysUserCredential(credential);
        }
        object.setAuthInfo(authInfo);
        return object;
    }

    private static RbacSecurityUserDetailsService service(UserAuthObject userAuthObject) {
        RbacSecurityUserDetailsService service = new RbacSecurityUserDetailsService();
        service.setSysUserComposite(new StubComposite(userAuthObject));
        return service;
    }

    private static Set<String> authorityStrings(SecurityUser securityUser) {
        Set<String> strings = new HashSet<>();
        securityUser.getAuthorities().forEach(a -> strings.add(a.getAuthority()));
        return strings;
    }

    @Test
    public void credentiallessUserStillLoadsAsPrincipal() {
        // 无凭证 -> 装载器提前返回，权限体为 null，这是合法状态而不是异常
        SecurityUser securityUser = service(userAuthObject(false, null))
                .loadUserByUsername("user7");
        Assert.assertNotNull(securityUser);
        Assert.assertEquals(Long.valueOf(7L), securityUser.getId());
        Assert.assertEquals("user7", securityUser.getUsername());
        Assert.assertTrue("权限体缺失时权限集合为空", securityUser.getUserAuthorities().isEmpty());
    }

    @Test
    public void missingAuthInfoDoesNotThrow() {
        try {
            service(userAuthObject(true, null)).loadUserByUsername("user7");
        } catch (NullPointerException e) {
            Assert.fail("权限体缺失不得让认证装载抛 NPE: " + e);
        }
    }

    @Test
    public void authoritiesAreTakenFromTheAuthInfo() {
        UserAuthInfo authInfo = new UserAuthInfo();
        authInfo.setUser(new User(7L, "使用者"));
        DeptWithParents group = new DeptWithParents(4L, "组");
        group.getParents().add(new ParentDept(2L, "中心", 1));
        authInfo.addDepts(group);

        SecurityUser securityUser = service(userAuthObject(true, authInfo)).loadUserByUsername("user7");
        Assert.assertEquals(new HashSet<>(Arrays.asList("USER__7", "DEPT__4", "RELATIONDEPT__2")),
                authorityStrings(securityUser));
    }

    @Test
    public void emptyAuthInfoProducesNoAuthorities() {
        // 权限体存在但用户部分缺失：给出空集合，让请求方按"无权限"处理
        SecurityUser securityUser = service(userAuthObject(true, new UserAuthInfo()))
                .loadUserByUsername("user7");
        Assert.assertEquals(0, securityUser.getUserAuthorities().size());
        Assert.assertEquals(0, new ArrayList<>(securityUser.getAuthorities()).size());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void unknownUserIsNotFound() {
        service(null).loadUserByUsername("ghost");
    }
}
