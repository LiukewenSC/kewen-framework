package com.kewen.framework.auth.security.response;

import com.kewen.framework.auth.security.model.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface JsonSuccessResultConverter {

    boolean support(Authentication authentication);

    JsonSuccessResult convert(Authentication authentication);

    SecurityUser convert(UserDetailsService userDetailsService, Authentication authentication);

    default JsonSuccessResult convert( SecurityUser securityUser) {
        JsonSuccessResult result = new JsonSuccessResult();
        result.setName(securityUser.getName());
        result.setNickName(securityUser.getNickName());
        result.setUsername(securityUser.getUsername());
        result.setPhone(securityUser.getPhone());
        result.setEmail(securityUser.getEmail());
        result.setAvatarFileId(securityUser.getAvatarFileId());
        result.setGender(securityUser.getGender());
        result.setAuthObject(securityUser.getAuthObject());
        result.setLoginTime(securityUser.getLoginTime());
        return result;
    }

}
