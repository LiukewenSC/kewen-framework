package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.boot.authority.biz.mapper.SysPositionMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysPosition;
import com.kewen.framework.boot.authority.biz.service.SysPositionService;
import org.springframework.stereotype.Service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class SysPositionServiceImpl extends ServiceImpl<SysPositionMapper, SysPosition> implements SysPositionService {

    @Override
    public int updateBatch(List<SysPosition> list) {
        return baseMapper.updateBatch(list);
    }
    @Override
    public int updateBatchSelective(List<SysPosition> list) {
        return baseMapper.updateBatchSelective(list);
    }
    @Override
    public int batchInsert(List<SysPosition> list) {
        return baseMapper.batchInsert(list);
    }
    @Override
    public int insertOrUpdate(SysPosition record) {
        return baseMapper.insertOrUpdate(record);
    }
    @Override
    public int insertOrUpdateSelective(SysPosition record) {
        return baseMapper.insertOrUpdateSelective(record);
    }
}
