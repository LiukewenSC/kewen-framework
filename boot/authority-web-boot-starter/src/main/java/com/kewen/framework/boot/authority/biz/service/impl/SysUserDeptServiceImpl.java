package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.base.common.model.DeptPrimary;
import com.kewen.framework.base.common.model.UserDept;
import com.kewen.framework.boot.authority.biz.entity.SysUserDept;
import com.kewen.framework.boot.authority.biz.mapper.SysUserDeptMapper;
import com.kewen.framework.boot.authority.biz.service.SysUserDeptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 * 用户部门关联表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserDeptServiceImpl extends ServiceImpl<SysUserDeptMapper, SysUserDept> implements SysUserDeptService {

    @Override
    public Page<SysUserDept> pageQuery(Integer pageNo, Integer pageSize, SysUserDept queryModel) {
        Page<SysUserDept> page = new Page<>();
        Wrapper<SysUserDept> wrapper = new LambdaQueryWrapper<SysUserDept>()
                .orderByDesc(SysUserDept::getUpdateTime);
        return this.page(page,wrapper);
    }

    @Override
    public List<SysUserDept> findList(SysUserDept queryModel) {
        Wrapper<SysUserDept> wrapper = new LambdaQueryWrapper<SysUserDept>()
                .orderByDesc(SysUserDept::getUpdateTime);
        return this.list(wrapper);
    }

}
