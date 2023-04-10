package com.kewen.framework.boot.auth.annotation.authedit;

import com.kewen.framework.base.authority.model.SysAuthorityObject;
import lombok.Data;

/**
 * @descrpition 默认业务编辑权限对象
 * @author kewen
 * @since 2022-12-19 14:41
 */
@Data
public class DefaultAuthDataEditBusiness implements AuthDataEditBusiness {

    private Long id;
    private SysAuthorityObject authority;


    @Override
    public Long getBusinessId() {
        return id;
    }

    @Override
    public SysAuthorityObject getAuthorityObject() {
        return authority;
    }
}
