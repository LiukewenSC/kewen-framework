package com.cu.framework.auth.rbac.composite;

import com.cu.framework.auth.rbac.model.UserAuthObject;

/**
 * 用户权限组合服务接口，负责按用户名装载用户权限对象以及修改用户密码
 * @author kewen
 * @since 2026-02-12
 */
public interface SysUserComposite{

    /**
     * 根据用户名加载用户信息、用户凭证以及用户权限集合体
     * @param username 用户名
     * @return 用户权限对象，用户不存在时返回 null
     */
    UserAuthObject loadByUsername(String username);

    /**
     * 修改密码
     * @param username 用户名
     * @param newPassword 新密码，已经做了加密处理后的密码
     * @return
     */
    boolean updatePassword(String username, String newPassword);

}
