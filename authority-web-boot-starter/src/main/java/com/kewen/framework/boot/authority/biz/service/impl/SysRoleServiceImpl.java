package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.boot.authority.biz.mapper.SysRoleMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysRole;
import com.kewen.framework.boot.authority.biz.service.SysRoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Override
    public int updateBatch(List<SysRole> list) {
        return baseMapper.updateBatch(list);
    }
    @Override
    public int updateBatchSelective(List<SysRole> list) {
        return baseMapper.updateBatchSelective(list);
    }
    @Override
    public int batchInsert(List<SysRole> list) {
        return baseMapper.batchInsert(list);
    }
    @Override
    public int insertOrUpdate(SysRole record) {
        return baseMapper.insertOrUpdate(record);
    }
    @Override
    public int insertOrUpdateSelective(SysRole record) {
        return baseMapper.insertOrUpdateSelective(record);
    }
}
