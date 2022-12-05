package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.boot.authority.biz.mapper.SysDeptMapper;
import com.kewen.framework.boot.authority.biz.service.SysDeptService;
import org.springframework.stereotype.Service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysDept;

@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    public int updateBatch(List<SysDept> list) {
        return baseMapper.updateBatch(list);
    }
    @Override
    public int updateBatchSelective(List<SysDept> list) {
        return baseMapper.updateBatchSelective(list);
    }
    @Override
    public int batchInsert(List<SysDept> list) {
        return baseMapper.batchInsert(list);
    }
    @Override
    public int insertOrUpdate(SysDept record) {
        return baseMapper.insertOrUpdate(record);
    }
    @Override
    public int insertOrUpdateSelective(SysDept record) {
        return baseMapper.insertOrUpdateSelective(record);
    }
}
