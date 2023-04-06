package com.kewen.framework.base.authority.service.impl;

import com.kewen.framework.base.authority.entity.SysMenu;
import com.kewen.framework.base.authority.mapper.SysMenuMapper;
import com.kewen.framework.base.authority.service.SysMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Override
    public Page<SysMenu> pageQuery(Integer pageNo, Integer pageSize, SysMenu queryModel) {
        Page<SysMenu> page = new Page<>();
        Wrapper<SysMenu> wrapper = new LambdaQueryWrapper<SysMenu>()
                .orderByDesc(SysMenu::getUpdateTime);
        return this.page(page,wrapper);
    }

    @Override
    public List<SysMenu> findList(SysMenu queryModel) {
        Wrapper<SysMenu> wrapper = new LambdaQueryWrapper<SysMenu>()
                .orderByDesc(SysMenu::getUpdateTime);
        return this.list(wrapper);
    }

}
