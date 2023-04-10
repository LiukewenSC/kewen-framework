package com.kewen.framework.boot.auth.bussiness.model;

import com.kewen.framework.base.authority.model.SysAuthorityObject;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-12-13 11:29
 */
@Data
public class BusinessAuthorityEditReq {

    @NotNull
    private Long businessId;

    @NotBlank
    private String module ;

    private String operate ="unified";

    @NotNull
    private SysAuthorityObject authority;
}
