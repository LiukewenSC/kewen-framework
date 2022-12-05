package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.base.common.model.Role;
import com.kewen.framework.boot.authority.biz.mapper.SysUserRoleMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysUserRole;
import com.kewen.framework.boot.authority.biz.service.SysUserRoleService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

    @Override
    public int updateBatch(List<SysUserRole> list) {
        return baseMapper.updateBatch(list);
    }
    @Override
    public int updateBatchSelective(List<SysUserRole> list) {
        return baseMapper.updateBatchSelective(list);
    }
    @Override
    public int batchInsert(List<SysUserRole> list) {
        return baseMapper.batchInsert(list);
    }
    @Override
    public int insertOrUpdate(SysUserRole record) {
        return baseMapper.insertOrUpdate(record);
    }
    @Override
    public int insertOrUpdateSelective(SysUserRole record) {
        return baseMapper.insertOrUpdateSelective(record);
    }

    @Override
    public List<Role> listUserRole(Integer userId) {
        List<Role> roles = baseMapper.listUserRole(userId);
        return roles==null? Collections.emptyList():roles;
    }
}
