package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.boot.authority.biz.entity.SysUserRole;
import com.kewen.framework.boot.authority.biz.mapper.SysUserRoleMapper;
import com.kewen.framework.boot.authority.biz.service.SysUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

    @Override
    public Page<SysUserRole> pageQuery(Integer pageNo, Integer pageSize, SysUserRole queryModel) {
        Page<SysUserRole> page = new Page<>();
        Wrapper<SysUserRole> wrapper = new LambdaQueryWrapper<SysUserRole>()
                .orderByDesc(SysUserRole::getUpdateTime);
        return this.page(page,wrapper);
    }

    @Override
    public List<SysUserRole> findList(SysUserRole queryModel) {
        Wrapper<SysUserRole> wrapper = new LambdaQueryWrapper<SysUserRole>()
                .orderByDesc(SysUserRole::getUpdateTime);
        return this.list(wrapper);
    }

}
