package com.cu.framework.auth.sample.model;


import com.cu.framework.auth.core.entity.IAuthObject;
import com.cu.framework.auth.rbac.composite.model.SimpleAuthObject;
import com.cu.framework.auth.sample.mp.entity.MeetingRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 创建会议室的请求体示例，在会议室基本信息之外携带要设置到该会议室上的数据权限对象
 * @author kewen
 * @since 2026-02-12
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MeetingRoomAddReq extends MeetingRoom {

    /** 会议室的数据权限对象，声明拥有该数据权限的用户/角色/部门等 */
    SimpleAuthObject authObject;

    /**
     * 获取业务数据 ID，即会议室主键
     * @return 会议室主键 id
     */
    public Long getDataId() {
        return getId();
    }

    public IAuthObject getAuthObject() {
        return authObject;
    }
}
