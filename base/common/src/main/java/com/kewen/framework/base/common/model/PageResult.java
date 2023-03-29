package com.kewen.framework.base.common.model;

import lombok.Data;

/**
 * @author kewen
 * @descrpition 分页返回
 * @since 2023-03-29
 */
@Data
public class PageResult {
    /**
     * 页码
     */
    protected Integer page;
    /**
     * 每页条数
     */
    protected Integer size;

    protected Integer total;
}
