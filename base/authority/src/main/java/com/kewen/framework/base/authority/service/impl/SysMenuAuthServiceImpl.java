package com.kewen.framework.base.authority.service.impl;

import com.kewen.framework.base.authority.entity.SysMenuAuth;
import com.kewen.framework.base.authority.mapper.SysMenuAuthMapper;
import com.kewen.framework.base.authority.model.Authority;
import com.kewen.framework.base.authority.model.AuthorityObject;
import com.kewen.framework.base.authority.service.SysMenuAuthService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kewen.framework.base.authority.utils.AuthorityConvertUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单权限表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
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

    @Override
    public void editMenuAuthorities(Integer menuId, AuthorityObject authorityObject) {
        List<Authority> to = AuthorityConvertUtil.to(authorityObject);
        //移除原有的
        remove(
                new LambdaQueryWrapper<SysMenuAuth>().eq(SysMenuAuth::getMenuId,menuId)
        );
        //批量插入新的
        if (!CollectionUtils.isEmpty(to)){
            saveBatch(
                    to.stream()
                            .map(a->
                                    SysMenuAuth.builder()
                                            .menuId(menuId)
                                            .authority(a.getAuthority())
                                            .description(a.getDescription())
                                            .build()
                            ).collect(Collectors.toList())
            );
        }
    }

}
