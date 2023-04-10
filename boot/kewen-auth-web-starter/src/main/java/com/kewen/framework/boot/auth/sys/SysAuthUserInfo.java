package com.kewen.framework.boot.auth.sys;

import com.kewen.framework.base.authority.model.SysAuthority;
import com.kewen.framework.base.authority.model.SysUserDetail;
import com.kewen.framework.boot.auth.AuthEntity;
import com.kewen.framework.boot.auth.AuthUserInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 *  默认的权限对象结构
 * @author kewen
 * @since 2023-04-10
 */
public class SysAuthUserInfo implements AuthUserInfo {


    SysUserDetail wrapper;


    public SysAuthUserInfo(SysUserDetail wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public List<AuthEntity> authEntities() {
        List<SysAuthority> authorities = wrapper.authorities();
        return authorities.stream().map(SysAuthEntityAdaptor::new).collect(Collectors.toList());
    }

    @Override
    public Long getUserId() {
        return wrapper.getUserId();
    }

    @Override
    public String getUserName() {
        return wrapper.getUserName();
    }
}
