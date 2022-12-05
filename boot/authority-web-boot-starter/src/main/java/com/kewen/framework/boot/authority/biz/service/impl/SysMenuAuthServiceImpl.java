package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.boot.authority.biz.mapper.SysMenuAuthMapper;
import com.kewen.framework.boot.authority.biz.service.SysMenuAuthService;
import org.springframework.stereotype.Service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysMenuAuth;

@Service
public class SysMenuAuthServiceImpl extends ServiceImpl<SysMenuAuthMapper, SysMenuAuth> implements SysMenuAuthService {

    @Override
    public int updateBatch(List<SysMenuAuth> list) {
        return baseMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<SysMenuAuth> list) {
        return baseMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<SysMenuAuth> list) {
        return baseMapper.batchInsert(list);
    }

    @Override
    public int insertOrUpdate(SysMenuAuth record) {
        return baseMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(SysMenuAuth record) {
        return baseMapper.insertOrUpdateSelective(record);
    }
}

