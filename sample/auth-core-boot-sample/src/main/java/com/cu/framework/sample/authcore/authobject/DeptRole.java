package com.cu.framework.sample.authcore.authobject;

import com.cu.framework.auth.core.entity.AuthConstant;
import com.cu.framework.auth.core.entity.IFlagAuthEntity;
import com.cu.framework.auth.core.exception.AuthEntityException;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 部门+角色的组合权限实体，权限串形如 {@code DEPT_ROLE__100_20}，
 * 描述形如 {@code DEPT_ROLE__研发部_资产角色}。
 * <p>
 * {@link com.cu.framework.auth.core.entity.AbstractIdNameFlagAuthEntity} 只支持单个 ID+Name，
 * 多位组合要直接实现 {@link IFlagAuthEntity}：标记里含一个 {@code AUTH_SUB_SPLIT} 就代表两个位置，
 * 位置 0 交部门、位置 1 交角色。本示例只需要"部门+角色"这一种组合，
 * 所以四个字段摊平放这里，不再为它单独建部门实体
 *
 * @author kewen
 * @since 2026-10-20
 */
@Data
public class DeptRole implements IFlagAuthEntity<String> {

    private static final long serialVersionUID = 1L;

    /** 组合权限标记：两个下划线分隔的组成部分，决定收发时的位置数为 2 */
    public static final String FLAG = "DEPT" + AuthConstant.AUTH_SUB_SPLIT + "ROLE";

    /** 位置 0：部门ID */
    private Long deptId;
    /** 位置 0：部门名 */
    private String deptName;
    /** 位置 1：角色ID，与 {@link DemoRole} 保持同一形态（字符串） */
    private String roleId;
    /** 位置 1：角色名 */
    private String roleName;

    public DeptRole() {
    }

    public DeptRole(Long deptId, String deptName, String roleId, String roleName) {
        this.deptId = deptId;
        this.deptName = deptName;
        this.roleId = roleId;
        this.roleName = roleName;
    }

    @Override
    public String flag() {
        return FLAG;
    }

    /**
     * 按位置交出 ID 与名字
     */
    @Override
    public Pair<String, String> serialize(int pos) {
        if (pos == 0) {
            if (deptId == null) {
                throw new AuthEntityException("组合权限缺少部门部分: " + FLAG);
            }
            return Pair.of(String.valueOf(deptId), deptName);
        }
        if (pos == 1) {
            return Pair.of(roleId, roleName);
        }
        throw new AuthEntityException("权限位置超出组合长度: " + pos);
    }

    /**
     * 按位置回填字段
     */
    @Override
    public void deSerialize(int pos, String posAuth, String posDescription) {
        if (pos == 0) {
            this.deptId = Long.valueOf(posAuth);
            this.deptName = posDescription;
        } else if (pos == 1) {
            this.roleId = posAuth;
            this.roleName = posDescription;
        } else {
            throw new AuthEntityException("权限字符串格式错误: " + pos + "-" + posAuth);
        }
    }
}
