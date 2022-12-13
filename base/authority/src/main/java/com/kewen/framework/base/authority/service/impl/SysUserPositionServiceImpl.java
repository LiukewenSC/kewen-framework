package com.kewen.framework.base.authority.service.impl;

import com.kewen.framework.base.authority.entity.SysUserPosition;
import com.kewen.framework.base.authority.mapper.SysUserPositionMapper;
import com.kewen.framework.base.authority.service.SysUserPositionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kewen.framework.base.common.model.Position;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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
    public List<Position> listUserPosition(Long userId) {
        List<Position> positions = baseMapper.listUserPosition(userId);
        return positions==null? Collections.emptyList():positions;
    }
}
