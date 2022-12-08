package com.kewen.framework.base.authority.service.impl;

import com.kewen.framework.base.authority.entity.SysUserPosition;
import com.kewen.framework.base.authority.mapper.SysUserPositionMapper;
import com.kewen.framework.base.authority.service.SysUserPositionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 * 用户岗位关联表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserPositionServiceImpl extends ServiceImpl<SysUserPositionMapper, SysUserPosition> implements SysUserPositionService {

    @Override
    public Page<SysUserPosition> pageQuery(Integer pageNo, Integer pageSize, SysUserPosition queryModel) {
        Page<SysUserPosition> page = new Page<>();
        Wrapper<SysUserPosition> wrapper = new LambdaQueryWrapper<SysUserPosition>()
                .orderByDesc(SysUserPosition::getUpdateTime);
        return this.page(page,wrapper);
    }

    @Override
    public List<SysUserPosition> findList(SysUserPosition queryModel) {
        Wrapper<SysUserPosition> wrapper = new LambdaQueryWrapper<SysUserPosition>()
                .orderByDesc(SysUserPosition::getUpdateTime);
        return this.list(wrapper);
    }

}
