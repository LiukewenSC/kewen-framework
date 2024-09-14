package com.kewen.framework.basic.model;

import com.kewen.framework.basic.utils.BeanUtil;
import lombok.Data;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author kewen
 * @descrpition 分页返回
 * @since 2023-03-29
 */
@Data
public class PageResult<T> {


    private Long page;
    private Long size;
    private Long total;
    private List<T> data;

    public static <T> PageResult<T> of(Long page, Long size, Long total, List<T> data){
        PageResult<T> result = new PageResult<>();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(total);
        result.setData(data);
        return result;
    }
}
