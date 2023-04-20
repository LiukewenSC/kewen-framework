package com.kewen.framework.auth.sys.mp.controller;

import com.kewen.framework.auth.sys.mp.service.SysGroupMpService;
import com.kewen.framework.auth.sys.mp.entity.SysGroup;
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

import com.kewen.framework.jdbc.core.utils.PageUtils;
import com.kewen.framework.common.core.model.PageResult;
import com.kewen.framework.common.core.model.Result;
import com.kewen.framework.common.core.model.IdReq;
import com.kewen.framework.common.core.model.IdsReq;
import com.kewen.framework.common.core.model.PageReq;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * <p>
 * 角色组表 前端控制器
 * </p>
 *
 * @author kewen
 * @since 2023-04-20
 */
@RestController
@RequestMapping("/mp/sysGroup")
public class SysGroupMpController {

    @Autowired
    SysGroupMpService sysGroupService;




    @PostMapping("/add")
    public Result add(@RequestBody SysGroup entity){

        sysGroupService.save(entity);

        return Result.success();
    }

    @PostMapping("/updateById")
    public Result updateById(@RequestBody SysGroup entity){

        sysGroupService.updateById(entity);

        return Result.success();
    }

    @PostMapping(value = "/deleteById")
    public Result deleteById(@RequestBody @Validated IdReq req ) {

        sysGroupService.removeById(req.getId());

        return Result.success();
    }

    @PostMapping("/deleteBatchByIds")
    public Result deleteBatch(@Validated @RequestBody IdsReq req ) {
        sysGroupService.removeBatchByIds(req.getIds());
        return Result.success();
    }

    @GetMapping(value = "/getById")
    public Result findById(@NotNull @RequestParam("id") Long id) {

        SysGroup entity = sysGroupService.getById(id);
        return Result.success(entity);
    }

    @GetMapping(value = "/page")
    public Result<PageResult<SysGroup>> pageQuery(@Validated @ModelAttribute PageReq pageReq,@ModelAttribute SysGroup model) {
        LambdaQueryWrapper<SysGroup> wrapper = new LambdaQueryWrapper<>(model);
        return Result.success(PageUtils.page(
            sysGroupService.page(PageUtils.page(pageReq), wrapper)
        ));
    }

    @GetMapping(value = "/list")
    public Result findList(@ModelAttribute SysGroup model) {

        LambdaQueryWrapper<SysGroup> wrapper = new LambdaQueryWrapper<>(model);
        List<SysGroup> list = sysGroupService.list(wrapper);
        return Result.success(list);
    }

}
