package com.cu.framework.auth.rbac.model;

import com.cu.framework.auth.rbac.composite.model.SimpleAuthObject;
import com.cu.framework.auth.rbac.mp.entity.SysUser;
import com.cu.framework.auth.rbac.mp.entity.SysUserCredential;
import lombok.Data;

/**
 * @author kewen
 * @since 2024-05-11
 */
@Data
public class UserAuthObject{
    /** 用户基本信息，对应 sys_user 表记录 */
    private SysUser sysUser;
    /** 用户凭证，对应 sys_user_credential 表记录，用户无凭证时为 null */
    private SysUserCredential sysUserCredential;
    /** 用户权限集合体，含用户/角色/部门及部门角色组合权限，用户无凭证时为 null */
    private SimpleAuthObject authObject;
}
