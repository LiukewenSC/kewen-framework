package com.kewen.framework.base.auth;

import java.util.Collection;
import java.util.List;

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

    /**
     * 是否有菜单访问权限
     * @param authorities
     * @param url
     * @return
     */
    boolean hasAuth(Collection<String> authorities, String url) ;
    /**
     * 查询用户手有某个业务操作的权限
     * @param auths 用户权限
     * @param module 模块
     * @param operate 操作
     * @param businessId 业务id
     * @return 是否有权限
     */
    boolean hasAuth(Collection<String> auths, String module, String operate, Long businessId);
    /**
     * 编辑菜单权限
     * @param menuId 菜单id
     * @param authority 权限结构
     */
    void editMenuAuthorities(Long menuId, List<AuthEntity> authority);

    /**
     * 编辑业务数据权限
     * @param businessId 业务id
     * @param module 模块
     * @param operate 操作
     * @param authority 权限结构
     */
    void editBusinessAuthority(Long businessId,String module,String operate, List<AuthEntity> authority);


}
