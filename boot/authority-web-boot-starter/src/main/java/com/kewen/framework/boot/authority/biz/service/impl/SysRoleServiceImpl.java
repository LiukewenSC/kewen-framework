package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.boot.authority.biz.entity.SysRole;
import com.kewen.framework.boot.authority.biz.mapper.SysRoleMapper;
import com.kewen.framework.boot.authority.biz.service.SysRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Override
    public Page<SysRole> pageQuery(Integer pageNo, Integer pageSize, SysRole queryModel) {
        Page<SysRole> page = new Page<>();
        Wrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .orderByDesc(SysRole::getUpdateTime);
        return this.page(page,wrapper);
    }

    @Override
    public List<SysRole> findList(SysRole queryModel) {
        Wrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .orderByDesc(SysRole::getUpdateTime);
        return this.list(wrapper);
    }

}
