package com.kewen.framework.boot.authority.biz.service;

import java.util.List;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysGroup;
import com.baomidou.mybatisplus.extension.service.IService;
public interface SysGroupService extends IService<SysGroup>{


    int updateBatch(List<SysGroup> list);

    int updateBatchSelective(List<SysGroup> list);

    int batchInsert(List<SysGroup> list);

    int insertOrUpdate(SysGroup record);

    int insertOrUpdateSelective(SysGroup record);

}
