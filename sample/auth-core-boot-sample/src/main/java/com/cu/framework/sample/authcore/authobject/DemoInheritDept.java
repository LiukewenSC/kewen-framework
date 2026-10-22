package com.cu.framework.sample.authcore.authobject;

import com.cu.framework.auth.core.entity.AbstractIdNameFlagAuthEntity;

/**
 * 继承型权限实体，权限串形如 {@code DEPTINHERIT__100}，语义是"100 号部门的下级部门成员持有此权限"。
 * <p>
 * 核心层只负责按形态把这条串编解码，<b>不认识"下级"这个概念</b>——从部门树把它展开成用户身上的串，
 * 是具体权限模型的职责（见 auth-rbac 示例）。本实体存在的目的，是证明核心层对这种
 * "语义由上层自定义"的标记一视同仁：能拼、能拆、能比对。
 * <p>
 * 标记必须是单段令牌：权限的位置数由标记按次级分隔符拆分得到，标记里含下划线会被当成多个位置，
 * 拼出来的串就与实体对不上
 *
 * @author kewen
 * @since 2026-10-22
 */
public class DemoInheritDept extends AbstractIdNameFlagAuthEntity<Long> {

    private static final long serialVersionUID = 1L;

    /** 权限标记 */
    public static final String FLAG = "DEPTINHERIT";

    public DemoInheritDept() {
        super();
    }

    public DemoInheritDept(Long id, String name) {
        setId(id);
        setName(name);
    }

    @Override
    public String flag() {
        return FLAG;
    }
}
