package com.cu.framework.authentication.oauth2;

import com.cu.framework.authentication.exception.SsoErrorCodes;
import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.service.SecurityUserDetailsService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
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
public class SsoSecurityUserConverter implements Converter<OAuth2User, Oauth2SecurityUser> {


    /**
     * 本系统账号来源，单点登录用它把身份源标识换成本系统的用户与权限
     */
    private final SecurityUserDetailsService userDetailsService;

    public SsoSecurityUserConverter(SecurityUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * 把身份源返回的原生主体装配为统一认证主体，OAuth2 与 OIDC 两条协议链路共用本方法。
     * <p>OIDC 的 ID Token 与 UserInfo 只在主体是 {@link OidcUser} 时取出：纯 OAuth2 客户端没有
     * ID Token，此时这两个字段为 null，依赖 ID Token 的协议功能本来就只对 OIDC 客户端有意义。
     * <p>调用方 MUST 只在各协议链路的末端调用一次，不要把它放进取 userinfo 的那一层：
     * OIDC 的内部委托只消费返回主体的 {@code getAttributes()}，在其内部装配会使本地账号库
     * 每次登录被查两遍，且 {@code LOCAL_USER_NOT_FOUND} 会抢在 ID Token 与 UserInfo 的
     * {@code sub} 一致性校验之前抛出。
     *
     * @param source 用户信息端点或 ID Token 产出的原生主体，其 name 为本系统的用户名
     * @return 统一认证主体
     * @throws OAuth2AuthenticationException 本系统没有对应账号
     */
    @Override
    public Oauth2SecurityUser convert(OAuth2User source) {
        SecurityUser localUser = loadLocalUser(source.getName());
        OidcIdToken idToken = null;
        OidcUserInfo userInfo = null;
        if (source instanceof OidcUser) {
            OidcUser oidcSource = (OidcUser) source;
            idToken = oidcSource.getIdToken();
            userInfo = oidcSource.getUserInfo();
        }
        return Oauth2SecurityUser.of(localUser, source.getAttributes(), idToken, userInfo);
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
