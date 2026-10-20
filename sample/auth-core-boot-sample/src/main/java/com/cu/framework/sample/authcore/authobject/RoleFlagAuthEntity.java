package com.cu.framework.sample.authcore.authobject;

import com.cu.framework.auth.core.entity.AbstractIdNameFlagAuthEntity;

/**
 * 角色维度的权限实体，权限串形如 {@code ROLE__10}
 *
 * @author kewen
 * @since 2026-10-20
 */
public class RoleFlagAuthEntity extends AbstractIdNameFlagAuthEntity<String> {

    /** 权限标记 */
    public static final String FLAG = "ROLE";

    @Override
    public String flag() {
        return FLAG;
    }
}
