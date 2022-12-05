package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.boot.authority.biz.entity.SysPermission;
import com.kewen.framework.boot.authority.biz.mapper.SysPermissionMapper;
import com.kewen.framework.boot.authority.biz.service.SysPermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 * 权限表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    @Override
    public Page<SysPermission> pageQuery(Integer pageNo, Integer pageSize, SysPermission queryModel) {
        Page<SysPermission> page = new Page<>();
        Wrapper<SysPermission> wrapper = new LambdaQueryWrapper<SysPermission>()
                .orderByDesc(SysPermission::getUpdateTime);
        return this.page(page,wrapper);
    }

    @Override
    public List<SysPermission> findList(SysPermission queryModel) {
        Wrapper<SysPermission> wrapper = new LambdaQueryWrapper<SysPermission>()
                .orderByDesc(SysPermission::getUpdateTime);
        return this.list(wrapper);
    }

}
