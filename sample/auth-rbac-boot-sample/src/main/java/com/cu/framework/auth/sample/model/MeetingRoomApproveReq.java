package com.cu.framework.auth.sample.model;

import com.cu.framework.auth.core.data.edit.IdDataEdit;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 预约会议室的请求体示例，实现 IdDataEdit 提供业务数据 ID，
 * 供 @AuthDataOperation 切面校验当前登录人对该会议室的预约权限
 * @author kewen
 * @since 2026-02-12
 */
@Data
public class MeetingRoomApproveReq implements IdDataEdit<Long> {

    /** 会议室 ID，必填 */
    @NotNull
    Long id;
    /** 预约时间段，示例入参，预约成功后原样返回 */
    String time;

    @Override
    public Long getDataId() {
        return id;
    }
}
