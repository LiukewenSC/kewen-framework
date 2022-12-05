package com.kewen.framework.boot.authority.model;

import com.kewen.common.model.Dept;
import com.kewen.common.model.Permission;
import com.kewen.common.model.Position;
import com.kewen.common.model.Role;
import com.kewen.common.model.User;
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
public class AuthorityObject {

    private Collection<User> users;
    private Collection<Dept> depts;
    private Collection<Position> positions;
    private Collection<Role> roles;
    private Collection<Permission> permissions;


}
