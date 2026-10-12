package com.cu.framework.auth.rbac.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * RBAC 通用多 ID 请求体，携带一组记录 ID 列表
 * @author kewen
 * @since 2026-02-12
 */
@Data
public class RbacIdsReq {
    /** 记录 ID 列表，必填 */
    @NotNull
    private List<Long> ids;
}
