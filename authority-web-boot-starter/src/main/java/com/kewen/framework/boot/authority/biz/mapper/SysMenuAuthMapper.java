package com.kewen.framework.boot.authority.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysMenuAuth;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysMenuAuthMapper extends BaseMapper<SysMenuAuth> {
    int updateBatch(List<SysMenuAuth> list);

    int updateBatchSelective(List<SysMenuAuth> list);

    int batchInsert(@Param("list") List<SysMenuAuth> list);

    int insertOrUpdate(SysMenuAuth record);

    int insertOrUpdateSelective(SysMenuAuth record);
}