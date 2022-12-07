package com.kewen.framework.base.authority.service.impl;

import com.kewen.framework.base.authority.entity.SysGroupRole;
import com.kewen.framework.base.authority.mapper.SysGroupRoleMapper;
import com.kewen.framework.base.authority.service.SysGroupRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 * 角色组角色关联表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysGroupRoleServiceImpl extends ServiceImpl<SysGroupRoleMapper, SysGroupRole> implements SysGroupRoleService {

    @Override
    public Page<SysGroupRole> pageQuery(Integer pageNo, Integer pageSize, SysGroupRole queryModel) {
        Page<SysGroupRole> page = new Page<>();
        Wrapper<SysGroupRole> wrapper = new LambdaQueryWrapper<SysGroupRole>()
                .orderByDesc(SysGroupRole::getUpdateTime);
        return this.page(page,wrapper);
    }

    @Override
    public List<SysGroupRole> findList(SysGroupRole queryModel) {
        Wrapper<SysGroupRole> wrapper = new LambdaQueryWrapper<SysGroupRole>()
                .orderByDesc(SysGroupRole::getUpdateTime);
        return this.list(wrapper);
    }

}
