package com.cu.framework.authentication.response;

import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.model.SecurityUserAuthority;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 登录成功响应体由统一认证主体投影而来，字段齐全但不带凭证
 *
 * @author kewen
 * @since 2026-10-18
 */
public class LoginSuccessResultTest {

    @Test
    public void projectsUserFieldsWithoutCredentials() {
        SecurityUser securityUser = new SecurityUser()
                .setId(1L)
                .setToken("stale-token")
                .setUsername("admin")
                .setName("管理员")
                .setNickName("老铁")
                .setPhone("13800000000")
                .setEmail("admin@example.com")
                .setAvatarFileId(66L)
                .setPassword("{bcrypt}hashed")
                .setGender("1")
                .setLoginTime(LocalDateTime.now())
                .setUserAuthorities(Collections.singletonList(new SecurityUserAuthority("ROLE_ADMIN", "管理员")));

        LoginSuccessResult result = LoginSuccessResult.of(securityUser);

        Assert.assertEquals("管理员", result.getName());
        Assert.assertEquals("老铁", result.getNickName());
        Assert.assertEquals("admin", result.getUsername());
        Assert.assertEquals("13800000000", result.getPhone());
        Assert.assertEquals("admin@example.com", result.getEmail());
        Assert.assertEquals(Long.valueOf(66L), result.getAvatarFileId());
        Assert.assertEquals("1", result.getGender());
        Assert.assertEquals("ROLE_ADMIN", result.getAuthorities().iterator().next().getAuthority());
        Assert.assertEquals(securityUser.getLoginTime(), result.getLoginTime());
        Assert.assertNull("响应体不得带出密码", result.getPassword());
        Assert.assertNull("会话标识由成功处理器写入", result.getToken());
    }
}
