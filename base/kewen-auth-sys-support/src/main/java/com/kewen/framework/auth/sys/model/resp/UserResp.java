package com.kewen.framework.auth.sys.model.resp;

import com.kewen.framework.auth.sys.model.Position;
import com.kewen.framework.auth.sys.model.Role;
import com.kewen.framework.auth.sys.model.UserDept;
import com.kewen.framework.auth.sys.mp.entity.SysUser;
import com.kewen.framework.common.core.utils.CopyObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

/**
 * 
 * @author kewen
 * @since 2023-04-19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserResp extends SysUser implements CopyObject<SysUser> {
    protected UserDept dept;
    protected Collection<Position> positions;
    protected Collection<Role> roles;
}