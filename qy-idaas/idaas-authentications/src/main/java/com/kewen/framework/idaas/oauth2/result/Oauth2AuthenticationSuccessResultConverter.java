package com.kewen.framework.idaas.oauth2.result;

import com.kewen.framework.auth.security.model.SecurityUser;
import com.kewen.framework.auth.security.response.JsonSuccessResult;
import com.kewen.framework.auth.security.response.AuthenticationSuccessResultConverter;
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
    @Override
    public boolean support(Authentication authentication) {
        return authentication.getPrincipal() instanceof OAuth2User;
    }

    @Override
    public JsonSuccessResult convert(Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        JsonSuccessResult result = new JsonSuccessResult();
        result.setName(oAuth2User.getName());
        result.setNickName(oAuth2User.getAttribute("nickName"));
        result.setUsername(oAuth2User.getAttribute("username"));
        result.setPhone(oAuth2User.getAttribute("phone"));
        result.setEmail(oAuth2User.getAttribute("email"));
        result.setAvatarFileId(oAuth2User.getAttribute("avatarFileId"));
        result.setGender(oAuth2User.getAttribute("gender"));
        result.setAuthObject(oAuth2User.getAttribute("authObject"));
        return result;
    }

    @Override
    public SecurityUser convert(UserDetailsService userDetailsService, Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String name = oAuth2User.getName();
        return (SecurityUser) userDetailsService.loadUserByUsername(name);
    }
}
