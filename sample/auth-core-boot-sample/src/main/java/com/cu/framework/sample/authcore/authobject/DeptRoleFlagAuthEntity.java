package com.cu.framework.sample.authcore.authobject;

import com.cu.framework.auth.core.entity.IFlagAuthEntity;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 部门+角色的组合权限实体，权限串形如 {@code DEPT_ROLE__100_20}，
 * 描述形如 {@code DEPT_ROLE__研发部_资产角色}。
 * <p>
 * {@link com.cu.framework.auth.core.entity.AbstractIdNameFlagAuthEntity} 只支持单个 ID+Name，
 * 多位组合要直接实现 {@link IFlagAuthEntity} 并按位置收发，本示例用它来覆盖这条扩展路径
 *
 * @author kewen
 * @since 2026-10-20
 */
public class DeptRoleFlagAuthEntity implements IFlagAuthEntity<Long> {

    private static final long serialVersionUID = 1L;

    /** 权限标记，两个下划线分隔的组成部分，决定收发时的位置数为 2 */
    public static final String FLAG = "DEPT_ROLE";

    /** 位置 0 的部门ID */
    private Long deptId;
    /** 位置 0 的部门名 */
    private String deptName;
    /** 位置 1 的角色ID */
    private Long roleId;
    /** 位置 1 的角色名 */
    private String roleName;

    @Override
    public String flag() {
        return FLAG;
    }

    /**
     * 按位置交出 ID 与名字，位置编号与 {@code AUTH_SUB_SPLIT} 分隔出的段一一对应
     */
    @Override
    public Pair<String, String> serialize(int pos) {
        if (pos == 0) {
            return Pair.of(String.valueOf(deptId), deptName);
        }
        return Pair.of(String.valueOf(roleId), roleName);
    }

    /**
     * 按位置回填 ID 与名字
     */
    @Override
    public void deSerialize(int pos, String posAuth, String posDescription) {
        if (pos == 0) {
            this.deptId = Long.valueOf(posAuth);
            this.deptName = posDescription;
        } else {
            this.roleId = Long.valueOf(posAuth);
            this.roleName = posDescription;
        }
    }

    public Long getDeptId() {
        return deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public Long getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }
}
