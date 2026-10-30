package com.cu.framework.basic.support.utils;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import com.cu.framework.basic.model.PageReq;
import com.cu.framework.basic.model.PageResult;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * RBAC 分页转换工具，负责 PageReq、MyBatis-Plus Page 与 PageResult 之间的转换，并提供直接执行分页查询的封装
 * @author kewen
 * @since 2026-02-12
 */
public class MybatisPageExecutor {

    /**
     * 将 PageReq 转换为 MyBatis-Plus 的分页对象
     * @param PageReq 分页请求参数
     * @param <T> 数据类型
     * @return Page 分页对象
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
        return baomidouPage2PageResult(page, page.getRecords());
    }

    /**
     * 将MybatisPlus的分页 page , size 转换为项目中的，并且转换为 入参类型 T
     * @param page
     * @param data
     * @param <T>
     * @return
     */
    public static <T> PageResult<T> baomidouPage2PageResult(Page page, List<T> data) {
        return PageResult.of((int)page.getCurrent(),(int)page.getSize(),(int)page.getTotal(),data);
    }

    /**
     * 直接执行Service并且组装返回
     * @param req 请求分页
     * @param service 服务对象
     * @param <T> 数据类型
     * @return
     */
    public static <T> PageResult<T> pageAndConvert(PageReq req, IService<T> service) {
        return pageAndConvert(req, service, new Query<T>());
    }

    /**
     * 直接执行Service并且组装返回 带条件
     * @param req 请求分页
     * @param service 服务对象
     * @param query 查询条件
     * @param <T> 数据类型
     * @return
     */
    public static <T> PageResult<T> pageAndConvert(PageReq req, IService<T> service, Query<T> query) {
        Page<T> baomidoPage = new Page<>(req.getPage(), req.getSize());

        Consumer<Page<T>> consumer = query == null ? null : query.getPageConsumer();
        Wrapper<T> wrapper = query == null ? null : query.getWrapper();
        if (consumer != null) {
            consumer.accept(baomidoPage);
        }
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
     * @param query 查询条件
     * @param function 转换规则
     * @param <T> 源参数
     * @param <R> 返回参数
     * @return
     */
    public static <T, R> PageResult<R> pageAndConvert(PageReq req, IService<T> service, Query<T> query, Function<List<T>, List<R>> function) {
        PageResult<T> PageResult = pageAndConvert(req, service, query);
        List<T> listT = PageResult.getData();
        List<R> listR = function.apply(listT);
        return PageResult.of(PageResult.getPage(), PageResult.getSize(), PageResult.getTotal(), listR);
    }

    /**
     * 分页查询条件载体，配合 pageAndConvert 使用
     * @author kewen
     * @since 2026-02-12
     */
    @Data
    @Accessors(chain = true)
    public static class Query<T> {
        /** 分页前对 Page 对象的自定义处理回调，可为 null */
        Consumer<Page<T>> pageConsumer;
        /** MyBatis-Plus 查询条件，为 null 时无条件分页查询 */
        Wrapper<T> wrapper;
    }
}
