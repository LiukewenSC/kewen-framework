package com.kewen.framework.boot.authority.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysApplicationAuth;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysApplicationAuthMapper extends BaseMapper<SysApplicationAuth> {
    int updateBatch(List<SysApplicationAuth> list);

    int updateBatchSelective(List<SysApplicationAuth> list);

    int batchInsert(@Param("list") List<SysApplicationAuth> list);

    int insertOrUpdate(SysApplicationAuth record);

    int insertOrUpdateSelective(SysApplicationAuth record);
}