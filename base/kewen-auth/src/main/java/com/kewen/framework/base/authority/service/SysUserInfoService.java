package com.kewen.framework.base.authority.service;

import com.kewen.framework.base.authority.entity.SysUserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import java.util.List;


/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
public interface SysUserInfoService extends IService<SysUserInfo> {

        /**
         * 分页查询
         * @param pageNo 页数
         * @param pageSize 页面大小
         * @param queryModel 查询参数
         * @return Page<SysUserInfo>
         */
        Page<SysUserInfo> pageQuery(Integer pageNo, Integer pageSize, SysUserInfo queryModel);

        /**
         * 列表查询
         * @param queryModel 查询参数
         * @return List<SysUserInfo>
         */
        List<SysUserInfo> findList(SysUserInfo queryModel);
}
