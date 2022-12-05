package com.kewen.framework.boot.authority.biz.service;

import java.util.List;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysUserGroup;
import com.baomidou.mybatisplus.extension.service.IService;
public interface SysUserGroupService extends IService<SysUserGroup>{


    int updateBatch(List<SysUserGroup> list);

    int updateBatchSelective(List<SysUserGroup> list);

    int batchInsert(List<SysUserGroup> list);

    int insertOrUpdate(SysUserGroup record);

    int insertOrUpdateSelective(SysUserGroup record);

}
