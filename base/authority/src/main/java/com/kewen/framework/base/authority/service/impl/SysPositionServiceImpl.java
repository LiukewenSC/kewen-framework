package com.kewen.framework.base.authority.service.impl;

import com.kewen.framework.base.authority.entity.SysPosition;
import com.kewen.framework.base.authority.mapper.SysPositionMapper;
import com.kewen.framework.base.authority.service.SysPositionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 * 岗位表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysPositionServiceImpl extends ServiceImpl<SysPositionMapper, SysPosition> implements SysPositionService {

    @Override
    public Page<SysPosition> pageQuery(Integer pageNo, Integer pageSize, SysPosition queryModel) {
        Page<SysPosition> page = new Page<>();
        Wrapper<SysPosition> wrapper = new LambdaQueryWrapper<SysPosition>()
                .orderByDesc(SysPosition::getUpdateTime);
        return this.page(page,wrapper);
    }

    @Override
    public List<SysPosition> findList(SysPosition queryModel) {
        Wrapper<SysPosition> wrapper = new LambdaQueryWrapper<SysPosition>()
                .orderByDesc(SysPosition::getUpdateTime);
        return this.list(wrapper);
    }

}
