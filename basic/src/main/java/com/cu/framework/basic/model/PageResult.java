package com.cu.framework.basic.model;


import lombok.Data;

import java.util.List;


/**
 * @author kewen
 * @descrpition 分页返回
 * @since 2023-03-29
 */
@Data
public class PageResult<T> {


    /** 页码 */
    private Integer page;
    /** 页面大小 */
    private Integer size;
    /** 总记录数 */
    private Integer total;
    /** 当前页数据列表 */
    private List<T> data;

    /**
     * 静态工厂方法，构建分页返回结果
     * @param page 页码
     * @param size 页面大小
     * @param total 总记录数
     * @param data 当前页数据列表
     * @param <T> 数据类型
     * @return 组装完成的分页结果
     */
    public static <T> PageResult<T> of(Integer page, Integer size, Integer total, List<T> data){
        PageResult<T> result = new PageResult<>();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(total);
        result.setData(data);
        return result;
    }
}
