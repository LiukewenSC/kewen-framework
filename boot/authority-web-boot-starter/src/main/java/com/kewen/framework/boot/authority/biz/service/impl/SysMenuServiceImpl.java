package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.boot.authority.biz.entity.SysMenu;
import com.kewen.framework.boot.authority.biz.mapper.SysMenuMapper;
import com.kewen.framework.boot.authority.biz.service.SysMenuService;
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
 * @since 2022-12-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {
    @Override
    public boolean hasAuth(Collection<String> authorities, String url) {
        Integer auth = baseMapper.hasAuth(authorities, url);
        return auth != null && auth > 0;
    }
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
