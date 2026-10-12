package com.cu.framework.auth.rbac.model;

import lombok.Data;

import java.util.List;

@Data
public class RbacPageResult<T> {

    private Long page;
    private Long size;
    private Long total;
    private List<T> data;

    public static <T> RbacPageResult<T> of(Long page, Long size, Long total, List<T> data){
        RbacPageResult<T> result = new RbacPageResult<>();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(total);
        result.setData(data);
        return result;
    }
}
