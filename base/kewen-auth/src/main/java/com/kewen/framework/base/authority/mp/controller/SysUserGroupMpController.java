package com.kewen.framework.base.authority.mp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kewen.framework.base.authority.mp.service.SysUserGroupMpService;
import com.kewen.framework.base.authority.mp.entity.SysUserGroup;
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
 * 用户角色组关联表 前端控制器
 * </p>
 *
 * @author kewen
 * @since 2023-04-07
 */
@RestController
@RequestMapping("/sysUserGroup")
public class SysUserGroupMpController {

    @Autowired
    SysUserGroupMpService sysUserGroupService;




    @PostMapping("/save")
    public Result save(@RequestBody SysUserGroup entity){

        sysUserGroupService.save(entity);

        return Result.success();
    }

    @PostMapping("/updateById")
    public Result updateById(@RequestBody SysUserGroup entity){

        sysUserGroupService.updateById(entity);

        return Result.success();
    }

    @PostMapping(value = "/deleteById")
    public Result deleteById(@RequestBody @Validated IdReq req ) {

        sysUserGroupService.removeById(req.getId());

        return Result.success();
    }

    @PostMapping("/deleteBatchByIds")
    public Result deleteBatch(@Validated @RequestBody IdsReq req ) {
        sysUserGroupService.removeBatchByIds(req.getIds());
        return Result.success();
    }

    @GetMapping(value = "/getById")
    public Result findById(@NotNull @RequestParam("id") Long id) {

        SysUserGroup entity = sysUserGroupService.getById(id);
        return Result.success(entity);
    }

    @GetMapping(value = "/page")
    public Result<PageResult<SysUserGroup>> pageQuery(@Validated @ModelAttribute PageReq pageReq,@ModelAttribute SysUserGroup model) {
        LambdaQueryWrapper<SysUserGroup> wrapper = new LambdaQueryWrapper<>(model);
        return Result.success(PageUtils.page(
            sysUserGroupService.page(PageUtils.page(pageReq), wrapper)
        ));
    }

    @GetMapping(value = "/list")
    public Result findList(@ModelAttribute SysUserGroup model) {

        LambdaQueryWrapper<SysUserGroup> wrapper = new LambdaQueryWrapper<>(model);
        List<SysUserGroup> list = sysUserGroupService.list(wrapper);
        return Result.success(list);
    }

}
