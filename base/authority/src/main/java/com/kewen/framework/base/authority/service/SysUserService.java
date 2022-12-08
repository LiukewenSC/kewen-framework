package com.kewen.framework.base.authority.service;

import com.kewen.framework.base.authority.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import java.util.List;


/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
public interface SysUserService extends IService<SysUser> {

        /**
         * 分页查询
         * @param pageNo 页数
         * @param pageSize 页面大小
         * @param queryModel 查询参数
         * @return Page<SysUser>
         */
        Page<SysUser> pageQuery(Integer pageNo, Integer pageSize, SysUser queryModel);

        /**
         * 列表查询
         * @param queryModel 查询参数
         * @return List<SysUser>
         */
        List<SysUser> findList(SysUser queryModel);
}
