package com.kewen.framework.datasource.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kewen.framework.base.common.model.PageReq;
import com.kewen.framework.base.common.model.PageResult;
import com.kewen.framework.base.common.utils.BeanUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kewen
 * @descrpition
 * @since 2023-03-30
 */
public class PageUtils {

    /**
     * 转换成分页返回对象
     * @param iPage
     * @param <T>
     * @return
     */
    public static <T> PageResult<T> page(IPage<T> iPage){
        long page = iPage.getPages();
        long size = iPage.getSize();
        long total = iPage.getTotal();
        return PageResult.of((int)page,(int)size,(int)total,iPage.getRecords());
    }
    public static <T> PageResult<T> page(IPage iPage,Class<T> tClass){
        long page = iPage.getPages();
        long size = iPage.getSize();
        long total = iPage.getTotal();
        List<T> list = new ArrayList<>();
        for (Object record : iPage.getRecords()) {
            list.add(BeanUtil.convert(record, tClass));
        }
        return PageResult.of((int)page,(int)size,(int)total,list);
    }

    /**
     * 转换成mybatis-plus对象
     * @param pageReq
     * @param <T>
     * @return
     */
    public static <T> Page<T> page(PageReq pageReq){
        Page<T> page = new Page<>(pageReq.getPage(),pageReq.getSize());
        return page;
    }
}
