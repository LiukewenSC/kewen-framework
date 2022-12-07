package com.kewen.framework.boot.authority.service;



import com.kewen.framework.base.authority.model.MenuResp;

import java.util.Collection;
import java.util.List;

/**
 * @descrpition  菜单权限整合服务类
 * @author kewen
 * @since 2022-12-01 10:24
 */
public interface SysMenuAuthUnify {
    /**
     * 校验用户的菜单权限
     * @param authorities 用户权限字符串
     * @param url 菜单url链接
     * @return 是否有权限
     */
    boolean hasAuth(Collection<String> authorities, String url) ;

    /**
     * 获取树形结构菜单
     * @return 树形结构的菜单返回对象
     */
    List<MenuResp> getMenuTree();

    /**
     * 获取当前用户有权限的菜单
     * @param authorities
     * @return
     */
    List<MenuResp> getCurrentUserMenuTree(Collection<String> authorities);

}
