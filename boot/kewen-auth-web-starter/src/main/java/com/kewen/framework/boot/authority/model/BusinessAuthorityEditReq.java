package com.kewen.framework.boot.authority.model;

import com.kewen.framework.base.authority.model.AuthorityObject;
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
    private Integer businessId;

    @NotBlank
    private String module ;

    private String operate ="unified";

    @NotNull
    private AuthorityObject authority;
}
