package com.kewen.framework.base.common.model;

import lombok.Data;

@Data
public class PageReq {
    /**
     * 页码
     */
    protected Integer page;
    /**
     * 每页条数
     */
    protected Integer size;
}
