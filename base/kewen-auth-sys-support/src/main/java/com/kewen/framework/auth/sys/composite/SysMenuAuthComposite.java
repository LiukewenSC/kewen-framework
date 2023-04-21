package com.kewen.framework.auth.sys.composite;



import com.kewen.framework.auth.sys.model.req.MenuSaveReq;
import com.kewen.framework.auth.sys.mp.entity.SysMenu;
import com.kewen.framework.auth.sys.model.SysAuthority;
import com.kewen.framework.auth.sys.model.resp.MenuResp;
import com.kewen.framework.common.core.model.IdReq;

import java.util.Collection;
import java.util.List;

/**
 * @descrpition  菜单权限整合服务类
 * @author kewen
 * @since 2022-12-01 10:24
 */
public interface SysMenuAuthComposite {
    /**
     * 校验用户的菜单权限
     * @param authorities 用户权限字符串
     * @param url 菜单url链接
     * @return 是否有权限
     */
    boolean hasMenuAuth(Collection<String> authorities, String url) ;

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


    /**
     * 编辑菜单权限
     * @param menuId 菜单id
     * @param authority 权限结构
     */
    void editMenuAuthorities(Long menuId, List<SysAuthority> authority);

    /**
     * 编辑业务数据权限
     * @param businessId 业务id
     * @param module 模块
     * @param operate 操作
     * @param authority 权限结构
     */
    void editBusinessAuthority(Long businessId,String module,String operate, List<SysAuthority> authority);


    /**
     * 查询用户手有某个业务操作的权限
     * @param auths 用户权限
     * @param module 模块
     * @param operate 操作
     * @param businessId 业务id
     * @return 是否有权限
     */
    boolean hasBusinessAuth(Collection<String> auths, String module, String operate, Long businessId);


    void addMenu(MenuSaveReq req);
    void updateMenu(MenuSaveReq req);
    void deleteMenu(IdReq req);

}
