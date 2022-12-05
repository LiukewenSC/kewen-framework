package com.kewen.framework.boot.authority.biz.service;

import java.util.List;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysUserRole;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kewen.common.model.Role;

public interface SysUserRoleService extends IService<SysUserRole>{


    int updateBatch(List<SysUserRole> list);

    int updateBatchSelective(List<SysUserRole> list);

    int batchInsert(List<SysUserRole> list);

    int insertOrUpdate(SysUserRole record);

    int insertOrUpdateSelective(SysUserRole record);

    List<Role> listUserRole(Integer userId);
}
