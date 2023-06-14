package com.kewen.framework.common.core.model;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class PageReq {
    /**
     * 页码
     */

    @NotNull
    @Min(value = 1L)
    protected Integer page;
    /**
     * 每页条数
     */
    @NotNull
    @Min(value = 1L)
    protected Integer size;

    /**
     * 从0开始
     * @return
     */
    public Integer getOffset(){
        return (page-1)*size;
    }
}
