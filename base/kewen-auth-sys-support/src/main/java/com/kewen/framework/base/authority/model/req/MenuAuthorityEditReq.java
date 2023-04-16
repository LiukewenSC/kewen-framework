package com.kewen.framework.base.authority.model.req;

import com.kewen.framework.base.authority.model.SysAuthorityObject;
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
    private SysAuthorityObject authority;

}
