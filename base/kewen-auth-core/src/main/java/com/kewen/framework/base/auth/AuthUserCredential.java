package com.kewen.framework.base.auth;
/**
 *  凭证信息
 * @author kewen
 * @since 2023-04-10
 */
public interface AuthUserCredential {

    String getPassword();

    /**
     * 账号未过期
     * @return
     */
    boolean isNonExpired();

    /**
     * 账号未锁定
     * @return
     */
    boolean isNonLocked();

    /**
     * 账号是否有效，
     * true 有效，未被禁用
     * false 禁用
     * @return
     */
    boolean isEnabled();

}
