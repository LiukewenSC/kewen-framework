package com.kewen.framework.auth.sys.model.req;

import com.kewen.framework.auth.sys.mp.entity.SysUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 
 * @author kewen
 * @since 2023-04-19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserEditReq extends SysUser {
    private List<Long> roleIds;
}