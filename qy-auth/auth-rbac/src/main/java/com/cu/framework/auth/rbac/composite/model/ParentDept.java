package com.cu.framework.auth.rbac.composite.model;

import com.cu.framework.auth.core.entity.AbstractIdNameFlagAuthEntity;

/**
 * 部门继承权限实体，权限串形如 {@code RELATIONDEPT__<部门ID>}，含义是该部门的<b>严格子孙部门</b>持有此权限。
 * <p>
 * 本部门自己的成员不因此获得权限，那部分由 {@link Dept} 的 {@code DEPT} 标记覆盖，两个标记正交。
 *
 * @author kewen
 * @since 2026-10-22
 */
public class ParentDept extends AbstractIdNameFlagAuthEntity<Long> {
    private static final long serialVersionUID = 1L;

    /**
     * 距本部门的层级，直接上级为 1、隔级为 2。不参与序列化，也不参与任何权限判定
     */
    private int level;

    public ParentDept() {
        super();
    }

    public ParentDept(Long id , String name,int level) {
        super();
        this.id=id;
        this.name=name;
        this.level=level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * 标记必须是单段令牌：权限的位置数是按次级分隔符拆标记得到的，旧值 {@code DEPT_RELATION} 会被拆成两个位置，
     * 而本类只有一个 ID，拼出的权限串因此错位
     */
    @Override
    public String flag() {
        return "RELATIONDEPT";
    }
}
