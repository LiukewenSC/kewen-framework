package com.cu.framework.auth.rbac.composite.model;


import com.cu.framework.auth.core.entity.AbstractAuthObject;
import com.cu.framework.auth.core.entity.BaseAuth;
import com.cu.framework.auth.core.entity.IFlagAuthEntity;
import com.cu.framework.auth.rbac.composite.SysUserComposite;
import com.cu.framework.auth.rbac.mp.entity.SysDept;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户权限体集合体
 * @author kewen
 * @since 2026-10-22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserAuthInfo extends AbstractAuthObject {

    private static final long serialVersionUID = 842909202036381753L;
    /**
     * 用户集合
     */
    protected List<User> users = new ArrayList<>();
    /**
     * 角色集合
     */
    protected List<Role> roles = new ArrayList<>();
    /**
     * 部门集合
     */
    protected List<DeptWithParents> depts = new ArrayList<>();

    /**
     * 部门角色集合
     */
    protected List<DeptRole> deptRoles = new ArrayList<>();

    public void addUsers(User... users){
        this.users.addAll(Arrays.asList(users));
    }
    public void addDepts(DeptWithParents... depts){
        this.depts.addAll(Arrays.asList(depts));
    }
    public void addRoles(Role... roles){
        this.roles.addAll(Arrays.asList(roles));
    }
    public void addDeptRoles(DeptRole... roles){
        this.deptRoles.addAll(Arrays.asList(roles));
    }


    /**
     * 将 users、roles、depts、deptRoles 中所有权限实体的权限字符串汇总为 BaseAuth 集合（Set 去重），
     * 并回调 addAnotherBashAuth 供子类追加其他权限
     * @return 汇总后的基础权限集合
     */
    @Override
    public Collection<BaseAuth> listBaseAuth() {
        Collection<BaseAuth> baseAuths = new HashSet<>();
        if (users != null) {
            for (User user : users) {
                baseAuths.add(user.getAuth());
            }
        }
        if (roles != null) {
            for (Role role : roles) {
                baseAuths.add(role.getAuth());
            }
        }
        if (depts != null) {
            for (Dept dept : depts) {
                baseAuths.add(dept.getAuth());
            }
        }
        if (deptRoles != null) {
            for (DeptRole deptRole : deptRoles) {
                baseAuths.add(deptRole.getAuth());
            }
        }
        addAnotherBashAuth(baseAuths);
        return baseAuths;
    }

    /**
     * 添加其他权限对象，若子类继承可以扩展这里，也可以覆写listBaseAuth()
     * @param baseAuths
     */
    protected void addAnotherBashAuth(Collection<BaseAuth>  baseAuths){

    }

    @Override
    protected void setBaseAuth(List<IFlagAuthEntity> abstractAuthEntities) {
        for (IFlagAuthEntity abstractAuthEntity : abstractAuthEntities) {
            if (abstractAuthEntity instanceof User) {
                users.add(((User) abstractAuthEntity));
            } else if (abstractAuthEntity instanceof Role) {
                roles.add(((Role) abstractAuthEntity));
            } else if (abstractAuthEntity instanceof DeptWithParents) {
                depts.add(((DeptWithParents) abstractAuthEntity));
            } else if (abstractAuthEntity instanceof Dept) {
                DeptWithParents dwp = new DeptWithParents(((Dept) abstractAuthEntity).getId(), ((Dept) abstractAuthEntity).getName());
                depts.add(dwp);
            } else if (abstractAuthEntity instanceof DeptRole){
                deptRoles.add(((DeptRole) abstractAuthEntity));
            }else {
                setAnotherBaseAuth(abstractAuthEntity);
            }
        }
    }

    /**
     * 设置其他权限对象，若子类需要的话可以扩展这里，也可以覆写 setBaseAuth()
     * @param abstractAuthEntity
     */
    protected void setAnotherBaseAuth(IFlagAuthEntity abstractAuthEntity){

    }


    /**
     * 填充
     * @param sysUserComposite
     */
    public void fillUserAuth(SysUserComposite sysUserComposite){
        List<Role> roles1 = getRoles();
        List<DeptWithParents> depts1 = getDepts();
        for (DeptWithParents dept : depts1) {
            for (Role role : roles1) {
                deptRoles.add(new DeptRole(dept,role));
            }
            List<ParentDept> parentChain = sysUserComposite.getDeptListByDeptId(dept.getId());
            if (parentChain != null && !parentChain.isEmpty()){
                dept.getParents().addAll(parentChain);
            }
        }
    }
}
