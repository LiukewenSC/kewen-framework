package com.cu.framework.auth.rbac.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RbacIdReq {
    @NotNull
    private Long id;
}
