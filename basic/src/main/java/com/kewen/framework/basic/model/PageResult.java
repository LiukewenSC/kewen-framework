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

    public static <T> PageResult<T> empty(PageReq pageReq){
        return empty(pageReq.getPage(), pageReq.getSize());
    }
    public static <T> PageResult<T> empty(Integer page, Integer size){
        PageResult<T> result = new PageResult<>();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(0);
        return result;
    }
    public static <T> PageResult<T> convert(PageResult<Object> page, Class<T> tClass, Consumer<T> consumer){
        List<Object> records = page.getRecords();
        List<T> convert = BeanUtil.toList(records, tClass,consumer);
        return convert(page,convert);
    }
    public static <T> PageResult<T> convert(PageResult<Object> page, List<T> records){
        PageResult<T> result = new PageResult<>();
        result.setPage(page.getPage());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(records);
        return result;
    }
    public static <T> PageResult<T> of(PageReq pageReq, Integer total, List<T> records){
        return of(pageReq.getPage(),pageReq.getSize(),total,records);
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
