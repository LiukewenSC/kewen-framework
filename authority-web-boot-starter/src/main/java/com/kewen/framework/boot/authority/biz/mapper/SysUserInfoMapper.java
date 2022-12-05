package com.kewen.framework.boot.authority.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysUserInfo;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysUserInfoMapper extends BaseMapper<SysUserInfo> {
    int updateBatch(List<SysUserInfo> list);

    int updateBatchSelective(List<SysUserInfo> list);

    int batchInsert(@Param("list") List<SysUserInfo> list);

    int insertOrUpdate(SysUserInfo record);

    int insertOrUpdateSelective(SysUserInfo record);
}