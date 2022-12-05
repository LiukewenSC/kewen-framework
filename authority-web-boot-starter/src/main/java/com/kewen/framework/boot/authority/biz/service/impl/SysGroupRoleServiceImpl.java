package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.boot.authority.biz.mapper.SysGroupRoleMapper;
import com.kewen.framework.boot.authority.biz.service.SysGroupRoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysGroupRole;

@Service
public class SysGroupRoleServiceImpl extends ServiceImpl<SysGroupRoleMapper, SysGroupRole> implements SysGroupRoleService {

    @Override
    public int updateBatch(List<SysGroupRole> list) {
        return baseMapper.updateBatch(list);
    }
    @Override
    public int updateBatchSelective(List<SysGroupRole> list) {
        return baseMapper.updateBatchSelective(list);
    }
    @Override
    public int batchInsert(List<SysGroupRole> list) {
        return baseMapper.batchInsert(list);
    }
    @Override
    public int insertOrUpdate(SysGroupRole record) {
        return baseMapper.insertOrUpdate(record);
    }
    @Override
    public int insertOrUpdateSelective(SysGroupRole record) {
        return baseMapper.insertOrUpdateSelective(record);
    }
}
