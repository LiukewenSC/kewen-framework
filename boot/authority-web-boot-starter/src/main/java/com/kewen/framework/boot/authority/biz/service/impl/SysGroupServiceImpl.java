package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.boot.authority.biz.entity.SysGroup;
import com.kewen.framework.boot.authority.biz.mapper.SysGroupMapper;
import com.kewen.framework.boot.authority.biz.service.SysGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 * 角色组表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysGroupServiceImpl extends ServiceImpl<SysGroupMapper, SysGroup> implements SysGroupService {

    @Override
    public Page<SysGroup> pageQuery(Integer pageNo, Integer pageSize, SysGroup queryModel) {
        Page<SysGroup> page = new Page<>();
        Wrapper<SysGroup> wrapper = new LambdaQueryWrapper<SysGroup>()
                .orderByDesc(SysGroup::getUpdateTime);
        return this.page(page,wrapper);
    }

    @Override
    public List<SysGroup> findList(SysGroup queryModel) {
        Wrapper<SysGroup> wrapper = new LambdaQueryWrapper<SysGroup>()
                .orderByDesc(SysGroup::getUpdateTime);
        return this.list(wrapper);
    }

}
