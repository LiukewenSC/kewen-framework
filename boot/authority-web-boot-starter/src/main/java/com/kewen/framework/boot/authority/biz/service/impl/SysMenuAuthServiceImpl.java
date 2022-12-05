package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.boot.authority.biz.entity.SysMenuAuth;
import com.kewen.framework.boot.authority.biz.mapper.SysMenuAuthMapper;
import com.kewen.framework.boot.authority.biz.service.SysMenuAuthService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 * 菜单权限表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysMenuAuthServiceImpl extends ServiceImpl<SysMenuAuthMapper, SysMenuAuth> implements SysMenuAuthService {

    @Override
    public Page<SysMenuAuth> pageQuery(Integer pageNo, Integer pageSize, SysMenuAuth queryModel) {
        Page<SysMenuAuth> page = new Page<>();
        Wrapper<SysMenuAuth> wrapper = new LambdaQueryWrapper<SysMenuAuth>()
                .orderByDesc(SysMenuAuth::getUpdateTime);
        return this.page(page,wrapper);
    }

    @Override
    public List<SysMenuAuth> findList(SysMenuAuth queryModel) {
        Wrapper<SysMenuAuth> wrapper = new LambdaQueryWrapper<SysMenuAuth>()
                .orderByDesc(SysMenuAuth::getUpdateTime);
        return this.list(wrapper);
    }

}
