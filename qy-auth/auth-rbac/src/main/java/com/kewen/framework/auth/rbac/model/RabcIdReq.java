package com.kewen.framework.auth.rbac.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RabcIdReq {
    @NotNull
    private Long id;
}
