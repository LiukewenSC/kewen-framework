package com.cu.framework.sample.authcore.authobject;

import com.cu.framework.auth.core.entity.AbstractIdNameFlagAuthEntity;

/**
 * 用户维度的权限实体，权限串形如 {@code USER__1}。
 * <p>
 * ID 用字符串：这是权限标识自己的形态，与示例的业务主键（一律整型）无关。
 * 正因为它是字符串，才能把"权限串里含单引号"这种载荷端到端演一遍
 *
 * @author kewen
 * @since 2026-10-20
 */
public class DemoUser extends AbstractIdNameFlagAuthEntity<String> {

    private static final long serialVersionUID = 1L;

    /** 权限标记 */
    public static final String FLAG = "USER";

    public DemoUser() {
        super();
    }

    public DemoUser(String id, String name) {
        setId(id);
        setName(name);
    }

    @Override
    public String flag() {
        return FLAG;
    }
}
