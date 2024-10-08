package com.kewen.framework.auth.core.annotation.authedit;

import com.kewen.framework.auth.core.model.AuthEntity;

import java.util.List;

/**
 * @descrpition 编辑权限
 * @author kewen
 * @since 2022-12-19 14:40
 */
public interface AuthDataEditBusiness<T extends AuthEntity> {

    /**
     * 获取业务ID
     * @return
     */
    Long getBusinessId();

    /**
     * 获取权限对象
     * @return
     */
    List<T> getAuthEntities();

}
