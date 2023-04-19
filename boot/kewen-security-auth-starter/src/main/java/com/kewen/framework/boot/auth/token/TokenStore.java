package com.kewen.framework.boot.auth.token;


/**
 * @descrpition 
 * @author kewen
 * @since 2022-11-29 11:21
 */
public interface TokenStore<T> {

    T get(String token);

    void set(String token, T userDetail);
}
