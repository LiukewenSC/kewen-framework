package com.kewen.framework.sample.basic.controller;

import com.kewen.framework.basic.model.Result;
import com.kewen.framework.basic.utils.MapUtil;
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


    @GetMapping("/paramSerializer")
    public Result paramSerializer(@RequestParam(required = false) LocalDate localDate, @RequestParam(required = false) LocalDateTime localDateTime){
        return Result.success();
    }
    @GetMapping("/paramSerializer2")
    public Result paramSerializer2(LocalDateParam req){
        return Result.success();
    }

    @GetMapping("/paramSerializer3")
    public Result paramSerializer3(@RequestBody LocalDateParam req){
        return Result.success();
    }
    @GetMapping("/deSerializerDate")
    public Result deSerializerDate(){
        Map<Object, Object> build = MapUtil.builder()
                .put("date", new Date())
                .put("localDateTime", LocalDateTime.now())
                .put("localDate", LocalDate.now())
                .build();
        return Result.success(build);
    }



}
