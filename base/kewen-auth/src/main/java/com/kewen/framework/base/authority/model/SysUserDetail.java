package com.kewen.framework.base.authority.model;


import com.kewen.framework.base.authority.utils.AuthorityConvertUtil;
import com.kewen.framework.base.common.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @descrpition 用户详情，包含岗位权限等
 * @author kewen
 * @since 2022-11-25 15:17
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SysUserDetail extends User {

    protected UserDept dept;
    protected Collection<Position> positions;
    protected Collection<Role> roles;

    public SysUserDetail setUser(User user){
        this.id=user.getId();
        this.name=user.getName();
        return this;
    }
    public List<SysAuthority> sysAuthorities(){
        return new ArrayList<>(AuthorityConvertUtil.parseCurrentUser(this));
    }

    public List<Dept> allDepts(){
        return dept.allDepts();
    }
}
