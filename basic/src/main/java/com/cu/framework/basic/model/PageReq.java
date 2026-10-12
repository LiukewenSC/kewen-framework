package com.cu.framework.basic.model;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 分页请求参数，包含页码、页面大小与模糊搜索关键字，并提供从 0 开始的偏移量计算
 * @author kewen
 * @since 2024-08-29
 */
@Data
public class PageReq {
    /**
     * 页码，从1开始
     */
    @NotNull
    private Integer page;

    /**
     * 页面大小
     */
    @NotNull
    private Integer size;

    /**
     * 模糊搜索关键字
     */
    protected String search;

    /**
     * 从0开始
     * @return
     */
    public Integer getOffset(){
        return (page-1)*size;
    }
}
