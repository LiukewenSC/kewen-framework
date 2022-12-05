package com.kewen.framework.boot.authority.biz.service;

import com.kewen.framework.boot.authority.biz.mapper.entity.SysApplicationAuth;

import java.util.Collection;import java.util.List;

/**
 * @author kewen
 * @descrpition 业务权限
 * @since 2022-11-25 9:39
 */
public interface SysApplicationAuthService {
    boolean hasAuth(Collection<String> auths, String module, String operate, Integer businessId);

    int updateBatch(List<SysApplicationAuth> list);

    int updateBatchSelective(List<SysApplicationAuth> list);

    int batchInsert(List<SysApplicationAuth> list);

    int insertOrUpdate(SysApplicationAuth record);

    int insertOrUpdateSelective(SysApplicationAuth record);
}

