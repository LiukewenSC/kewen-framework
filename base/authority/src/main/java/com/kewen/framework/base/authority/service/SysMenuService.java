package com.kewen.framework.base.authority.service;

import com.kewen.framework.base.authority.entity.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import java.util.Collection;
import java.util.List;


/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
public interface SysMenuService extends IService<SysMenu> {

    boolean hasAuth(Collection<String> userAuthorities, String url);

    /**
     * 分页查询
     *
     * @param pageNo     页数
     * @param pageSize   页面大小
     * @param queryModel 查询参数
     * @return Page<SysMenu>
     */
    Page<SysMenu> pageQuery(Integer pageNo, Integer pageSize, SysMenu queryModel);

    /**
     * 列表查询
     *
     * @param queryModel 查询参数
     * @return List<SysMenu>
     */
    List<SysMenu> findList(SysMenu queryModel);

}
