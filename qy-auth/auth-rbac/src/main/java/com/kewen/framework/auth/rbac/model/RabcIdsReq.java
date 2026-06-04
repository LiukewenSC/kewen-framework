package com.kewen.framework.auth.rbac.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class RabcIdsReq {
    @NotNull
    private List<Long> ids;
}
