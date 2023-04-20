package com.kewen.framework.auth.sys.composite.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.kewen.framework.auth.core.model.AuthPasswordEncoder;
import com.kewen.framework.auth.sys.composite.SysUserComposite;
import com.kewen.framework.auth.sys.model.Dept;
import com.kewen.framework.auth.sys.model.DeptPrimary;
import com.kewen.framework.auth.sys.model.Position;
import com.kewen.framework.auth.sys.model.Role;
import com.kewen.framework.auth.sys.model.SysUserInfo;
import com.kewen.framework.auth.sys.model.UserDept;
import com.kewen.framework.auth.sys.model.req.UpdatePasswordReq;
import com.kewen.framework.auth.sys.mp.entity.SysUser;
import com.kewen.framework.auth.sys.mp.entity.SysUserCredential;
import com.kewen.framework.auth.sys.mp.service.SysUserCredentialMpService;
import com.kewen.framework.auth.sys.mp.service.SysUserMpService;
import com.kewen.framework.auth.sys.composite.mapper.SysUserCompositeMapper;
import com.kewen.framework.auth.sys.mp.service.SysMenuAuthMpService;
import com.kewen.framework.common.core.exception.AuthenticationException;
import com.kewen.framework.common.core.exception.BizException;
import com.kewen.framework.common.core.model.IdReq;
import com.kewen.framework.common.core.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @descrpition 系统用户相关联合查询
 * @author kewen
 * @since 2023-04-07
 */
@Component
public class SysUserCompositeImpl implements SysUserComposite {

    @Autowired
    SysUserCompositeMapper userCompositeMapper;
    @Autowired
    SysMenuAuthMpService sysMenuAuthMpService;
    @Autowired
    SysUserMpService userMpService;
    @Autowired
    SysUserCredentialMpService userCredentialsMpService;

    @Autowired
    AuthPasswordEncoder authPasswordEncoder;

    @Override
    public UserDept getUserDept(Long userId) {

        List<DeptPrimary> depts = userCompositeMapper.listUserDept(userId);

        if (depts==null){
            return null;
        }
        boolean hasPrimary=false;
        Dept deptPrimary=null;
        List<Dept> extras = new ArrayList<>();

        Iterator<DeptPrimary> iterator = depts.iterator();
        while (iterator.hasNext()){
            DeptPrimary next = iterator.next();
            if (next.getIsPrimary()){
                hasPrimary = true;
                deptPrimary=next;
                iterator.remove();
            } else {
                extras.add(next);
            }
        }

        //数据为空返回null
        if (deptPrimary == null && extras.size()==0){
            return null;
        }

        //如果没有主要机构，取最后一个，移除原有
        if (!hasPrimary && extras.size()>0){
            deptPrimary = extras.remove(extras.size() - 1);
        }

        UserDept userDept = new UserDept();
        userDept.setPrimary(deptPrimary);
        if (!CollectionUtils.isEmpty(extras)){
            userDept.setExtras(extras);
        }
        return userDept;
    }

    @Override
    public List<Role> listUserRole(Long userId) {
        List<Role> roles = userCompositeMapper.listUserRole(userId);
        return roles==null? Collections.emptyList():roles;
    }

    @Override
    public List<Position> listUserPosition(Long userId) {
        List<Position> positions = userCompositeMapper.listUserPosition(userId);
        return positions==null? Collections.emptyList():positions;
    }

    @Override
    public Long getUserId(String username) {
        SysUser sysUser = userMpService.getOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .or().eq(SysUser::getPhone,username)
                        .or().eq(SysUser::getEmail,username)
                        .select(SysUser::getId)
        );
        if (sysUser ==null){
            throw new BizException("未查询到用户");
        }
        return sysUser.getId();
    }

    @Override
    public SysUserInfo getSysUserInfo(Long userId) {
        SysUser sysUser = userMpService.getOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getId, userId)
        );
        if (sysUser == null) {
            throw new AuthenticationException("用户不存在");
        }

        //查询机构
        UserDept userDept = getUserDept(userId);

        //查询岗位
        List<Position> positions = listUserPosition(userId);

        //查询角色
        List<Role> roles = listUserRole(userId);


        return new SysUserInfo()
                .setUser(new User(sysUser.getId(),sysUser.getName()))
                .setDept(userDept)
                .setPositions(positions)
                .setRoles(roles)
                ;
    }

    @Override
    public SysUserCredential getUserCredential(Long userId){

        return userCredentialsMpService.getOne(
                new LambdaQueryWrapper<SysUserCredential>()
                        .eq(SysUserCredential::getUserId, userId)
        );

    }

    @Override
    public void updatePassword(UpdatePasswordReq req) {
        SysUser byId = userMpService.getById(req.getId());
        if (byId ==null){
            throw new BizException("未查询到用户");
        }
        SysUserCredential credential = userCredentialsMpService.getOne(
                new LambdaQueryWrapper<SysUserCredential>()
                        .eq(SysUserCredential::getUserId, req.getId())
                        .select(SysUserCredential::getPassword)
        );
        if (credential==null) {
            throw new BizException("未查询到用户凭证信息");
        }
        String encodePassword = credential.getPassword();
        if (!authPasswordEncoder.matches(req.getOldPassword(),encodePassword)) {
            throw new BizException("密码不匹配，无法修改");
        }
        String newPassword = authPasswordEncoder.encode(req.getNewPassword());
        userCredentialsMpService.update(
                new LambdaUpdateWrapper<SysUserCredential>()
                        .set(SysUserCredential::getPassword,newPassword)
                        .set(SysUserCredential::getRemark, req.getRemark()==null?"":req.getRemark())
                        .eq(SysUserCredential::getUserId,req.getId())
        );
    }

    @Override
    public void resetPassword(IdReq req) {
        SysUser byId = userMpService.getById(req.getId());
        if (byId ==null){
            throw new BizException("未查询到用户");
        }
        SysUserCredential credential = userCredentialsMpService.getOne(
                new LambdaQueryWrapper<SysUserCredential>()
                        .eq(SysUserCredential::getUserId, req.getId())
                        .select(SysUserCredential::getPassword)
        );
        String initPassword = authPasswordEncoder.encode("123456");
        if (credential ==null){
            userCredentialsMpService.save(
                    new SysUserCredential()
                            .setUserId(req.getId())
                            .setPassword(initPassword)
            );
        } else {
            userCredentialsMpService.update(
                    new LambdaUpdateWrapper<SysUserCredential>()
                            .set(SysUserCredential::getPassword,initPassword)
                            .eq(SysUserCredential::getUserId,req.getId())
            );
        }
    }
}
