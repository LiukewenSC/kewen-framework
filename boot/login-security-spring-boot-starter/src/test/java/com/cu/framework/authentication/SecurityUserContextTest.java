package com.cu.framework.authentication;

import com.cu.framework.authentication.exception.NoLoginException;
import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.model.SecurityUserAuthority;
import com.cu.framework.authentication.oauth2.Oauth2SecurityUser;
import com.cu.framework.authentication.saml.SamlSecurityUser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

/**
 * 当前登录用户的取法：认证主体一律是 SecurityUser，取用户不碰账号存储
 *
 * @author kewen
 * @since 2026-10-18
 */
public class SecurityUserContextTest {

    @After
    public void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private SecurityUser localUser() {
        return new SecurityUser()
                .setId(1L)
                .setUsername("admin")
                .setName("管理员")
                .setEnabled(true)
                .setUserAuthorities(Collections.singletonList(new SecurityUserAuthority("ROLE_ADMIN", null)));
    }

    /**
     * 没有认证对象时按未登录处理
     */
    @Test(expected = NoLoginException.class)
    public void absentAuthenticationIsNotLoggedIn() {
        SecurityUserContext.getCurrentUser();
    }

    /**
     * 匿名访问的主体是字符串，不能被当成登录用户
     */
    @Test(expected = NoLoginException.class)
    public void anonymousAuthenticationIsNotLoggedIn() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
        SecurityUserContext.getCurrentUser();
    }

    /**
     * 账号密码与三种单点登录的主体都走同一条取法，且返回的就是安全上下文里的那个实例
     */
    @Test
    public void allProtocolsReadTheSameInstance() {
        Oauth2SecurityUser oauth2Principal = Oauth2SecurityUser.of(localUser(), null, null, null);
        SamlSecurityUser samlPrincipal = SamlSecurityUser.of(localUser(), null, null, "demo-saml");

        assertReturnsSameInstance(localUser());
        assertReturnsSameInstance(oauth2Principal);
        assertReturnsSameInstance(samlPrincipal);
    }

    /**
     * 连续取当前用户返回同一个对象：说明读取过程没有回查账号存储，权限因而是登录时的快照
     */
    private void assertReturnsSameInstance(SecurityUser principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        SecurityUser first = SecurityUserContext.getCurrentUser();
        SecurityUser second = SecurityUserContext.getCurrentUser();

        Assert.assertSame(principal, first);
        Assert.assertSame(first, second);
        Assert.assertEquals("admin", second.getUsername());
    }
}
