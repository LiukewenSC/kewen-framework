package com.kewen.framework.boot.authority.biz.service;

import com.kewen.framework.boot.authority.biz.mapper.entity.SysUser;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
public interface SysUserService extends IService<SysUser>{


    int updateBatch(List<SysUser> list);

    int updateBatchSelective(List<SysUser> list);

    int batchInsert(List<SysUser> list);

    int insertOrUpdate(SysUser record);

    int insertOrUpdateSelective(SysUser record);

}
