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
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 身份源主体到统一认证主体的装配：权限一律来自本系统，本系统没有账号就拒绝登录。
 * <p>OAuth2 与 OIDC 共用 {@code assemble(OAuth2User)} 单方法，两条协议各占一个用例，
 * 分别守住"无 ID Token 时字段为 null"和"ID Token 与 UserInfo 原样保留"。
 *
 * @author kewen
 * @since 2026-10-19
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

    private final SecurityUserSsoAssembler assembler =
            new SecurityUserSsoAssembler(new OnlyAdminUserDetailsService());

    /**
     * 身份源声明：本系统用户名取自 preferred_username，另带一个与本地权限无关的声明
     */
    private static Map<String, Object> claims() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("sub", "1001");
        attributes.put("preferred_username", "admin");
        attributes.put("role-from-idp", "anything");
        return attributes;
    }

    /**
     * 纯 OAuth2 分支：以配置指向的声明取本地账号，声明里的权限不参与，且没有 ID Token
     */
    @Test
    public void assemblesOauth2PrincipalWithoutIdToken() {
        OAuth2User nativeUser = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("SCOPE_read")),
                claims(), "preferred_username");

        Oauth2SecurityUser principal = assembler.assemble(nativeUser);

        Assert.assertNull("纯 OAuth2 客户端没有 ID Token", principal.getIdToken());
        Assert.assertNull("纯 OAuth2 客户端没有 UserInfo", principal.getUserInfo());
        Assert.assertEquals("admin", principal.getUsername());
        Assert.assertEquals(Long.valueOf(1L), principal.getId());
        Assert.assertEquals("管理员", principal.getName());
        Assert.assertEquals("身份源声明不得参与权限判定，权限只来自本系统账号",
                1, principal.getAuthorities().size());
        Assert.assertEquals("meeting:read", principal.getAuthorities().iterator().next().getAuthority());
        Assert.assertEquals("1001", principal.getAttributes().get("sub"));
        Assert.assertEquals("anything", principal.getAttributes().get("role-from-idp"));
    }

    /**
     * OIDC 分支：ID Token 与 UserInfo 实例原样留在主体上，因此它仍是 OidcUser，
     * 会话终止等依赖 ID Token 的协议功能可用；attributes 同时保留合并后的声明
     */
    @Test
    public void assemblesOidcPrincipalKeepingIdTokenAndUserInfo() {
        OidcIdToken idToken = new OidcIdToken("id-token-value", Instant.now(),
                Instant.now().plusSeconds(600), claims());
        OidcUserInfo userInfo = new OidcUserInfo(claims());
        List<GrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority("SCOPE_openid"));

        Oauth2SecurityUser principal = assembler.assemble(
                new DefaultOidcUser(authorities, idToken, userInfo, "preferred_username"));

        Assert.assertSame("ID Token 必须原样保留，不能是重建的等价对象", idToken, principal.getIdToken());
        Assert.assertSame("UserInfo 必须原样保留", userInfo, principal.getUserInfo());
        Assert.assertEquals("admin", principal.getUsername());
        Assert.assertEquals("meeting:read", principal.getAuthorities().iterator().next().getAuthority());
        Assert.assertEquals("1001", principal.getAttributes().get("sub"));
    }

    /**
     * 本系统没有对应账号时以可区分的错误码失败，而不是放一个空权限会话进来
     */
    @Test
    public void rejectsWhenLocalAccountAbsent() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("preferred_username", "stranger");
        OAuth2User nativeUser = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("SCOPE_read")),
                attributes, "preferred_username");

        try {
            assembler.assemble(nativeUser);
            Assert.fail("本系统无对应账号时必须失败");
        } catch (OAuth2AuthenticationException e) {
            Assert.assertEquals(SsoErrorCodes.LOCAL_USER_NOT_FOUND, e.getError().getErrorCode());
        }
    }

    /**
     * OIDC 主体在本系统无账号时同样以该错误码失败，防止 instanceof 分支绕过校验
     */
    @Test
    public void rejectsOidcWhenLocalAccountAbsent() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("sub", "2002");
        attributes.put("preferred_username", "stranger");
        OidcIdToken idToken = new OidcIdToken("id-token-value", Instant.now(),
                Instant.now().plusSeconds(600), attributes);

        try {
            assembler.assemble(new DefaultOidcUser(
                    Collections.<GrantedAuthority>emptyList(), idToken, null, "preferred_username"));
            Assert.fail("本系统无对应账号时必须失败");
        } catch (OAuth2AuthenticationException e) {
            Assert.assertEquals(SsoErrorCodes.LOCAL_USER_NOT_FOUND, e.getError().getErrorCode());
        }
    }
}
