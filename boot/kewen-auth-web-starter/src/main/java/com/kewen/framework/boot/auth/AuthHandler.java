package com.kewen.framework.boot.auth;

import java.util.Collection;

/**
 *
 * <U> 用户的泛型
 * <C> 用户凭证的泛型
 *
 * @author kewen
 * @since 2023-04-10
 */
public interface AuthHandler {

    /**
     * 是否有菜单访问权限
     * @param authorities
     * @param url
     * @return
     */
    boolean hasAuth(Collection<String> authorities, String url) ;

    /**
     * 根据用户名获取用户id
     * @param username
     * @return
     */
    Long getUserId(String username);

    /**
     * 获取用户信息
     * @param userId 用户id
     * @return
     */
    AuthUserInfo getAuthUser(Long userId);

    /**
     * 获取用户凭证信息
     * @param userId
     * @return
     */
    AuthUserCredential getCredential(Long userId);


}
