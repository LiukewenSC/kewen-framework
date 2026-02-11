package com.kewen.framework.basic.model;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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
