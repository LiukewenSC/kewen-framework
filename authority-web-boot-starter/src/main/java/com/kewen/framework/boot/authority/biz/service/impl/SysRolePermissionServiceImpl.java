package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.common.model.Permission;
import com.kewen.framework.boot.authority.biz.mapper.SysRolePermissionMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysRolePermission;
import com.kewen.framework.boot.authority.biz.service.SysRolePermissionService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> implements SysRolePermissionService {

    @Override
    public int updateBatch(List<SysRolePermission> list) {
        return baseMapper.updateBatch(list);
    }
    @Override
    public int updateBatchSelective(List<SysRolePermission> list) {
        return baseMapper.updateBatchSelective(list);
    }
    @Override
    public int batchInsert(List<SysRolePermission> list) {
        return baseMapper.batchInsert(list);
    }
    @Override
    public int insertOrUpdate(SysRolePermission record) {
        return baseMapper.insertOrUpdate(record);
    }
    @Override
    public int insertOrUpdateSelective(SysRolePermission record) {
        return baseMapper.insertOrUpdateSelective(record);
    }

    @Override
    public List<Permission> listRolePosition(List<Integer> roleIds) {
        List<Permission> permissions = baseMapper.listRolePosition(roleIds);
        return permissions==null? Collections.emptyList():permissions;
    }
}
