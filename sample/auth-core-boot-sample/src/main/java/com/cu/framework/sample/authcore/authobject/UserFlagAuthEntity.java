package com.cu.framework.sample.authcore.authobject;

import com.cu.framework.auth.core.entity.AbstractIdNameFlagAuthEntity;

/**
 * 用户维度的权限实体，权限串形如 {@code USER__1}。
 * <p>
 * ID 用字符串：这是权限标识自己的形态，与示例业务主键（一律整型）无关，
 * 因此权限串里允许出现整型装不下的字符，示例正是要拿这一点去验证
 * "权限串只作为数据参与比对、不被解释成语句片段"
 *
 * @author kewen
 * @since 2026-10-20
 */
public class UserFlagAuthEntity extends AbstractIdNameFlagAuthEntity<String> {

    /** 权限标记 */
    public static final String FLAG = "USER";

    @Override
    public String flag() {
        return FLAG;
    }
}
