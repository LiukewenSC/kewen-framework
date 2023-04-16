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
public class SysAuthUserInfo extends SysUserDetail implements AuthUserInfo {


    public SysAuthUserInfo(SysUserDetail wrapper) {
        this.id=wrapper.getId();
        this.name=wrapper.getName();
        this.dept=wrapper.getDept();
        this.roles=wrapper.getRoles();
        this.positions=wrapper.getPositions();
    }

    @Override
    public List<AuthEntity> getAuthorities() {
        List<SysAuthority> authorities = sysAuthorities();
        return authorities.stream().map(SysAuthEntityAdaptor::new).collect(Collectors.toList());
    }
}
