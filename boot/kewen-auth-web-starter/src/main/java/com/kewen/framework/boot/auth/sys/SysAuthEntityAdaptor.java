package com.kewen.framework.boot.auth.sys;

import com.kewen.framework.base.authority.model.SysAuthority;
import com.kewen.framework.boot.auth.AuthEntity;

/**
 * 
 * @author kewen
 * @since 2023-04-10
 */
public class SysAuthEntityAdaptor implements AuthEntity {
    SysAuthority sysAuthority;

    public SysAuthEntityAdaptor(SysAuthority sysAuthority) {
        this.sysAuthority = sysAuthority;
    }

    @Override
    public String getAuth() {
        return sysAuthority.getAuthority();
    }
}
