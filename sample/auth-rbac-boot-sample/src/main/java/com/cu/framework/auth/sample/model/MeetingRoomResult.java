package com.cu.framework.auth.sample.model;



import com.cu.framework.auth.core.entity.IAuthObject;
import com.cu.framework.auth.rbac.composite.model.SimpleAuthObject;
import com.cu.framework.basic.utils.BeanUtil;
import com.cu.framework.auth.sample.mp.entity.MeetingRoom;
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

    /** 会议室的数据权限对象，默认空集合，查询列表时由 AuthDataAdaptor.fillDataAuths 回填 */
    private IAuthObject authObject=new SimpleAuthObject();

    /**
     * 将会议室实体属性拷贝为结果对象，不做额外处理
     * @param meetingRoom 待转换的会议室实体
     * @return 转换后的会议室结果对象
     */
    public static MeetingRoomResult from(MeetingRoom meetingRoom) {
        return from(meetingRoom,null);
    }
    /**
     * 将会议室实体属性拷贝为结果对象，并可通过回调对结果做额外加工（如回填数据权限）
     * @param meetingRoom 待转换的会议室实体
     * @param consumer 转换完成后的回调处理器，可为 null
     * @return 转换后的会议室结果对象
     */
    public static MeetingRoomResult from(MeetingRoom meetingRoom, Consumer<MeetingRoomResult> consumer) {
        MeetingRoomResult result = BeanUtil.toBean(meetingRoom, MeetingRoomResult.class);
        if (consumer != null) {
            consumer.accept(result);
        }
        return result;
    }

}
