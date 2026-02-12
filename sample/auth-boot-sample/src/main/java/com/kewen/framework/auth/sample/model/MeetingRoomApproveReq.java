package com.kewen.framework.auth.sample.model;

import com.kewen.framework.auth.core.data.edit.IdDataEdit;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MeetingRoomApproveReq implements IdDataEdit<Long> {

    @NotNull
    Long id;
    String time;

    @Override
    public Long getDataId() {
        return id;
    }
}
