package com.kewen.framework.auth.sys.model;

import com.kewen.framework.common.core.model.User;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * @descrpition 权限对象合集
 * @author kewen
 * @since 2022-11-28 13:45
 */
@Data
@Accessors(chain = true)
public class SysAuthorityObject {

    private Collection<User> users;
    private Collection<Dept> depts;
    private Collection<Position> positions;
    private Collection<Role> roles;


}
