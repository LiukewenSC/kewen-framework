package com.cu.framework.sample.basic.controller;

import com.cu.framework.basic.model.Result;
import com.cu.framework.basic.utils.MapUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

/**
 * 
 * @author kewen
 * @since 2023-04-27
 */
@RestController
@RequestMapping("/jackson")
public class JacksonSerializerController {


    /**
     * 测试 @RequestParam 形式的 LocalDate / LocalDateTime 参数反序列化，两个参数均非必传
     * @param localDate 日期参数，格式由全局转换配置决定
     * @param localDateTime 日期时间参数，格式由全局转换配置决定
     * @return 成功响应
     */
    @GetMapping("/paramSerializer")
    public Result paramSerializer(@RequestParam(required = false) LocalDate localDate, @RequestParam(required = false) LocalDateTime localDateTime){
        return Result.success(MapUtil.builder().put("localDate", localDate).put("localDateTime", localDateTime).build());
    }
    /**
     * 测试以对象属性绑定方式（不加 @RequestBody）反序列化 LocalDateParam 中的日期字段
     * @param req 含 LocalDate / LocalDateTime 字段的参数对象
     * @return 成功响应
     */
    @GetMapping("/paramSerializer2")
    public Result paramSerializer2(LocalDateParam req){
        return Result.success(req);
    }

    /**
     * 测试 @RequestBody JSON 请求体中日期字段的反序列化
     * @param req 含 LocalDate / LocalDateTime 字段的参数对象
     * @return 成功响应
     */
    @GetMapping("/paramSerializer3")
    public Result paramSerializer3(@RequestBody LocalDateParam req){
        return Result.success(req);
    }
    /**
     * 返回包含 Date / LocalDateTime / LocalDate 三种日期类型的 Map，用于观察 JSON 序列化输出格式
     * @return 含各类型当前时间的 Map
     */
    @GetMapping("/deSerializerDate")
    public Result<Map<Object, Object>> deSerializerDate(){
        Map<Object, Object> build = MapUtil.builder()
                .put("date", new Date())
                .put("localDateTime", LocalDateTime.now())
                .put("localDate", LocalDate.now())
                .build();
        return Result.success(build);
    }



}
