package com.cu.framework.auth.rbac.composite.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cu.framework.auth.rbac.composite.SysUserComposite;
import com.cu.framework.auth.rbac.composite.mapper.SysUserUnionCompositeMapper;
import com.cu.framework.auth.rbac.composite.model.ParentDept;
import com.cu.framework.auth.rbac.composite.model.UserAuthInfo;
import com.cu.framework.auth.rbac.model.UserAuthObject;
import com.cu.framework.auth.rbac.mp.entity.SysDept;
import com.cu.framework.auth.rbac.mp.entity.SysUser;
import com.cu.framework.auth.rbac.mp.entity.SysUserCredential;
import com.cu.framework.auth.rbac.mp.service.SysDeptMpService;
import com.cu.framework.auth.rbac.mp.service.SysUserCredentialMpService;
import com.cu.framework.auth.rbac.mp.service.SysUserMpService;
import com.cu.framework.basic.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * @author kewen
 * @since 2024-05-11
 */
@Service
@RequiredArgsConstructor
public class SysUserCompositeImpl implements SysUserComposite {

    private static final Logger log = LoggerFactory.getLogger(SysUserCompositeImpl.class);
    private final SysUserMpService userMpService;

    private final SysUserCredentialMpService credentialMpService;

    private final SysUserUnionCompositeMapper unionCompositeMapper;
    private final SysDeptMpService deptMpService;

    @Override
    public List<ParentDept> listAncestorDepts(Long deptId) {
        return listAncestorDepts(deptId,
                id -> deptMpService.getOne(new LambdaQueryWrapper<SysDept>().eq(SysDept::getId, id)));
    }

    /**
     * 从本部门向上走<b>严格上级链（不含自身）</b>，按"根在前、直接上级在后"返回。
     * 查询动作以函数入参注入，使脏层级数据的终止行为能脱离数据库被直接断言。
     *
     * @param deptId     本部门 ID
     * @param deptLookup 按 ID 查部门，查不到时返回 null
     * @return 上级部门链，本部门是根或查不到时为空集合
     */
    static List<ParentDept> listAncestorDepts(Long deptId, Function<Long, SysDept> deptLookup) {
        if (deptId == null) {
            return Collections.emptyList();
        }
        SysDept self = deptLookup.apply(deptId);
        if (self == null) {
            return Collections.emptyList();
        }
        // 脏数据里 parent_id 可能自引用或成环，靠 visited 保证有限步内终止
        Set<Long> visited = new HashSet<>();
        visited.add(deptId);
        List<SysDept> reverseParents = new ArrayList<>();
        Long currentId = self.getParentId();
        while (currentId != null && currentId != 0L && visited.add(currentId)) {
            SysDept dept = deptLookup.apply(currentId);
            if (dept == null) {
                break;
            }
            reverseParents.add(dept);
            currentId = dept.getParentId();
        }
        // reverseParents 下标 0 就是直接上级，故 level = 下标 + 1 即"距自身的层级"
        List<ParentDept> parents = new ArrayList<>(reverseParents.size());
        for (int i = reverseParents.size() - 1; i >= 0; i--) {
            SysDept dept = reverseParents.get(i);
            parents.add(new ParentDept(dept.getId(), dept.getName(), i + 1));
        }
        return parents;
    }

    @Override
    public SysUserCredential getUserCredentialByUsername(String username) {
        SysUser user = userMpService.getOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (user == null){
            throw new BizException("用户不存在");
        }
        SysUserCredential credential = credentialMpService.getOne(
                new LambdaQueryWrapper<SysUserCredential>().eq(SysUserCredential::getUserId, user.getId())
        );
        if (credential == null){
            throw new BizException("用户凭证不存在");
        }
        return credential;
    }

    @Override
    public UserAuthObject getUserAuthObjectByUsername(String username) {

        SysUser user = userMpService.getOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (user == null){
            return null;
        }

        UserAuthObject userAuthObject = new UserAuthObject();
        userAuthObject.setSysUser(user);
        SysUserCredential credential = credentialMpService.getOne(
                new LambdaQueryWrapper<SysUserCredential>().eq(SysUserCredential::getUserId, user.getId())
        );
        if (credential == null){
            return userAuthObject;
        }
        userAuthObject.setSysUserCredential(credential);
        //查找用户权限体
        UserAuthInfo userAuthInfo = unionCompositeMapper.getUserAuthInfo(user.getId());
        if (userAuthInfo != null){
            userAuthInfo.fillUserAuth(this);
        }
        userAuthObject.setAuthInfo(userAuthInfo);
        return userAuthObject;
    }

    @Override
    public boolean updatePassword(String username, String encryptedNewPassword) {
        SysUser user = userMpService.getOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (user == null){
            log.info("更新用户失败，用户不存在");
            return false;
        }

       return credentialMpService.update(
                new LambdaUpdateWrapper<SysUserCredential>()
                        .eq(SysUserCredential::getUserId, user.getId())
                        .set(SysUserCredential::getPassword, encryptedNewPassword)
        );


    }
}
