package com.kewen.framework.base.authority.mp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kewen.framework.base.authority.mp.service.SysUserMpService;
import com.kewen.framework.base.authority.mp.entity.SysUser;
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
 * 用户表 前端控制器
 * </p>
 *
 * @author kewen
 * @since 2023-04-07
 */
@RestController
@RequestMapping("/sysUser")
public class SysUserMpController {

    @Autowired
    SysUserMpService sysUserService;




    @PostMapping("/save")
    public Result save(@RequestBody SysUser entity){

        sysUserService.save(entity);

        return Result.success();
    }

    @PostMapping("/updateById")
    public Result updateById(@RequestBody SysUser entity){

        sysUserService.updateById(entity);

        return Result.success();
    }

    @PostMapping(value = "/deleteById")
    public Result deleteById(@RequestBody @Validated IdReq req ) {

        sysUserService.removeById(req.getId());

        return Result.success();
    }

    @PostMapping("/deleteBatchByIds")
    public Result deleteBatch(@Validated @RequestBody IdsReq req ) {
        sysUserService.removeBatchByIds(req.getIds());
        return Result.success();
    }

    @GetMapping(value = "/getById")
    public Result findById(@NotNull @RequestParam("id") Long id) {

        SysUser entity = sysUserService.getById(id);
        return Result.success(entity);
    }

    @GetMapping(value = "/page")
    public Result<PageResult<SysUser>> pageQuery(@Validated @ModelAttribute PageReq pageReq,@ModelAttribute SysUser model) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>(model);
        return Result.success(PageUtils.page(
            sysUserService.page(PageUtils.page(pageReq), wrapper)
        ));
    }

    @GetMapping(value = "/list")
    public Result findList(@ModelAttribute SysUser model) {

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>(model);
        List<SysUser> list = sysUserService.list(wrapper);
        return Result.success(list);
    }

}
