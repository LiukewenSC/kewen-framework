package com.cu.framework.basic.support.utils;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cu.framework.basic.model.PageReq;
import com.cu.framework.basic.model.PageResult;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页转换工具，在本框架的 {@link PageReq}/{@link PageResult} 与 MyBatis-Plus 的 {@link Page} 之间做适配，
 * 使业务层可以直接用 IService 完成分页查询而不必手写转换代码
 * @author kewen
 * @since 2024-10-22
 */
public class PageUtil {

    /**
     * 将框架的分页请求参数转换为 MyBatis-Plus 的分页对象
     * @param PageReq 框架分页请求，取其 page（页码，从 1 开始）与 size（每页条数）
     * @param <T> 分页记录的类型
     * @return 尚未执行查询的 MyBatis-Plus 分页对象
     */
    public static <T> Page<T> pageReq2BaomidouPage(PageReq PageReq) {
        return new Page<>(PageReq.getPage(), PageReq.getSize());
    }

    /**
     * 将MybatisPlus的分页返回转换为项目中的
     * @param page
     * @param <T>
     * @return
     */
    public static <T> PageResult<T> baomidouPage2PageResult(Page<T> page) {
        return PageResult.of((int) page.getCurrent(),(int)page.getSize(),(int)page.getTotal(),page.getRecords());
    }

    /**
     * 直接执行Service并且组装返回
     * @param req 请求分页
     * @param service 服务对象
     * @param <T> 数据类型
     * @return
     */
    public static <T> PageResult<T> page(PageReq req, IService<T> service) {
        return page(req, service, null);
    }

    /**
     * 直接执行Service并且组装返回 带条件
     * @param req 请求分页
     * @param service 服务对象
     * @param wrapper 查询条件
     * @param <T> 数据类型
     * @return
     */
    public static <T> PageResult<T> page(PageReq req, IService<T> service, Wrapper<T> wrapper) {
        Page<T> baomidoPage = pageReq2BaomidouPage(req);
        if (wrapper == null) {
            baomidoPage = service.page(baomidoPage);
        } else {
            baomidoPage = service.page(baomidoPage, wrapper);
        }
        return baomidouPage2PageResult(baomidoPage);
    }

    /**
     * 直接执行Service并且组装返回，带自定义条件和转换规则
     * @param req 请求分页
     * @param service 服务对象
     * @param wrapper 查询条件
     * @param tToResult 转换规则
     * @param <T> 源参数
     * @param <R> 返回参数
     * @return
     */
    public static <T, R> PageResult<R> page(PageReq req, IService<T> service, Wrapper<T> wrapper, Function<T, R> tToResult) {
        PageResult<T> pageResult = page(req, service, wrapper);
        List<T> listT = pageResult.getData();
        List<R> listR = listT.stream().map(tToResult).collect(Collectors.toList());
        return PageResult.of(pageResult.getPage(), pageResult.getSize(), pageResult.getTotal(), listR);
    }
}
