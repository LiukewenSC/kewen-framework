package com.kewen.framework.boot.authority.biz.service;

import com.kewen.framework.boot.authority.biz.mapper.entity.SysMenuAuth;
import com.baomidou.mybatisplus.extension.service.IService;import java.util.List;

public interface SysMenuAuthService extends IService<SysMenuAuth> {


    int updateBatch(List<SysMenuAuth> list);

    int updateBatchSelective(List<SysMenuAuth> list);

    int batchInsert(List<SysMenuAuth> list);

    int insertOrUpdate(SysMenuAuth record);

    int insertOrUpdateSelective(SysMenuAuth record);
}

