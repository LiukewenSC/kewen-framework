package com.kewen.framework.auth.sys.model;

import com.kewen.framework.auth.core.model.AuthUserCredential;
import com.kewen.framework.auth.sys.mp.entity.SysUserCredential;

import java.time.LocalDateTime;

/**
 *  系统权限的包装类型
 * @author kewen
 * @since 2023-04-10
 */
public class SysAuthUserCredential implements AuthUserCredential {

    SysUserCredential instance;

    public SysAuthUserCredential(SysUserCredential instance) {
        this.instance=instance;
    }

    @Override
    public String getPassword() {
        return instance.getPassword();
    }

    @Override
    public boolean isNonExpired() {
        LocalDateTime expiredTime = instance.getPasswordExpiredTime();
        // expiredTime == null 表示系统未设定过期时间，永久有效
        return expiredTime == null || LocalDateTime.now().isAfter(expiredTime);
    }

    @Override
    public boolean isNonLocked() {
        LocalDateTime lockedDeadline = instance.getAccountLockedDeadline();

        return lockedDeadline == null || LocalDateTime.now().isAfter(lockedDeadline);
    }

    @Override
    public boolean isEnabled() {
        return instance.getEnabled();
    }
}
