package com.cu.framework.auth.sample.model;


import com.cu.framework.auth.core.data.edit.DataIdCarrier;
import com.cu.framework.auth.core.entity.IAuthObject;
import com.cu.framework.auth.rbac.composite.model.SimpleAuthObject;
import com.cu.framework.auth.sample.mp.entity.MeetingRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.Collections;

/**
 * 编辑会议室的请求体示例，实现 DataIdCarrier 交出业务数据 ID，
 * 供 @AuthDataOperation 切面校验编辑权限，并在编辑时通过 AuthDataAdaptor 更新预约权限
 * @author kewen
 * @since 2026-02-12
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MeetingRoomUpdateReq extends MeetingRoom implements DataIdCarrier<Long> {

    /**
     * 会议室预约权限
     */
    SimpleAuthObject authObject;

    @Override
    public Collection<Long> getDataIds() {
        return Collections.singletonList(getId());
    }


    public IAuthObject getAuthObject() {
        return authObject;
    }
}
