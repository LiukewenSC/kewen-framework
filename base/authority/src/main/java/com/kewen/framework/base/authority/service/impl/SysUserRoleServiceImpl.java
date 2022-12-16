package com.kewen.framework.base.authority.service.impl;

import com.kewen.framework.base.authority.entity.SysUserRole;
import com.kewen.framework.base.authority.mapper.SysUserRoleMapper;
import com.kewen.framework.base.authority.service.SysUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kewen.framework.base.common.model.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

    @Override
    public List<Role> listUserRole(Integer userId) {
        List<Role> roles = baseMapper.listUserRole(userId);
        return roles==null? Collections.emptyList():roles;
    }
}
