package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.boot.authority.biz.mapper.SysUserGroupMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysUserGroup;
import com.kewen.framework.boot.authority.biz.service.SysUserGroupService;
import org.springframework.stereotype.Service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class SysUserGroupServiceImpl extends ServiceImpl<SysUserGroupMapper, SysUserGroup> implements SysUserGroupService {

    @Override
    public int updateBatch(List<SysUserGroup> list) {
        return baseMapper.updateBatch(list);
    }
    @Override
    public int updateBatchSelective(List<SysUserGroup> list) {
        return baseMapper.updateBatchSelective(list);
    }
    @Override
    public int batchInsert(List<SysUserGroup> list) {
        return baseMapper.batchInsert(list);
    }
    @Override
    public int insertOrUpdate(SysUserGroup record) {
        return baseMapper.insertOrUpdate(record);
    }
    @Override
    public int insertOrUpdateSelective(SysUserGroup record) {
        return baseMapper.insertOrUpdateSelective(record);
    }
}
