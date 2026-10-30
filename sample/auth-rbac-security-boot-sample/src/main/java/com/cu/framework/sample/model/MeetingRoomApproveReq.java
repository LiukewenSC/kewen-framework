package com.cu.framework.sample.model;

import com.cu.framework.auth.core.data.edit.DataIdCarrier;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;

/**
 * 预约会议室的请求体示例，实现 DataIdCarrier 交出业务数据 ID，
 * 供 @AuthDataOperation 切面校验当前登录人对该会议室的预约权限
 * @author kewen
 * @since 2026-02-12
 */
@Data
public class MeetingRoomApproveReq implements DataIdCarrier<Long> {

    /** 会议室 ID，必填 */
    @NotNull
    Long id;
    /** 预约时间段，示例入参，预约成功后原样返回 */
    String time;

    @Override
    public Collection<Long> getDataIds() {
        return Collections.singletonList(id);
    }
}
