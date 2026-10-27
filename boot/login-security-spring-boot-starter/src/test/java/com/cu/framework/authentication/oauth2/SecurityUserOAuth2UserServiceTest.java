package com.cu.framework.authentication.oauth2;

import com.cu.framework.authentication.exception.SsoErrorCodes;
import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.model.SecurityUserAuthority;
import com.cu.framework.authentication.service.SecurityUserDetailsService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OAuth2 端点的用户信息服务：委托路由取原生主体、只装配一次，
 * 并守住"单次登录只查一遍本地账号库"这条不变式。
 *
 * @author kewen
 * @since 2026-10-19
 */
public class SecurityUserOAuth2UserServiceTest {

    /**
     * 只认 admin 账号的本地用户来源，同时记录被查库次数
     */
    private static class CountingUserDetailsService implements SecurityUserDetailsService {

        private int lookups;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            lookups++;
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

    /**
     * 不发 HTTP 的路由桩，同时记录被调用次数
     */
    private static class StubRoutingUserService extends RoutingOAuth2UserService {

        private final OAuth2User nativeUser;
        private int invocations;

        StubRoutingUserService(OAuth2User nativeUser) {
            super(propertiesWithoutClients());
            this.nativeUser = nativeUser;
        }

        @Override
        public OAuth2User loadUser(OAuth2UserRequest userRequest) {
            invocations++;
            return nativeUser;
        }
    }

    private static Oauth2OidcProperties propertiesWithoutClients() {
        Oauth2OidcProperties properties = new Oauth2OidcProperties();
        properties.setClients(Collections.<Oauth2OidcProperties.Oauth2OidcRegistrationClientProperties>emptyList());
        return properties;
    }

    private static OAuth2UserRequest userRequest() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("internal")
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://idp.example.com/authorize")
                .tokenUri("https://idp.example.com/token")
                .build();
        OAuth2AccessToken accessToken = OAuth2AccessTokenResponse.withToken("access-token-value")
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .build()
                .getAccessToken();
        return new OAuth2UserRequest(registration, accessToken);
    }

    private static OAuth2User nativeUser(String userNameAttribute) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("sub", "1001");
        attributes.put("preferred_username", userNameAttribute);
        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("SCOPE_read")),
                attributes, "preferred_username");
    }

    /**
     * 单次 loadUser 只查一遍本地账号库、只发一次 userinfo 委托，
     * 且产出的是携带本地权限的统一认证主体
     */
    @Test
    public void assemblesOncePerRequest() {
        CountingUserDetailsService userDetailsService = new CountingUserDetailsService();
        StubRoutingUserService router = new StubRoutingUserService(nativeUser("admin"));
        SecurityUserOAuth2UserService service = new SecurityUserOAuth2UserService(router,
                new SsoSecurityUserConverter(userDetailsService));

        OAuth2User loaded = service.loadUser(userRequest());

        Assert.assertEquals("一次登录只允许查一遍本地账号库", 1, userDetailsService.lookups);
        Assert.assertEquals("一次登录只允许向 userinfo 委托一次", 1, router.invocations);
        Assert.assertTrue(loaded instanceof Oauth2SecurityUser);
        Oauth2SecurityUser principal = (Oauth2SecurityUser) loaded;
        Assert.assertEquals("admin", principal.getUsername());
        Assert.assertEquals(Long.valueOf(1L), principal.getId());
        Assert.assertEquals(1, principal.getAuthorities().size());
        Assert.assertEquals("meeting:read", principal.getAuthorities().iterator().next().getAuthority());
        Assert.assertEquals("1001", principal.getAttributes().get("sub"));
    }

    /**
     * 本系统无对应账号时以可区分的错误码终止，且不会重试查库
     */
    @Test
    public void failsWithoutRetryingWhenLocalAccountAbsent() {
        CountingUserDetailsService userDetailsService = new CountingUserDetailsService();
        StubRoutingUserService router = new StubRoutingUserService(nativeUser("stranger"));
        SecurityUserOAuth2UserService service = new SecurityUserOAuth2UserService(router,
                new SsoSecurityUserConverter(userDetailsService));

        try {
            service.loadUser(userRequest());
            Assert.fail("本系统无对应账号时必须失败");
        } catch (OAuth2AuthenticationException e) {
            Assert.assertEquals(SsoErrorCodes.LOCAL_USER_NOT_FOUND, e.getError().getErrorCode());
        }
        Assert.assertEquals("查不到账号不得重试查库", 1, userDetailsService.lookups);
    }
}
