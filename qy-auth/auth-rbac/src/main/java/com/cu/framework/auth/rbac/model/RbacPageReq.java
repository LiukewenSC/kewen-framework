package com.cu.framework.auth.rbac.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * RBAC 通用分页请求参数，携带页码、页面大小与模糊搜索关键字
 * @author kewen
 * @since 2026-02-12
 */
@Data
public class RbacPageReq {

    /**
     * 页码，从1开始
     */
    @NotNull
    private Long page;

    /**
     * 页面大小
     */
    @NotNull
    private Long size;

    /**
     * 模糊搜索关键字
     */
    private String search;
}
