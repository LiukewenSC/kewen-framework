package com.cu.framework.auth.rbac.composite.model;


import com.cu.framework.auth.core.entity.AbstractAuthObject;
import com.cu.framework.auth.core.entity.FlatteningAuthority;
import com.cu.framework.auth.core.entity.IFlagAuthEntity;
import com.cu.framework.auth.rbac.composite.SysUserComposite;
import com.cu.framework.basic.exception.BizException;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

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
    protected User user;
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
     * @return 汇总后的基础权限集合，只在用户需要匹配权限字符串的时候使用，不会有逆向操作，因此可以放心构建；
     * 当需要扩展用户权限时，如需要支持跨层级的部门+角色、如支持角色树等，可以扩展此处；
     */
    @Override
    public Collection<FlatteningAuthority> listBaseAuth() {
        Collection<FlatteningAuthority> flatteningAuthorities = new HashSet<>();
        flatteningAuthorities.add(user.getAuth());
        if (roles != null) {
            for (Role role : roles) {
                flatteningAuthorities.add(role.getAuth());
            }
        }
        if (depts != null) {
            for (DeptWithParents dept : depts) {
                flatteningAuthorities.add(dept.getAuth());
                if (dept.getParents() != null){
                    for (ParentDept parent : dept.getParents()) {
                        flatteningAuthorities.add(parent.getAuth());
                    }
                }
            }
        }
        if (deptRoles != null) {
            for (DeptRole deptRole : deptRoles) {
                flatteningAuthorities.add(deptRole.getAuth());
            }
        }
        addAnotherBashAuth(flatteningAuthorities);
        return flatteningAuthorities;
    }

    /**
     * 添加其他权限对象，若子类继承可以扩展这里，也可以覆写listBaseAuth()
     * @param flatteningAuthorities
     */
    protected void addAnotherBashAuth(Collection<FlatteningAuthority> flatteningAuthorities){

    }

    @Override
    protected void setBaseAuth(List<IFlagAuthEntity> abstractAuthEntities) {
        for (IFlagAuthEntity abstractAuthEntity : abstractAuthEntities) {
            if (abstractAuthEntity instanceof User) {
                setUser(((User) abstractAuthEntity));
            } else if (abstractAuthEntity instanceof Role) {
                roles.add(((Role) abstractAuthEntity));
            } else if (abstractAuthEntity instanceof DeptWithParents) {
                depts.add(((DeptWithParents) abstractAuthEntity));
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

    /**
     * 用户权限不应该逆向，因为没有场景会根据用户的扁平化权限查询实际的权限；
     *
     * @param flatteningAuthority 基础权限
     * @return
     */
    @Override
    public IFlagAuthEntity parseBaseAuth(FlatteningAuthority flatteningAuthority) {
        throw new BizException("用户权限不应该逆向");
    }
}
