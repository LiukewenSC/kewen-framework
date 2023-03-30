package com.kewen.framework.base.common.model;

import lombok.Data;

import java.util.List;

/**
 * @author kewen
 * @descrpition 分页返回
 * @since 2023-03-29
 */
@Data
public class PageResult<T> {
    /**
     * 页码
     */
    protected Integer page;
    /**
     * 每页条数
     */
    protected Integer size;

    protected Integer total;

    protected List<T> records;

    private PageResult() {
    }

    public static <T> PageResult<T> of(Integer page, Integer size, Integer total, List<T> records){
        PageResult<T> result = new PageResult<>();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(total);
        result.setRecords(records);
        return result;
    }
}
