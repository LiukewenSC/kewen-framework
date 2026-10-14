package com.cu.framework.authentication.oauth2.result;

import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.response.LoginSuccessResult;
import com.cu.framework.authentication.response.AuthenticationSuccessResultConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * 2026/02/22
 *
 * @author kewen
 * @since 1.0
 */
public class Oauth2AuthenticationSuccessResultConverter implements AuthenticationSuccessResultConverter {
    /**
     * 判断是否支持该认证结果：principal 为 OAuth2User 时支持
     * @param authentication 认证结果
     * @return principal 是 OAuth2User 返回 true
     */
    @Override
    public boolean support(Authentication authentication) {
        return authentication.getPrincipal() instanceof OAuth2User;
    }

    /**
     * 把 OAuth2User 的属性（name、nickName、username、phone、email、avatarFileId、gender、authObject）
     * 提取组装为统一的登录成功 JSON 结果
     * @param authentication 认证结果，principal 须为 OAuth2User
     * @return 登录成功响应结果
     */
    @Override
    public LoginSuccessResult convert(Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        LoginSuccessResult result = new LoginSuccessResult();
        result.setName(oAuth2User.getName());
        result.setNickName(oAuth2User.getAttribute("nickName"));
        result.setUsername(oAuth2User.getAttribute("username"));
        result.setPhone(oAuth2User.getAttribute("phone"));
        result.setEmail(oAuth2User.getAttribute("email"));
        result.setAvatarFileId(oAuth2User.getAttribute("avatarFileId"));
        result.setGender(oAuth2User.getAttribute("gender"));
        result.setAuthorities(oAuth2User.getAttribute("authObject"));
        return result;
    }

    /**
     * 以 OAuth2User 的 name 作为用户名，通过 UserDetailsService 加载对应的 SecurityUser
     * @param userDetailsService 用户详情服务
     * @param authentication 认证结果，principal 须为 OAuth2User
     * @return 加载到的 SecurityUser
     */
    @Override
    public SecurityUser convert(UserDetailsService userDetailsService, Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String name = oAuth2User.getName();
        return (SecurityUser) userDetailsService.loadUserByUsername(name);
    }
}
