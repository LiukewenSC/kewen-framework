package com.kewen.framework.base.authority.model;

import com.kewen.framework.base.common.model.UserDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author kewen
 * @descrpition
 * @since 2023-04-07
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserAuthenticationDetail extends UserDetail {

    private String password;


}
