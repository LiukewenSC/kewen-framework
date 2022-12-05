package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.boot.authority.biz.entity.SysDept;
import com.kewen.framework.boot.authority.biz.mapper.SysDeptMapper;
import com.kewen.framework.boot.authority.biz.service.SysDeptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 * 部门表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    public Page<SysDept> pageQuery(Integer pageNo, Integer pageSize, SysDept queryModel) {
        Page<SysDept> page = new Page<>();
        Wrapper<SysDept> wrapper = new LambdaQueryWrapper<SysDept>()
                .orderByDesc(SysDept::getUpdateTime);
        return this.page(page,wrapper);
    }

    @Override
    public List<SysDept> findList(SysDept queryModel) {
        Wrapper<SysDept> wrapper = new LambdaQueryWrapper<SysDept>()
                .orderByDesc(SysDept::getUpdateTime);
        return this.list(wrapper);
    }

}
