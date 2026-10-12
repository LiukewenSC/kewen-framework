package com.cu.framework.auth.sample.model;


import com.cu.framework.auth.core.entity.IAuthObject;
import com.cu.framework.auth.rbac.composite.model.SimpleAuthObject;
import com.cu.framework.auth.sample.mp.entity.MeetingRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MeetingRoomAddReq extends MeetingRoom {

    SimpleAuthObject authObject;

    public Long getDataId() {
        return getId();
    }

    public IAuthObject getAuthObject() {
        return authObject;
    }
}
