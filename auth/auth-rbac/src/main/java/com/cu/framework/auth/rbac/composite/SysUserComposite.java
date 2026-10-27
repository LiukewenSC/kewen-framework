package com.cu.framework.auth.rbac.composite;

import com.cu.framework.auth.rbac.composite.model.Dept;
import com.cu.framework.auth.rbac.composite.model.ParentDept;
import com.cu.framework.auth.rbac.model.UserAuthObject;
import com.cu.framework.auth.rbac.mp.entity.SysDept;
import com.cu.framework.auth.rbac.mp.entity.SysUserCredential;

import java.util.List;

/**
 * 用户权限组合服务接口，负责按用户名装载用户权限对象以及修改用户密码
 * @author kewen
 * @since 2026-02-12
 */
public interface SysUserComposite{


    /**
     * 取本部门的<b>严格上级链</b>，按"根在前、直接上级在后"返回，<b>不含本部门</b>。
     * 本部门自身的权限由部门标记覆盖，与继承标记正交。
     * @param deptId 本部门 ID
     * @return 上级部门链，无上级时为空集合
     */
    List<ParentDept> listAncestorDepts(Long deptId);

    /**
     * 根据用户名获取用户凭证
     * @param username 用户名
     * @return 用户凭证
     */
    SysUserCredential getUserCredentialByUsername(String username);

    /**
     * 根据用户名加载用户信息、用户凭证以及用户权限集合体
     * @param username 用户名
     * @return 用户权限对象，用户不存在时返回 null
     */
    UserAuthObject getUserAuthObjectByUsername(String username);

    /**
     * 修改密码
     * @param username 用户名
     * @param encodedNewPassword 新密码，已经做了加密处理后的密码
     * @return
     */
    boolean updatePassword(String username, String encodedNewPassword);

}
