package com.kewen.framework.auth.rbac.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RabcPageReq {

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
