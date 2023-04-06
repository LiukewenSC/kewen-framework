package com.kewen.framework.boot.authority.currentuser.token;

import com.kewen.framework.boot.authority.model.LoginResp;
import com.kewen.framework.boot.authority.currentuser.AbstractCurrentUserContextManager;
import com.kewen.framework.base.common.model.UserDetail;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;

/**
 * @descrpition 基于token的用户上下文 ，需要先有通过token获取
 *      此拦截器只保证从 TokenUserDetailStore 中获取，存储的时候是在登录时完成，登录时需要将数据写入store中
 * @author kewen
 * @since 2022-11-25 15:42
 */
public class TokenCurrentUserContextManager extends AbstractCurrentUserContextManager {

    /**
     * userDetail存储器
     */
    private final TokenUserDetailStore tokenUserDetailStore;
    private final TokenKeyGenerator tokenKeyGenerator;

    @Value("${auth.webHeader.tokenKey:token}")
    private String tokenKeyInHeader;


    public TokenCurrentUserContextManager(TokenUserDetailStore tokenUserDetailStore, TokenKeyGenerator tokenKeyGenerator) {
        this.tokenUserDetailStore = tokenUserDetailStore;
        this.tokenKeyGenerator = tokenKeyGenerator;
    }

    @Override
    public UserDetail fetchUserDetail(HttpServletRequest request) {
        String token = request.getHeader(tokenKeyInHeader);
        return StringUtils.isBlank(token)?null: getUserDetail(token);
    }

    @Override
    public UserDetail getUserDetail(String token) {
        return tokenUserDetailStore.getUserDetail(token);
    }

    @Override
    public LoginResp saveUserDetail(UserDetail userDetail) {
        String token = tokenKeyGenerator.generateKey();
        tokenUserDetailStore.setUserDetail(token,userDetail);
        LoginResp copy = LoginResp.copy(userDetail);
        copy.setToken(token);
        return copy;
    }
}
