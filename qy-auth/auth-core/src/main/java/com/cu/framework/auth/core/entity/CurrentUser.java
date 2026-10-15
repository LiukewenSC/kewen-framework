package com.cu.framework.auth.core.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 登录用户
 */
@Data
@Accessors(chain = true)
public class CurrentUser {
    private String username;
}
