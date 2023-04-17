package com.kewen.framework.boot.auth.web.token;


import com.kewen.framework.auth.core.model.AuthUserInfo;
import com.kewen.framework.boot.auth.web.WebAuthUserInfoContextContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;

/**
 * @descrpition 基于token的用户上下文 ，需要先有通过token获取
 *      此拦截器只保证从 TokenUserDetailStore 中获取，存储的时候是在登录时完成，登录时需要将数据写入store中
 * @author kewen
 * @since 2022-11-25 15:42
 */
public class TokenCurrentUserInfoContextContainer implements WebAuthUserInfoContextContainer {

    @Autowired
    HttpServletRequest request;
    /**
     * userDetail存储器
     */
    @Autowired
    private TokenUserDetailStore tokenUserDetailStore;

    @Autowired
    private TokenKeyGenerator tokenKeyGenerator;

    @Value("${auth.webHeader.tokenKey:token}")
    private String tokenKeyInHeader;


    @Override
    public String saveAuthUserInfo(AuthUserInfo userDetail) {
        String token = tokenKeyGenerator.generateKey();
        tokenUserDetailStore.setUserDetail(token,userDetail);
        return token;
    }

    @Override
    public AuthUserInfo getAuthUserInfo() {
        String token = request.getHeader(tokenKeyInHeader);
        return tokenUserDetailStore.getAuthUserInfo(token);
    }
}
