package com.cu.framework.auth.rbac.composite;

import com.cu.framework.auth.rbac.model.UserAuthObject;

public interface SysUserComposite{

    UserAuthObject loadByUsername(String username);

    /**
     * 修改密码
     * @param username 用户名
     * @param newPassword 新密码，已经做了加密处理后的密码
     * @return
     */
    boolean updatePassword(String username, String newPassword);

}
