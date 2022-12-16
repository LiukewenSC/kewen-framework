package com.kewen.framework.base.authority.service;

import com.kewen.framework.base.authority.entity.SysApplicationAuth;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kewen.framework.base.authority.model.AuthorityObject;


import java.util.Collection;
import java.util.List;


/**
 * <p>
 * 应用权限表 服务类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
public interface SysApplicationAuthService extends IService<SysApplicationAuth> {

    boolean hasAuth(Collection<String> auths, String module, String operate, Long businessId);

    /**
     * 分页查询
     *
     * @param pageNo     页数
     * @param pageSize   页面大小
     * @param queryModel 查询参数
     * @return Page<SysApplicationAuth>
     */
    Page<SysApplicationAuth> pageQuery(Integer pageNo, Integer pageSize, SysApplicationAuth queryModel);

    /**
     * 列表查询
     *
     * @param queryModel 查询参数
     * @return List<SysApplicationAuth>
     */
    List<SysApplicationAuth> findList(SysApplicationAuth queryModel);

    void editBusinessAuthority(Integer businessId, String module, String operate, AuthorityObject authority);
}
