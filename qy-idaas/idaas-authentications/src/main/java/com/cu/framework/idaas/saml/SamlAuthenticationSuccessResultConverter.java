package com.cu.framework.idaas.saml;

import com.cu.framework.auth.security.model.SecurityUser;
import com.cu.framework.auth.security.response.AuthenticationSuccessResultConverter;
import com.cu.framework.auth.security.response.JsonSuccessResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

/**
 * 2026/02/27
 *
 * @author kewen
 * @since 4.24.0-SP01
 */
public class SamlAuthenticationSuccessResultConverter implements AuthenticationSuccessResultConverter {
    /**
     * 判断是否支持该认证结果：principal 为 Saml2AuthenticatedPrincipal 时支持
     * @param authentication 认证结果
     * @return principal 是 Saml2AuthenticatedPrincipal 返回 true
     */
    @Override
    public boolean support(Authentication authentication) {
        return authentication.getPrincipal() instanceof Saml2AuthenticatedPrincipal;
    }

    /**
     * 把 SAML 认证主体转换为统一的登录成功 JSON 结果：
     * name 取 principal 名称，nickName 取 relyingPartyRegistrationId
     * @param authentication 认证结果，principal 须为 Saml2AuthenticatedPrincipal
     * @return 登录成功响应结果
     */
    @Override
    public JsonSuccessResult convert(Authentication authentication) {
        Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
        JsonSuccessResult result = new JsonSuccessResult();
        result.setName(principal.getName());
        result.setNickName(principal.getRelyingPartyRegistrationId());
        return result;
    }

    /**
     * 以 SAML 主体的 name 作为用户名，通过 UserDetailsService 加载对应的 SecurityUser
     * @param userDetailsService 用户详情服务
     * @param authentication 认证结果，principal 须为 Saml2AuthenticatedPrincipal
     * @return 加载到的 SecurityUser
     */
    @Override
    public SecurityUser convert(UserDetailsService userDetailsService, Authentication authentication) {
        Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
        String name = principal.getName();
        return (SecurityUser) userDetailsService.loadUserByUsername(name);
    }
}
