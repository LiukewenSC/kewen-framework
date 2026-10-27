package com.cu.framework.authentication.oauth2;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * OAuth2 协议（非 OIDC）登录端点使用的用户信息服务：取回身份源原生主体后装配成统一认证主体。
 * <p>装配由 {@link SsoSecurityUserConverter} 完成，与 {@link SecurityUserOidcUserService}
 * 在各自协议链路的末端各调用一次，两条链路共用同一个装配器。
 * <p>装配 MUST 留在本类而不是下沉到 {@link RoutingOAuth2UserService}：后者还被
 * {@link SecurityUserOidcUserService} 当作取 userinfo 的内部委托，而 OIDC 的最终主体由
 * {@code OidcUserService} 用 ID Token 另建，内部委托的装配结果只会被丢弃，
 * 于是每次 OIDC 登录多查一遍本地账号库；且当 userinfo 整体被跳过时内部装配根本不会执行。
 * <p>不注册为 Bean：容器里 {@code OAuth2UserService} 类型的实例保持单一，
 * 即 {@link Oauth2Config#routingOauth2UserService()} 那个只路由不装配的实例。
 *
 * @author kewen
 * @since 2026-10-19
 */
public class SecurityUserOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    /** 按客户端分发的 userinfo 取回者，本类对它无配置权 */
    private final RoutingOAuth2UserService routingUserService;

    /** 身份源主体到统一认证主体的装配器 */
    private final SsoSecurityUserConverter converter;

    public SecurityUserOAuth2UserService(RoutingOAuth2UserService routingUserService,
                                         SsoSecurityUserConverter converter) {
        this.routingUserService = routingUserService;
        this.converter = converter;
    }

    /**
     * 委托路由取回身份源原生主体，再装配成统一认证主体
     *
     * @param userRequest 用户信息请求
     * @return 统一认证主体
     * @throws OAuth2AuthenticationException 用户信息请求失败，或本系统没有对应账号
     */
    @Override
    public Oauth2SecurityUser loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        return converter.convert(routingUserService.loadUser(userRequest));
    }
}
