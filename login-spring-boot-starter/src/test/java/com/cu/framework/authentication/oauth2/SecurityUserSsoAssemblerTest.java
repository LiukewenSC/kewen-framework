package com.cu.framework.authentication.oauth2;

import com.cu.framework.authentication.exception.SsoErrorCodes;
import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.model.SecurityUserAuthority;
import com.cu.framework.authentication.service.SecurityUserDetailsService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 身份源主体到统一认证主体的装配：权限一律来自本系统，本系统没有账号就拒绝登录
 *
 * @author kewen
 * @since 2026-10-18
 */
public class SecurityUserSsoAssemblerTest {

    /**
     * 只提供 admin 账号的用户来源，行为与业务实现一致：查不到就抛 UsernameNotFoundException
     */
    private static class OnlyAdminUserDetailsService implements SecurityUserDetailsService {

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            if (!"admin".equals(username)) {
                throw new UsernameNotFoundException("未找到用户");
            }
            return new SecurityUser()
                    .setId(1L)
                    .setUsername("admin")
                    .setName("管理员")
                    .setEnabled(true)
                    .setUserAuthorities(Collections.singletonList(
                            new SecurityUserAuthority("meeting:read", "会议室查看")));
        }

        @Override
        public UserDetails updatePassword(UserDetails user, String encryptedNewPassword) {
            return user;
        }
    }

    private final SecurityUserSsoAssembler assembler = new SecurityUserSsoAssembler(new OnlyAdminUserDetailsService());

    private Map<String, Object> claims(String userNameAttribute) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("sub", "1001");
        attributes.put(userNameAttribute, "admin");
        attributes.put("role-from-idp", "anything");
        return attributes;
    }

    /**
     * OAuth2 分支：以配置指向的声明取本地账号，声明里的权限不参与
     */
    @Test
    public void assemblesOauth2PrincipalFromLocalAccount() {
        OAuth2User nativeUser = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("SCOPE_read")), claims("preferred_username"), "preferred_username");

        Oauth2SecurityUser principal = assembler.assemble(nativeUser);

        Assert.assertEquals("admin", principal.getUsername());
        Assert.assertEquals(Long.valueOf(1L), principal.getId());
        Assert.assertEquals("SCOPE_read", nativeUser.getAuthorities().iterator().next().getAuthority());
        Assert.assertEquals(1, principal.getAuthorities().size());
        Assert.assertEquals("meeting:read", principal.getAuthorities().iterator().next().getAuthority());
        Assert.assertEquals("1001", principal.getAttributes().get("sub"));
    }

    /**
     * OIDC 分支：ID Token 原样留在主体上，因此它仍然是 OidcUser，依赖 ID Token 的协议功能可用
     */
    @Test
    public void assemblesOidcPrincipalKeepingIdToken() {
        OidcIdToken idToken = new OidcIdToken("id-token-value", Instant.now(),
                Instant.now().plusSeconds(600), claims("preferred_username"));
        DefaultOidcUser nativeUser = new DefaultOidcUser(
                Collections.<GrantedAuthority>singletonList(new SimpleGrantedAuthority("SCOPE openid")),
                idToken, null, "preferred_username");

        Oauth2SecurityUser principal = assembler.assemble(nativeUser);

        Assert.assertSame(idToken, principal.getIdToken());
        Assert.assertEquals("admin", principal.getUsername());
        Assert.assertEquals("meeting:read", principal.getAuthorities().iterator().next().getAuthority());
    }

    /**
     * 本系统没有对应账号时以可区分的错误码失败，而不是放一个空权限会话进来
     */
    @Test
    public void rejectsWhenLocalAccountAbsent() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("preferred_username", "stranger");
        OAuth2User nativeUser = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("SCOPE_read")), attributes, "preferred_username");

        try {
            assembler.assemble(nativeUser);
            Assert.fail("本系统无对应账号时必须失败");
        } catch (OAuth2AuthenticationException e) {
            Assert.assertEquals(SsoErrorCodes.LOCAL_USER_NOT_FOUND, e.getError().getErrorCode());
        }
    }
}
