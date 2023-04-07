package com.kewen.framework.boot.auth.bussiness.model;

import com.kewen.framework.base.authority.model.AuthorityObject;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @descrpition 菜单编辑权限入参
 * @author kewen
 * @since 2022-12-13 10:33
 */
@Data
public class MenuAuthorityEditReq {

    @NotNull
    private Long menuId;

    @NotNull
    private AuthorityObject authority;

}
