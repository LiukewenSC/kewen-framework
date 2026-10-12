package com.cu.framework.auth.sample.model;

import com.cu.framework.auth.rbac.composite.model.SimpleAuthObject;
import com.cu.framework.auth.sample.mp.entity.MeetingRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author kewen
 * @since 2024-08-13
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MeetingRoomUpdatePrimaryAuth extends MeetingRoom {
    private SimpleAuthObject authObject;
}
