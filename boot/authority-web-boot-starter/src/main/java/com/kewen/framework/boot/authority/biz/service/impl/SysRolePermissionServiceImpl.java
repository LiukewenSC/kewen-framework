package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.boot.authority.biz.entity.SysRolePermission;
import com.kewen.framework.boot.authority.biz.mapper.SysRolePermissionMapper;
import com.kewen.framework.boot.authority.biz.service.SysRolePermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 * 角色权限关联表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> implements SysRolePermissionService {

    @Override
    public Page<SysRolePermission> pageQuery(Integer pageNo, Integer pageSize, SysRolePermission queryModel) {
        Page<SysRolePermission> page = new Page<>();
        Wrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<SysRolePermission>()
                .orderByDesc(SysRolePermission::getUpdateTime);
        return this.page(page,wrapper);
    }

    @Override
    public List<SysRolePermission> findList(SysRolePermission queryModel) {
        Wrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<SysRolePermission>()
                .orderByDesc(SysRolePermission::getUpdateTime);
        return this.list(wrapper);
    }

}
