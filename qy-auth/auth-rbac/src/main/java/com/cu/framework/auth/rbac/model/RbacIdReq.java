package com.cu.framework.auth.rbac.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * RBAC 通用单 ID 请求体，用于按 ID 操作（如删除）的接口入参
 * @author kewen
 * @since 2026-02-12
 */
@Data
public class RbacIdReq {
    /** 记录 ID，必填 */
    @NotNull
    private Long id;
}
