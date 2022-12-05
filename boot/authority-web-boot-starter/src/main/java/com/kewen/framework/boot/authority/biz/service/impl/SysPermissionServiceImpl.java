package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.boot.authority.biz.mapper.SysPermissionMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysPermission;
import com.kewen.framework.boot.authority.biz.service.SysPermissionService;
import org.springframework.stereotype.Service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    @Override
    public int updateBatch(List<SysPermission> list) {
        return baseMapper.updateBatch(list);
    }
    @Override
    public int updateBatchSelective(List<SysPermission> list) {
        return baseMapper.updateBatchSelective(list);
    }
    @Override
    public int batchInsert(List<SysPermission> list) {
        return baseMapper.batchInsert(list);
    }
    @Override
    public int insertOrUpdate(SysPermission record) {
        return baseMapper.insertOrUpdate(record);
    }
    @Override
    public int insertOrUpdateSelective(SysPermission record) {
        return baseMapper.insertOrUpdateSelective(record);
    }
}
