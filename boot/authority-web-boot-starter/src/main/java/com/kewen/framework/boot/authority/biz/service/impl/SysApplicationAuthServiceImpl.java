package com.kewen.framework.boot.authority.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kewen.framework.boot.authority.biz.mapper.SysApplicationAuthMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysApplicationAuth;
import com.kewen.framework.boot.authority.biz.service.SysApplicationAuthService;

import java.util.Collection;import java.util.List;

/**
 * @author kewen
 * @descrpition 业务权限 此处暂时没有修改
 * @since 2022-11-25 9:40
 */
public class SysApplicationAuthServiceImpl extends ServiceImpl<SysApplicationAuthMapper, SysApplicationAuth> implements SysApplicationAuthService {
    @Override
    public boolean hasAuth(Collection<String> auths, String module, String operate, Integer businessId) {
        SysApplicationAuth one = getOne(
                new LambdaQueryWrapper<SysApplicationAuth>()
                        .eq(SysApplicationAuth::getModule, module)
                        .eq(SysApplicationAuth::getBusinessId, businessId)
                        .eq(SysApplicationAuth::getOperate, operate)
                        .in(SysApplicationAuth::getAuthority, auths)
                        .last("limit 1")
                        .select(SysApplicationAuth::getId)
        );
        return one != null;
    }

    @Override
    public int updateBatch(List<SysApplicationAuth> list) {
        return baseMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<SysApplicationAuth> list) {
        return baseMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<SysApplicationAuth> list) {
        return baseMapper.batchInsert(list);
    }

    @Override
    public int insertOrUpdate(SysApplicationAuth record) {
        return baseMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(SysApplicationAuth record) {
        return baseMapper.insertOrUpdateSelective(record);
    }
}

