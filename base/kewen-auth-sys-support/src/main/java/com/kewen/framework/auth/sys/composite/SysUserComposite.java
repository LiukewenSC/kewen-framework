package com.kewen.framework.auth.sys.composite;

import com.kewen.framework.auth.sys.model.Position;
import com.kewen.framework.auth.sys.model.Role;
import com.kewen.framework.auth.sys.model.SysUserInfo;
import com.kewen.framework.auth.sys.model.UserDept;
import com.kewen.framework.auth.sys.model.req.UpdatePasswordReq;
import com.kewen.framework.auth.sys.mp.entity.SysUserCredential;
import com.kewen.framework.common.core.model.IdReq;

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

    Long getUserId(String username);
    SysUserInfo getSysUserInfo(Long userId);

    /**
     * 获取账号凭证信息
     * @param userId 用户id
     * @return
     */
    SysUserCredential getUserCredential(Long userId);

    /**
     * 修改密码
     * @param req
     */
    void updatePassword(UpdatePasswordReq req);

    void resetPassword(IdReq req);


}
