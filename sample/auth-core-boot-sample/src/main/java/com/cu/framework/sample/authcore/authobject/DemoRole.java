package com.cu.framework.sample.authcore.authobject;

import com.cu.framework.auth.core.entity.AbstractIdNameFlagAuthEntity;

/**
 * 角色维度的权限实体，权限串形如 {@code ROLE__10}
 *
 * @author kewen
 * @since 2026-10-20
 */
public class DemoRole extends AbstractIdNameFlagAuthEntity<String> {

    private static final long serialVersionUID = 1L;

    /** 权限标记 */
    public static final String FLAG = "ROLE";

    public DemoRole() {
        super();
    }

    public DemoRole(String id, String name) {
        setId(id);
        setName(name);
    }

    @Override
    public String flag() {
        return FLAG;
    }
}
