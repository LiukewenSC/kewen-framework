package com.kewen.framework.base.authority.service.impl;

import com.kewen.framework.base.authority.entity.SysUser;
import com.kewen.framework.base.authority.mapper.SysUserMapper;
import com.kewen.framework.base.authority.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public Page<SysUser> pageQuery(Integer pageNo, Integer pageSize, SysUser queryModel) {
        Page<SysUser> page = new Page<>();
        Wrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .orderByDesc(SysUser::getUpdateTime);
        return this.page(page,wrapper);
    }

    @Override
    public List<SysUser> findList(SysUser queryModel) {
        Wrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .orderByDesc(SysUser::getUpdateTime);
        return this.list(wrapper);
    }

}
