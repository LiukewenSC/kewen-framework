package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.base.common.model.Position;
import com.kewen.framework.boot.authority.biz.mapper.SysUserPositionMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysUserPosition;
import com.kewen.framework.boot.authority.biz.service.SysUserPositionService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class SysUserPositionServiceImpl extends ServiceImpl<SysUserPositionMapper, SysUserPosition> implements SysUserPositionService {

    @Override
    public List<Position> listUserPosition(Integer userId) {
        List<Position> positions = baseMapper.listUserPosition(userId);
        return positions==null? Collections.emptyList():positions;
    }

    @Override
    public int updateBatch(List<SysUserPosition> list) {
        return baseMapper.updateBatch(list);
    }
    @Override
    public int updateBatchSelective(List<SysUserPosition> list) {
        return baseMapper.updateBatchSelective(list);
    }
    @Override
    public int batchInsert(List<SysUserPosition> list) {
        return baseMapper.batchInsert(list);
    }
    @Override
    public int insertOrUpdate(SysUserPosition record) {
        return baseMapper.insertOrUpdate(record);
    }
    @Override
    public int insertOrUpdateSelective(SysUserPosition record) {
        return baseMapper.insertOrUpdateSelective(record);
    }
}
