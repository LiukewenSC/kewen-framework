package com.kewen.framework.auth.sys.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kewen.framework.auth.sys.composite.SysUserComposite;
import com.kewen.framework.auth.sys.model.SysUserInfo;
import com.kewen.framework.auth.sys.model.req.UserEditReq;
import com.kewen.framework.auth.sys.model.req.UserPageReq;
import com.kewen.framework.auth.sys.model.resp.UserResp;
import com.kewen.framework.auth.sys.mp.entity.SysRole;
import com.kewen.framework.auth.sys.mp.entity.SysUser;
import com.kewen.framework.auth.sys.mp.entity.SysUserRole;
import com.kewen.framework.auth.sys.mp.service.SysRoleMpService;
import com.kewen.framework.auth.sys.mp.service.SysUserMpService;
import com.kewen.framework.auth.sys.mp.service.SysUserRoleMpService;
import com.kewen.framework.common.core.model.PageResult;
import com.kewen.framework.jdbc.core.utils.PageUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * @author kewen
 * @since 2023-04-19
 */
@Service
public class SysUserService {


    @Autowired
    SysUserMpService userMpService;
    @Autowired
    SysRoleMpService roleMpService;
    @Autowired
    SysUserRoleMpService userRoleMpService;

    @Autowired
    SysUserComposite userComposite;


    public UserResp getUser(Long userId) {
        SysUser byId = userMpService.getById(userId);

        return convert2Resp(byId);
    }

    public PageResult<UserResp> pageUser(UserPageReq req) {

        Page<SysUser> page = userMpService.page(
                PageUtils.page(req),
                new LambdaQueryWrapper<SysUser>()
                        .like(StringUtils.isNotBlank(req.getName()),SysUser::getName,req.getName())
        );

        List<SysUser> records = page.getRecords();

        List<UserResp> userResps = new ArrayList<>();
        for (SysUser record : records) {
            UserResp resp = convert2Resp(record);
            userResps.add(resp);
        }
        return PageResult.of(req, (int) page.getTotal(), userResps);
    }
    private UserResp convert2Resp(SysUser user){
        UserResp userResp = new UserResp();
        userResp.copyProperties(user);
        SysUserInfo sysUserInfo = userComposite.getSysUserInfo(user.getId());
        userResp.setDept(sysUserInfo.getDept());
        userResp.setPositions(sysUserInfo.getPositions());
        userResp.setRoles(sysUserInfo.getRoles());
        return userResp;
    }

    @Transactional
    public void addUser(UserEditReq req) {
        userMpService.save(req);
        if (!CollectionUtils.isEmpty(req.getRoleIds())){
            userRoleMpService.saveBatch(
                    req.getRoleIds().stream()
                            .map(rId->new SysUserRole().setUserId(req.getId()).setRoleId(rId))
                            .collect(Collectors.toList())
            );
        }
    }
    @Transactional
    public void updateUser(UserEditReq req) {
        userMpService.updateById(req);
        userRoleMpService.remove(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId,req.getId())
        );
        if (!CollectionUtils.isEmpty(req.getRoleIds())){
            userRoleMpService.saveBatch(
                    req.getRoleIds().stream()
                            .map(rId->new SysUserRole().setUserId(req.getId()).setRoleId(rId))
                            .collect(Collectors.toList())
            );
        }
    }

    public void deleteUser(Long id) {
        userMpService.removeById(id);
    }

    public int userCount() {
        long count = userMpService.count();
        return (int) count;
    }


    public PageResult<SysRole> pageRole(UserPageReq req) {

        Page<SysRole> page = roleMpService.page(
                PageUtils.page(req),
                new LambdaQueryWrapper<SysRole>()
                        .like(StringUtils.isNotBlank(req.getName()),SysRole::getName,req.getName())
        );
        return PageUtils.page(page);
    }

    public void addRole(SysRole req) {
        roleMpService.save(req);
    }

    public void updateRole(SysRole req) {
        roleMpService.updateById(req);
    }

    public void deleteRole(Long id) {
        roleMpService.removeById(id);
    }
}
