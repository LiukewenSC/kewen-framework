package com.cu.framework.auth.rbac.composite.impl;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.cu.framework.auth.rbac.composite.AuthMenuStore;
import com.cu.framework.auth.rbac.mp.entity.SysAuthMenu;
import com.cu.framework.auth.rbac.mp.entity.SysMenuApi;
import com.cu.framework.auth.rbac.mp.entity.SysMenuRoute;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 *  内存中可过期的菜单存储
 * @author kewen
 * @since 2024-08-15
 */
public class MemoryExpiredAuthMenuStore implements AuthMenuStore {
    /**
     * 菜单、权限的缓存
     */
    private static final Cache<String, Object> cache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();


    /**
     * 获取菜单API列表，优先读缓存，未命中时通过 callable 加载并写入缓存（写入后 10 分钟过期）
     * @param callable 缓存未命中时的数据加载回调
     * @return 菜单API列表
     */
    @Override
    public List<SysMenuApi> getApiMenus(Callable<List<SysMenuApi>> callable) {
        try {
            return  (List<SysMenuApi>) cache.get("getApiMenus", callable);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取菜单路由列表，优先读缓存，未命中时通过 callable 加载并写入缓存（写入后 10 分钟过期）
     * @param callable 缓存未命中时的数据加载回调
     * @return 菜单路由列表
     */
    @Override
    public List<SysMenuRoute> getRouteMenus(Callable<List<SysMenuRoute>> callable) {
        try {
            return  (List<SysMenuRoute>) cache.get("getRouteMenus", callable);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取菜单权限列表，优先读缓存，未命中时通过 callable 加载并写入缓存（写入后 10 分钟过期）
     * @param callable 缓存未命中时的数据加载回调
     * @return 菜单权限列表
     */
    @Override
    public List<SysAuthMenu> getAuthMenus(Callable<List<SysAuthMenu>> callable) {
        try {
            return  (List<SysAuthMenu>) cache.get("getAuthMenus", callable);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
