package com.kewen.framework.base.authority.service.impl;

import com.kewen.framework.base.authority.entity.SysUserGroup;
import com.kewen.framework.base.authority.mapper.SysUserGroupMapper;
import com.kewen.framework.base.authority.service.SysUserGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 * 用户角色组关联表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserGroupServiceImpl extends ServiceImpl<SysUserGroupMapper, SysUserGroup> implements SysUserGroupService {

    @Override
    public Page<SysUserGroup> pageQuery(Integer pageNo, Integer pageSize, SysUserGroup queryModel) {
        Page<SysUserGroup> page = new Page<>();
        Wrapper<SysUserGroup> wrapper = new LambdaQueryWrapper<SysUserGroup>()
                .orderByDesc(SysUserGroup::getUpdateTime);
        return this.page(page,wrapper);
    }

    @Override
    public List<SysUserGroup> findList(SysUserGroup queryModel) {
        Wrapper<SysUserGroup> wrapper = new LambdaQueryWrapper<SysUserGroup>()
                .orderByDesc(SysUserGroup::getUpdateTime);
        return this.list(wrapper);
    }

}
