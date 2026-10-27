package com.cu.framework.auth.rbac.security;

import com.cu.framework.authentication.exception.NoLoginException;
import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.model.SecurityUserAuthority;
import com.cu.framework.basic.model.Result;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 当前用户端点的身份投影与路径契约：只返回页面需要的字段，不泄露密码；
 * 未登录时把 NoLoginException 交给上层 advice 转换为 401。
 *
 * @author kewen
 * @since 2026-10-27
 */
public class RbacCurrentUserEndpointTest {

    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void returnsIdentityWithoutPassword() {
        SecurityUser user = new SecurityUser()
                .setId(1L)
                .setName("系统用户")
                .setNickName("kewen")
                .setUsername("kewen")
                .setPhone("15708479468")
                .setEmail("kewen@example.com")
                .setAvatarFileId(99L)
                .setGender("1")
                .setPassword("{bcrypt}secret")
                .setLoginTime(LocalDateTime.of(2026, 10, 27, 12, 0, 0))
                .setUserAuthorities(Arrays.asList(new SecurityUserAuthority("ROLE__1", "超级管理员")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        Result<RbacCurrentUserResp> result = new RbacCurrentUserEndpoint().currentUser();

        assertEquals(Integer.valueOf(200), result.getCode());
        RbacCurrentUserResp data = result.getData();
        assertNotNull(data);
        assertEquals(Long.valueOf(1L), data.getId());
        assertEquals("系统用户", data.getName());
        assertEquals("kewen", data.getNickName());
        assertEquals("kewen", data.getUsername());
        assertEquals("15708479468", data.getPhone());
        assertEquals("kewen@example.com", data.getEmail());
        assertEquals(Long.valueOf(99L), data.getAvatarFileId());
        assertEquals("1", data.getGender());
        assertEquals(1, data.getAuthorities().size());
        assertEquals("ROLE__1", data.getAuthorities().get(0).getAuthority());
        assertEquals(LocalDateTime.of(2026, 10, 27, 12, 0, 0), data.getLoginTime());
        for (Field field : RbacCurrentUserResp.class.getDeclaredFields()) {
            assertNotEquals("当前用户响应不得包含密码字段", "password", field.getName());
        }
    }

    @Test(expected = NoLoginException.class)
    public void throwsNoLoginWhenNotAuthenticated() {
        SecurityContextHolder.clearContext();
        new RbacCurrentUserEndpoint().currentUser();
    }

    @Test
    public void mappingUsesConfigurableCurrentUserPath() throws NoSuchMethodException {
        GetMapping mapping = RbacCurrentUserEndpoint.class
                .getMethod("currentUser")
                .getAnnotation(GetMapping.class);
        assertNotNull(mapping);
        assertArrayEquals(new String[]{"${cu.security.current-user-url:/currentUser}"}, mapping.value());
    }
}
