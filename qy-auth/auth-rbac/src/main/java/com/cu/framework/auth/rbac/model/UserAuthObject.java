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
    private SysUser sysUser;
    private SysUserCredential sysUserCredential;
    private SimpleAuthObject authObject;
}
