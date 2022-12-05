package com.kewen.framework.boot.authority.biz.service;

import com.kewen.framework.boot.authority.biz.entity.SysMenuAuth;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import java.util.List;


/**
 * <p>
 * 菜单权限表 服务类
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
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
}
