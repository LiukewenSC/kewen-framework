package com.kewen.framework.boot.auth.token;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @descrpition 内存存储类，适合单体应用存储，集群模式应该考虑使用Redis
 * @author kewen
 * @since 2022-11-25 16:01
 */
public class MemoryTokenStore<T> implements TokenStore<T> {

    private LoadingCache<String, T> store;

    public MemoryTokenStore(long expireTime) {
        loadingCache(expireTime);
    }
    private void loadingCache(long expireTime){
        this.store = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofSeconds(expireTime)).build(new CacheLoader<String, T>() {
            @Override
            public T load(String o) throws Exception {
                return null;
            }
        });
    }
    public void setExpireTime(long expireTime){
        loadingCache(expireTime);
    }

    @Override
    public T get(String token) {
        try {
            return store.getIfPresent(token);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(String token, T value) {
        store.put(token,value);
    }
}
