package com.kewen.framework.auth.sys.model;


import com.kewen.framework.auth.core.model.AuthUserInfo;
import com.kewen.framework.auth.sys.utils.AuthorityConvertUtil;
import com.kewen.framework.common.core.model.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @descrpition 用户详情，包含岗位权限等
 * @author kewen
 * @since 2022-11-25 15:17
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SysUserInfo extends User implements AuthUserInfo<SysAuthority> {

    protected UserDept dept;
    protected Collection<Position> positions;
    protected Collection<Role> roles;

    public SysUserInfo setUser(User user){
        this.id=user.getId();
        this.name=user.getName();
        return this;
    }

    public List<Dept> allDepts(){
        return dept.allDepts();
    }

    @Override
    public List<SysAuthority> getAuthorities() {
        return new ArrayList<>(AuthorityConvertUtil.parseCurrentUser(this));
    }
}
