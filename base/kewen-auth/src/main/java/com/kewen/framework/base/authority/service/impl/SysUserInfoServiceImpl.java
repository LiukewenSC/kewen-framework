package com.kewen.framework.base.authority.service.impl;

import com.kewen.framework.base.authority.entity.SysUserInfo;
import com.kewen.framework.base.authority.mapper.SysUserInfoMapper;
import com.kewen.framework.base.authority.service.SysUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserInfoServiceImpl extends ServiceImpl<SysUserInfoMapper, SysUserInfo> implements SysUserInfoService {

    @Override
    public Page<SysUserInfo> pageQuery(Integer pageNo, Integer pageSize, SysUserInfo queryModel) {
        Page<SysUserInfo> page = new Page<>();
        Wrapper<SysUserInfo> wrapper = new LambdaQueryWrapper<SysUserInfo>()
                .orderByDesc(SysUserInfo::getUpdateTime);
        return this.page(page,wrapper);
    }

    @Override
    public List<SysUserInfo> findList(SysUserInfo queryModel) {
        Wrapper<SysUserInfo> wrapper = new LambdaQueryWrapper<SysUserInfo>()
                .orderByDesc(SysUserInfo::getUpdateTime);
        return this.list(wrapper);
    }

}
