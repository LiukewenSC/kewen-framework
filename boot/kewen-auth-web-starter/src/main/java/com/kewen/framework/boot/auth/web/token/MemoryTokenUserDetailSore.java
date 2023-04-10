package com.kewen.framework.boot.auth.web.token;

import com.kewen.framework.boot.auth.AuthUserInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @descrpition 内存存储类，适合单体应用存储，集群模式应该考虑使用Redis
 * @author kewen
 * @since 2022-11-25 16:01
 */
public class MemoryTokenUserDetailSore implements TokenUserDetailStore {

    private final Map<String, AuthUserInfo> store = new ConcurrentHashMap<>();


    @Override
    public AuthUserInfo getAuthUserInfo(String token) {
        return store.get(token);
    }

    @Override
    public void setUserDetail(String token, AuthUserInfo userDetail) {
        store.put(token,userDetail);
    }
}
