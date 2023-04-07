package com.kewen.framework.base.authority.model;

import com.kewen.framework.base.common.model.User;
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


}
