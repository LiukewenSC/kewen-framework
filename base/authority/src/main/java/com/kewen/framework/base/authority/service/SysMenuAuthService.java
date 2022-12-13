package com.kewen.framework.base.authority.service;

import com.kewen.framework.base.authority.entity.SysMenuAuth;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kewen.framework.base.authority.model.AuthorityObject;


import java.util.List;


/**
 * <p>
 * 菜单权限表 服务类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
public interface SysMenuAuthService extends IService<SysMenuAuth> {

    /**
     * 分页查询
     * @param pageNo 页数
     * @param pageSize 页面大小
     * @param queryModel 查询参数
     * @return Page<SysMenuAuth>
     */
    Page<SysMenuAuth> pageQuery(Integer pageNo, Integer pageSize, SysMenuAuth queryModel);

    /**
     * 列表查询
     * @param queryModel 查询参数
     * @return List<SysMenuAuth>
     */
    List<SysMenuAuth> findList(SysMenuAuth queryModel);

    /**
     * 编辑菜单权限，全量
     * @param menuId 菜单id
     * @param authorityObject 权限对象
     */
    void editMenuAuthorities(Long menuId, AuthorityObject authorityObject);
}
