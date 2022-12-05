package com.kewen.framework.boot.authority.biz.service;

import java.util.List;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysUserDept;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kewen.common.model.UserDept;

public interface SysUserDeptService extends IService<SysUserDept>{


    /**
     * 查询用户的部门列表
     * @param userId
     * @return
     */
    UserDept getUserDept(Integer userId);

    int updateBatch(List<SysUserDept> list);

    int updateBatchSelective(List<SysUserDept> list);

    int batchInsert(List<SysUserDept> list);

    int insertOrUpdate(SysUserDept record);

    int insertOrUpdateSelective(SysUserDept record);

}
