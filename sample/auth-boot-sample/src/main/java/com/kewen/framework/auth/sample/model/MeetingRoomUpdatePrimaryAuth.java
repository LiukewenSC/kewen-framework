package com.kewen.framework.auth.sample.model;

import com.kewen.framework.auth.rabc.composite.model.SimpleAuthObject;
import com.kewen.framework.auth.sample.mp.entity.MeetingRoom;
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
