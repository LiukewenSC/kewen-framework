package com.cu.framework.sample.authcore.entity;

import lombok.Data;

/**
 * 副作用证据表 demo_execution_log 的行。
 * <p>
 * 受管辖的写方法第一行往这里插一条；判定拒绝发生在方法体之前，
 * 所以"没有新增行"就是"方法体没执行"的直接证据，比只看响应码可靠
 *
 * @author kewen
 * @since 2026-10-20
 */
@Data
public class ExecutionLog {

    /** 主键 */
    private Long pk;
    /** 被调用的接口标识 */
    private String apiKey;
    /** 本次涉及的业务数据ID，仅作记录 */
    private String bizDataId;
    /** 备注 */
    private String memo;
    /** 记录时间 */
    private String createTime;
}
