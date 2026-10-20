package com.cu.framework.sample.authcore.mapper;

import com.cu.framework.sample.authcore.entity.AuthDataRow;
import com.cu.framework.sample.authcore.entity.TableCount;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限数据的只读观察通道，供验证脚本直接看表内容。
 * <p>
 * 框架自身的读写走 JdbcTemplate，这里只是把同一张表读回来给人看，不承担判定职责
 *
 * @author kewen
 * @since 2026-10-20
 */
public interface AuthDataViewMapper {

    /**
     * 取某个槽位下配置的权限行
     *
     * @param bizModule 业务功能
     * @param action    操作类型
     * @param bizDataId 业务数据ID
     * @return 权限行
     */
    List<AuthDataRow> listSlot(@Param("bizModule") String bizModule,
                               @Param("action") String action,
                               @Param("bizDataId") Long bizDataId);

    /**
     * 取某个业务功能下的全部权限行
     *
     * @param bizModule 业务功能
     * @return 权限行
     */
    List<AuthDataRow> listByModule(@Param("bizModule") String bizModule);

    /**
     * 各表示例行数，用于断言"重复启动不产生重复行"
     *
     * @return 表名与行数
     */
    List<TableCount> tableCounts();
}
