package com.kewen.framework.boot.authority.biz.service;

import com.kewen.framework.boot.authority.biz.mapper.entity.SysPosition;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
public interface SysPositionService extends IService<SysPosition>{


    int updateBatch(List<SysPosition> list);

    int updateBatchSelective(List<SysPosition> list);

    int batchInsert(List<SysPosition> list);

    int insertOrUpdate(SysPosition record);

    int insertOrUpdateSelective(SysPosition record);

}
