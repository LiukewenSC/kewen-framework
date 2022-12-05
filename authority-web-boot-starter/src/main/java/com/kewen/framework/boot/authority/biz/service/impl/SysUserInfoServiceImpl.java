package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.boot.authority.biz.mapper.SysUserInfoMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysUserInfo;
import com.kewen.framework.boot.authority.biz.service.SysUserInfoService;
import org.springframework.stereotype.Service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class SysUserInfoServiceImpl extends ServiceImpl<SysUserInfoMapper, SysUserInfo> implements SysUserInfoService {

    @Override
    public int updateBatch(List<SysUserInfo> list) {
        return baseMapper.updateBatch(list);
    }
    @Override
    public int updateBatchSelective(List<SysUserInfo> list) {
        return baseMapper.updateBatchSelective(list);
    }
    @Override
    public int batchInsert(List<SysUserInfo> list) {
        return baseMapper.batchInsert(list);
    }
    @Override
    public int insertOrUpdate(SysUserInfo record) {
        return baseMapper.insertOrUpdate(record);
    }
    @Override
    public int insertOrUpdateSelective(SysUserInfo record) {
        return baseMapper.insertOrUpdateSelective(record);
    }
}
