package com.kewen.framework.boot.authority.biz.service;

import java.util.List;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysUserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
public interface SysUserInfoService extends IService<SysUserInfo>{


    int updateBatch(List<SysUserInfo> list);

    int updateBatchSelective(List<SysUserInfo> list);

    int batchInsert(List<SysUserInfo> list);

    int insertOrUpdate(SysUserInfo record);

    int insertOrUpdateSelective(SysUserInfo record);

}
