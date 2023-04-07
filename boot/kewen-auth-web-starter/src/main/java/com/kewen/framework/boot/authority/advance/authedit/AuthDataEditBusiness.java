package com.kewen.framework.boot.authority.advance.authedit;

import com.kewen.framework.base.authority.model.AuthorityObject;

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
    AuthorityObject getAuthorityObject();

}
