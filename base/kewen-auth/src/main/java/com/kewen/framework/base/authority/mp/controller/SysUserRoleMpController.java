package com.kewen.framework.base.authority.mp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kewen.framework.base.authority.mp.service.SysUserRoleMpService;
import com.kewen.framework.base.authority.mp.entity.SysUserRole;
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
 *  前端控制器
 * </p>
 *
 * @author kewen
 * @since 2023-04-07
 */
@RestController
@RequestMapping("/sysUserRole")
public class SysUserRoleMpController {

    @Autowired
    SysUserRoleMpService sysUserRoleService;




    @PostMapping("/save")
    public Result save(@RequestBody SysUserRole entity){

        sysUserRoleService.save(entity);

        return Result.success();
    }

    @PostMapping("/updateById")
    public Result updateById(@RequestBody SysUserRole entity){

        sysUserRoleService.updateById(entity);

        return Result.success();
    }

    @PostMapping(value = "/deleteById")
    public Result deleteById(@RequestBody @Validated IdReq req ) {

        sysUserRoleService.removeById(req.getId());

        return Result.success();
    }

    @PostMapping("/deleteBatchByIds")
    public Result deleteBatch(@Validated @RequestBody IdsReq req ) {
        sysUserRoleService.removeBatchByIds(req.getIds());
        return Result.success();
    }

    @GetMapping(value = "/getById")
    public Result findById(@NotNull @RequestParam("id") Long id) {

        SysUserRole entity = sysUserRoleService.getById(id);
        return Result.success(entity);
    }

    @GetMapping(value = "/page")
    public Result<PageResult<SysUserRole>> pageQuery(@Validated @ModelAttribute PageReq pageReq,@ModelAttribute SysUserRole model) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>(model);
        return Result.success(PageUtils.page(
            sysUserRoleService.page(PageUtils.page(pageReq), wrapper)
        ));
    }

    @GetMapping(value = "/list")
    public Result findList(@ModelAttribute SysUserRole model) {

        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>(model);
        List<SysUserRole> list = sysUserRoleService.list(wrapper);
        return Result.success(list);
    }

}
