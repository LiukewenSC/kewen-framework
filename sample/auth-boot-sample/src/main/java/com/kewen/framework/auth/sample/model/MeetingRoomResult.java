package com.kewen.framework.auth.sample.model;



import com.kewen.framework.auth.core.entity.IAuthObject;
import com.kewen.framework.auth.rabc.composite.model.SimpleAuthObject;
import com.kewen.framework.auth.rabc.utils.BeanUtil;
import com.kewen.framework.auth.sample.mp.entity.MeetingRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.function.Consumer;

/**
 * 
 * @author kewen
 * @since 2024-08-19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MeetingRoomResult  extends MeetingRoom {

    private IAuthObject authObject=new SimpleAuthObject();

    public static MeetingRoomResult from(MeetingRoom meetingRoom) {
        return from(meetingRoom,null);
    }
    public static MeetingRoomResult from(MeetingRoom meetingRoom, Consumer<MeetingRoomResult> consumer) {
        MeetingRoomResult result = BeanUtil.toBean(meetingRoom, MeetingRoomResult.class);
        if (consumer != null) {
            consumer.accept(result);
        }
        return result;
    }

}
