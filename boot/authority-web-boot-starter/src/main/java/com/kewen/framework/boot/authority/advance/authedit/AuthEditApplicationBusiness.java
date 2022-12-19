package com.kewen.framework.boot.authority.advance.authedit;

import com.kewen.framework.base.authority.model.AuthorityObject;

/**
 * @descrpition 编辑权限
 * @author kewen
 * @since 2022-12-19 14:40
 */
public interface AuthEditApplicationBusiness {

    /**
     * 获取业务ID
     * @return
     */
    Integer getBusinessId();

    /**
     * 获取权限对象
     * @return
     */
    AuthorityObject getAuthorityObject();

}
