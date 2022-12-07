package com.kewen.framework.base.authority.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kewen.framework.base.authority.service.SysPermissionService;
import com.kewen.framework.base.authority.entity.SysPermission;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import com.kewen.framework.base.common.model.Result;


import java.util.List;


/**
 * <p>
 * 权限表 前端控制器
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
@RestController
@RequestMapping("/sysPermission")
public class SysPermissionController {

    @Autowired
    SysPermissionService sysPermissionService;

    @GetMapping("/save")
    public Result save(@ModelAttribute SysPermission entity){

        sysPermissionService.saveOrUpdate(entity);

        return Result.success();
    }

    @GetMapping(value = "/delete/{id}")
    public Result delete(@PathVariable String id) {

        sysPermissionService.removeById(id);

        return Result.success();
    }

    @GetMapping(value = "/findById/{id}")
    public Result findById(@PathVariable("id") String id) {

        SysPermission entity = sysPermissionService.getById(id);
        return Result.success(entity);
    }

    @GetMapping(value = "/pageQuery")
    public Result pageQuery(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @ModelAttribute SysPermission queryModel) {
        Page<SysPermission> page = sysPermissionService.pageQuery(pageNo, pageSize, queryModel);
        return Result.success(page);
    }

    @GetMapping(value = "/findList")
    public Result findList(@ModelAttribute SysPermission queryModel) {

        List<SysPermission> list = sysPermissionService.findList(queryModel);

        return Result.success(list);
    }

}
