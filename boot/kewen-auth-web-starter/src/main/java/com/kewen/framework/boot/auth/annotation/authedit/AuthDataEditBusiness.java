package com.kewen.framework.boot.auth.annotation.authedit;

import com.kewen.framework.base.authority.model.SysAuthorityObject;

/**
 * @descrpition 编辑权限
 * @author kewen
 * @since 2022-12-19 14:40
 */
public interface AuthDataEditBusiness {

    /**
     * 获取业务ID
     * @return
     */
    Long getBusinessId();

    /**
     * 获取权限对象
     * @return
     */
    SysAuthorityObject getAuthorityObject();

}
