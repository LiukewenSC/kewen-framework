package com.cu.framework.sample.authcore.mapper;

import com.cu.framework.sample.authcore.entity.ExecutionLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 副作用证据表的读写。判定的"方法体没执行"就靠这里计数
 *
 * @author kewen
 * @since 2026-10-20
 */
public interface ExecutionLogMapper {

    /**
     * 记录一次方法体真正执行
     *
     * @param apiKey    接口标识
     * @param bizDataId 本次涉及的业务数据ID，多个用逗号连接
     * @param memo      备注
     * @return 插入条数
     */
    int insert(@Param("apiKey") String apiKey, @Param("bizDataId") String bizDataId, @Param("memo") String memo);

    /**
     * 按接口标识计数，用于断言拒绝时计数不增长
     *
     * @param apiKey 接口标识
     * @return 记录条数
     */
    int countByApiKey(@Param("apiKey") String apiKey);

    /**
     * 按接口标识取明细
     *
     * @param apiKey 接口标识
     * @return 记录列表
     */
    List<ExecutionLog> listByApiKey(@Param("apiKey") String apiKey);
}
