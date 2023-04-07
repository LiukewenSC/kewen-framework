package com.kewen.framework.base.authority.support;

import com.kewen.framework.base.authority.model.AuthorityObject;
import com.kewen.framework.base.common.model.Position;
import com.kewen.framework.base.common.model.Role;
import com.kewen.framework.base.common.model.UserDept;
import com.kewen.framework.base.common.model.UserDetail;

import java.util.Collection;
import java.util.List;

/**
 * @descrpition 系统用户相关联合查询
 * @author kewen
 * @since 2023-04-07
 */
public interface SysUserComposite {
    /**
     * 查询用户机构
     * @param userId 用户id
     * @return 用户机构（同时返回机构层级）
     */
    UserDept getUserDept(Long userId);

    /**
     * 查询用户角色
     * @param userId 用户id
     * @return 角色集合
     */
    List<Role> listUserRole(Long userId);

    /**
     * 查询用户岗位
     * @param userId 用户id
     * @return 岗位集合
     */
    List<Position> listUserPosition(Long userId);




    UserDetail getUserDetail(String loginInfo);
    UserDetail getUserDetailWithCredentials(String loginInfo);

}
