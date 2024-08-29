package com.kewen.framework.extension.mp.controller;

import com.kewen.framework.extension.mp.service.SysRequestLogMpService;
import com.kewen.framework.extension.mp.entity.SysRequestLog;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.validation.annotation.Validated;

import com.kewen.framework.basic.model.PageResult;
import com.kewen.framework.basic.model.Result;
import com.kewen.framework.basic.model.IdReq;
import com.kewen.framework.basic.model.IdsReq;
import com.kewen.framework.basic.model.PageReq;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * <p>
 * 请求日志记录器 前端控制器
 * </p>
 *
 * @author kewen
 * @since 2023-04-26
 */
@RestController
@RequestMapping("/mp/sysRequestLog")
public class SysRequestLogMpController {

    @Autowired
    SysRequestLogMpService sysRequestLogService;




    @PostMapping("/add")
    public Result add(@RequestBody SysRequestLog entity){

        sysRequestLogService.save(entity);

        return Result.success();
    }

    @PostMapping("/updateById")
    public Result updateById(@RequestBody SysRequestLog entity){

        sysRequestLogService.updateById(entity);

        return Result.success();
    }

    @PostMapping(value = "/deleteById")
    public Result deleteById(@RequestBody @Validated IdReq req ) {

        sysRequestLogService.removeById(req.getId());

        return Result.success();
    }

    @PostMapping("/deleteBatchByIds")
    public Result deleteBatch(@Validated @RequestBody IdsReq req ) {
        sysRequestLogService.removeBatchByIds(req.getIds());
        return Result.success();
    }

    @GetMapping(value = "/getById")
    public Result findById(@NotNull @RequestParam("id") Long id) {

        SysRequestLog entity = sysRequestLogService.getById(id);
        return Result.success(entity);
    }

    @GetMapping(value = "/page")
    public Result<PageResult<SysRequestLog>> pageQuery(@Validated @ModelAttribute PageReq pageReq,@ModelAttribute SysRequestLog model) {
        LambdaQueryWrapper<SysRequestLog> wrapper = new LambdaQueryWrapper<>(model);
        /*todo return Result.success(PageUtils.page(
            sysRequestLogService.page(PageUtils.page(pageReq), wrapper)
        ));*/
        return null;
    }

    @GetMapping(value = "/list")
    public Result findList(@ModelAttribute SysRequestLog model) {

        LambdaQueryWrapper<SysRequestLog> wrapper = new LambdaQueryWrapper<>(model);
        List<SysRequestLog> list = sysRequestLogService.list(wrapper);
        return Result.success(list);
    }

}
