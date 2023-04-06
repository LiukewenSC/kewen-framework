package com.kewen.framework.base.common.model;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class PageReq {
    /**
     * 页码
     */

    @NotNull
    protected Integer page;
    /**
     * 每页条数
     */
    @NotNull
    protected Integer size;
}
