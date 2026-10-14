package com.cu.framework.rbac.security;

import com.cu.framework.auth.core.entity.CurrentUser;
import com.cu.framework.auth.core.entity.IAuthObject;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RbacSecurityCurrentUser implements CurrentUser {

    private String username;
    private String password;
    private IAuthObject authObject;

}
