package com.kewen.framework.boot.authority.advance.authedit;

import com.kewen.framework.base.authority.model.AuthorityObject;
import lombok.Data;

/**
 * @descrpition 默认业务编辑权限对象
 * @author kewen
 * @since 2022-12-19 14:41
 */
@Data
public class DefaultAuthEditApplicationBusiness  implements AuthEditApplicationBusiness {

    private Integer id;
    private AuthorityObject authority;


    @Override
    public Integer getBusinessId() {
        return id;
    }

    @Override
    public AuthorityObject getAuthorityObject() {
        return authority;
    }
}
