package com.kewen.common.model;


import java.util.Collection;
import java.util.Collections;

/**
 * @descrpition 用户详情，包含岗位权限等
 * @author kewen
 * @since 2022-11-25 15:17
 */
public class UserDetail {
    protected User user;
    protected UserDept dept;
    protected Collection<Role> roles;
    protected Collection<Permission> permissions;

    public static UserDetailBuilder builder(){
        return new UserDetailBuilder();
    }

    public Collection<Dept> getDepts(){
        if (dept==null) {
            return Collections.emptyList();
        }
        return dept.getDepts();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserDept getDept() {
        return dept;
    }

    public void setDept(UserDept dept) {
        this.dept = dept;
    }

    public Collection<Role> getRoles() {
        return roles;
    }

    public void setRoles(Collection<Role> roles) {
        this.roles = roles;
    }

    public Collection<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<Permission> permissions) {
        this.permissions = permissions;
    }

    public static final class UserDetailBuilder {
        private UserDetail userDetail;

        private UserDetailBuilder() {
            userDetail = new UserDetail();
        }

        public UserDetailBuilder user(User user) {
            userDetail.setUser(user);
            return this;
        }

        public UserDetailBuilder dept(UserDept dept) {
            userDetail.setDept(dept);
            return this;
        }

        public UserDetailBuilder roles(Collection<Role> roles) {
            userDetail.setRoles(roles);
            return this;
        }

        public UserDetailBuilder permissions(Collection<Permission> permissions) {
            userDetail.setPermissions(permissions);
            return this;
        }

        public UserDetail build() {
            return userDetail;
        }
    }
}
