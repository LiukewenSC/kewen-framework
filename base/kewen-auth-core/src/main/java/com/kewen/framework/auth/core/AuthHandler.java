package com.kewen.framework.auth.core;

import com.kewen.framework.auth.core.model.AuthEntity;
import com.kewen.framework.auth.core.model.AuthUserCredential;
import com.kewen.framework.auth.core.model.AuthUserInfo;

import java.util.Collection;
import java.util.List;

/**
 *
 * <E> 权限对象泛型
 * @author kewen
 * @since 2023-04-10
 */
public interface AuthHandler<E extends AuthEntity> {



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
    boolean hasMenuAuth(Collection<String> authorities, String url) ;
    /**
     * 查询用户手有某个业务操作的权限
     * @param auths 用户权限
     * @param module 模块
     * @param operate 操作
     * @param businessId 业务id
     * @return 是否有权限
     */
    boolean hasBusinessAuth(Collection<String> auths, String module, String operate, Long businessId);
    /**
     * 编辑菜单权限
     * @param menuId 菜单id
     * @param authority 权限结构
     */
    void editMenuAuthorities(Long menuId, List<E> authority);

    /**
     * 编辑业务数据权限
     * @param businessId 业务id
     * @param module 模块
     * @param operate 操作
     * @param authority 权限结构
     */
    void editBusinessAuthority(Long businessId,String module,String operate, List<E> authority);


}
