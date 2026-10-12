package com.cu.framework.auth.rbac.model;

import lombok.Data;

import java.util.List;

/**
 * RBAC 通用分页返回结果
 * @author kewen
 * @since 2026-02-12
 */
@Data
public class RbacPageResult<T> {

    /** 页码 */
    private Long page;
    /** 页面大小 */
    private Long size;
    /** 总条数 */
    private Long total;
    /** 当前页数据列表 */
    private List<T> data;

    /**
     * 静态工厂方法，组装分页返回结果
     * @param page 页码
     * @param size 页面大小
     * @param total 总条数
     * @param data 当前页数据列表
     * @param <T> 数据类型
     * @return 分页返回结果
     */
    public static <T> RbacPageResult<T> of(Long page, Long size, Long total, List<T> data){
        RbacPageResult<T> result = new RbacPageResult<>();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(total);
        result.setData(data);
        return result;
    }
}
