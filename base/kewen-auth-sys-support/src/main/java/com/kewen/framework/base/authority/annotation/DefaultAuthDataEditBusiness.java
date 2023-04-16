package com.kewen.framework.base.authority.annotation;

import com.kewen.framework.base.auth.AuthEntity;
import com.kewen.framework.base.auth.annotation.authedit.AuthDataEditBusiness;
import com.kewen.framework.base.authority.model.SysAuthority;
import com.kewen.framework.base.authority.model.SysAuthorityObject;
import com.kewen.framework.base.authority.utils.AuthorityConvertUtil;
import lombok.Data;

import java.util.List;

/**
 * @descrpition 默认业务编辑权限对象
 * @author kewen
 * @since 2022-12-19 14:41
 */
@Data
public class DefaultAuthDataEditBusiness implements AuthDataEditBusiness<SysAuthority> {

    private Long id;
    private SysAuthorityObject authority;


    @Override
    public Long getBusinessId() {
        return id;
    }

    @Override
    public List<SysAuthority> getAuthEntities() {

        List<SysAuthority> list = AuthorityConvertUtil.to(authority);

        return list;
    }
}
