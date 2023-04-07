package com.kewen.framework.base.authority.mp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kewen.framework.base.authority.mp.service.SysApplicationAuthMpService;
import com.kewen.framework.base.authority.mp.entity.SysApplicationAuth;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.validation.annotation.Validated;

import com.kewen.framework.base.common.model.Result;
import com.kewen.framework.base.common.model.PageResult;
import com.kewen.framework.datasource.utils.PageUtils;
import com.kewen.framework.base.common.model.IdReq;
import com.kewen.framework.base.common.model.IdsReq;
import com.kewen.framework.base.common.model.PageReq;

import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * <p>
 * 应用权限表 前端控制器
 * </p>
 *
 * @author kewen
 * @since 2023-04-07
 */
@RestController
@RequestMapping("/sysApplicationAuth")
public class SysApplicationAuthMpController {

    @Autowired
    SysApplicationAuthMpService sysApplicationAuthService;




    @PostMapping("/save")
    public Result save(@RequestBody SysApplicationAuth entity){

        sysApplicationAuthService.save(entity);

        return Result.success();
    }

    @PostMapping("/updateById")
    public Result updateById(@RequestBody SysApplicationAuth entity){

        sysApplicationAuthService.updateById(entity);

        return Result.success();
    }

    @PostMapping(value = "/deleteById")
    public Result deleteById(@RequestBody @Validated IdReq req ) {

        sysApplicationAuthService.removeById(req.getId());

        return Result.success();
    }

    @PostMapping("/deleteBatchByIds")
    public Result deleteBatch(@Validated @RequestBody IdsReq req ) {
        sysApplicationAuthService.removeBatchByIds(req.getIds());
        return Result.success();
    }

    @GetMapping(value = "/getById")
    public Result findById(@NotNull @RequestParam("id") Long id) {

        SysApplicationAuth entity = sysApplicationAuthService.getById(id);
        return Result.success(entity);
    }

    @GetMapping(value = "/page")
    public Result<PageResult<SysApplicationAuth>> pageQuery(@Validated @ModelAttribute PageReq pageReq,@ModelAttribute SysApplicationAuth model) {
        LambdaQueryWrapper<SysApplicationAuth> wrapper = new LambdaQueryWrapper<>(model);
        return Result.success(PageUtils.page(
            sysApplicationAuthService.page(PageUtils.page(pageReq), wrapper)
        ));
    }

    @GetMapping(value = "/list")
    public Result findList(@ModelAttribute SysApplicationAuth model) {

        LambdaQueryWrapper<SysApplicationAuth> wrapper = new LambdaQueryWrapper<>(model);
        List<SysApplicationAuth> list = sysApplicationAuthService.list(wrapper);
        return Result.success(list);
    }

}
