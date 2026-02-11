package com.kewen.framework.auth.sample.model;


import com.kewen.framework.auth.core.entity.IAuthObject;
import com.kewen.framework.auth.rabc.composite.model.SimpleAuthObject;
import com.kewen.framework.auth.sample.mp.entity.MeetingRoom;
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
