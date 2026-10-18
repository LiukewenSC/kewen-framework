package com.cu.framework.authentication.oauth2;

import com.cu.framework.authentication.exception.SsoErrorCodes;
import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.service.SecurityUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * 把身份源返回的主体装配为统一认证主体
 * <p>
 * 身份源只负责证明"这个人是谁"：以主体的标识取本系统账号，展示信息与权限一律来自本系统，
 * 身份源返回的声明与 ID Token 仅作为协议级功能的数据留在认证主体上，不参与授权判定。
 *
 * @author kewen
 * @since 2026-10-18
 */
public class SecurityUserSsoAssembler {

    /**
     * 本系统账号来源，单点登录用它把身份源标识换成本系统的用户与权限
     */
    private final SecurityUserDetailsService userDetailsService;

    public SecurityUserSsoAssembler(SecurityUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * 装配 OAuth2 协议（无 ID Token）的认证主体
     *
     * @param source 用户信息端点返回的主体，其 name 为本系统的用户名
     * @return 统一认证主体
     * @throws OAuth2AuthenticationException 本系统没有对应账号
     */
    public Oauth2SecurityUser assemble(OAuth2User source) {
        SecurityUser localUser = loadLocalUser(source.getName());
        return Oauth2SecurityUser.of(localUser, source.getAttributes(), null, null);
    }

    /**
     * 装配 OIDC 协议的认证主体，保留 ID Token 与 UserInfo 供会话终止等协议功能使用
     *
     * @param source 用户信息服务返回的主体，其 name 为本系统的用户名
     * @return 统一认证主体
     * @throws OAuth2AuthenticationException 本系统没有对应账号
     */
    public Oauth2SecurityUser assemble(OidcUser source) {
        SecurityUser localUser = loadLocalUser(source.getName());
        return Oauth2SecurityUser.of(localUser, source.getAttributes(), source.getIdToken(), source.getUserInfo());
    }

    /**
     * 以身份源主体的标识作为本系统用户名取账号，取不到时以可区分的错误码终止本次认证，
     * 而不是放一个没有权限的会话进来
     */
    private SecurityUser loadLocalUser(String username) {
        try {
            return (SecurityUser) userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            OAuth2Error error = new OAuth2Error(SsoErrorCodes.LOCAL_USER_NOT_FOUND,
                    "身份源认证的账号 [" + username + "] 在本系统中不存在", null);
            throw new OAuth2AuthenticationException(error, error.toString());
        }
    }
}
