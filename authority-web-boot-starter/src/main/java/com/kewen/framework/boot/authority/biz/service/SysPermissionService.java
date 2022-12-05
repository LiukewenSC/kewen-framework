package com.kewen.framework.boot.authority.biz.service;

import java.util.List;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysPermission;
import com.baomidou.mybatisplus.extension.service.IService;
public interface SysPermissionService extends IService<SysPermission>{


    int updateBatch(List<SysPermission> list);

    int updateBatchSelective(List<SysPermission> list);

    int batchInsert(List<SysPermission> list);

    int insertOrUpdate(SysPermission record);

    int insertOrUpdateSelective(SysPermission record);

}
