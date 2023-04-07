package com.kewen.framework.boot.authority.advance.authedit;

import com.kewen.framework.base.authority.model.AuthorityObject;
import lombok.Data;

/**
 * @descrpition 默认业务编辑权限对象
 * @author kewen
 * @since 2022-12-19 14:41
 */
@Data
public class DefaultAuthDataEditBusiness implements AuthDataEditBusiness {

    private Long id;
    private AuthorityObject authority;


    @Override
    public Long getBusinessId() {
        return id;
    }

    @Override
    public AuthorityObject getAuthorityObject() {
        return authority;
    }
}
