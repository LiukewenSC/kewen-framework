package com.cu.framework.authentication.oauth2;

import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.model.SecurityUserAuthority;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuth2/OIDC 统一认证主体的字段来源与接口契约
 *
 * @author kewen
 * @since 2026-10-18
 */
public class Oauth2SecurityUserTest {

    private SecurityUser localUser() {
        List<SecurityUserAuthority> authorities =
                Collections.singletonList(new SecurityUserAuthority("meeting:read", "会议室查看"));
        return new SecurityUser()
                .setId(7L)
                .setUsername("admin")
                .setName("管理员")
                .setNickName("老铁")
                .setEmail("admin@example.com")
                .setGender("1")
                .setEnabled(true)
                .setUserAuthorities(authorities);
    }

    private OidcIdToken idToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "1001");
        return new OidcIdToken("token-value", Instant.now(), Instant.now().plusSeconds(600), claims);
    }

    /**
     * 账号信息逐项复制自本系统加载的用户，身份源声明另存在
     */
    @Test
    public void copiesLocalUserFieldsAndKeepsNativeClaims() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "1001");
        attributes.put("preferred_username", "admin");

        Oauth2SecurityUser principal = Oauth2SecurityUser.of(localUser(), attributes, idToken(), null);

        Assert.assertEquals(Long.valueOf(7L), principal.getId());
        Assert.assertEquals("admin", principal.getUsername());
        Assert.assertEquals("管理员", principal.getName());
        Assert.assertEquals("老铁", principal.getNickName());
        Assert.assertEquals("admin@example.com", principal.getEmail());
        Assert.assertEquals("1", principal.getGender());
        Assert.assertEquals("1001", principal.getAttributes().get("sub"));
    }

    /**
     * 标识只能取 getUsername()：getName() 是姓名，被 AuthenticatedPrincipal 与 OAuth2User 复用
     */
    @Test
    public void nameIsDisplayNameWhileUsernameIsTheIdentity() {
        Oauth2SecurityUser principal = Oauth2SecurityUser.of(localUser(), null, null, null);

        Assert.assertEquals("admin", principal.getUsername());
        Assert.assertEquals("管理员", principal.getName());
        Assert.assertNotEquals(principal.getUsername(), principal.getName());
    }

    /**
     * 权限来自本系统账号，不掺身份源的作用域
     */
    @Test
    public void authoritiesComeFromLocalUser() {
        Oauth2SecurityUser principal = Oauth2SecurityUser.of(localUser(), null, null, null);

        Assert.assertEquals(1, principal.getAuthorities().size());
        Assert.assertEquals("meeting:read", principal.getAuthorities().iterator().next().getAuthority());
    }

    /**
     * 声明集与属性集是同一份内容，ID Token 原样保留，供依赖 OidcUser 的协议功能使用
     */
    @Test
    public void claimsAreAttributesAndIdTokenIsRetained() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "1001");
        OidcIdToken idToken = idToken();

        Oauth2SecurityUser principal = Oauth2SecurityUser.of(localUser(), attributes, idToken, null);

        Assert.assertTrue(principal instanceof OidcUser);
        Assert.assertSame(idToken, principal.getIdToken());
        Assert.assertEquals(principal.getAttributes(), principal.getClaims());
        Assert.assertEquals("1001", principal.getSubject());
    }

    /**
     * 纯 OAuth2 客户端没有 ID Token，属性为空时给出空 Map 而不是 null
     */
    @Test
    public void absentIdTokenAndAttributesAreTolerated() {
        Oauth2SecurityUser principal = Oauth2SecurityUser.of(localUser(), null, null, null);

        Assert.assertNull(principal.getIdToken());
        Assert.assertTrue(principal.getAttributes().isEmpty());
    }
}
