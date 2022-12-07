package com.kewen.framework.base.authority.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kewen.framework.base.authority.service.SysPositionService;
import com.kewen.framework.base.authority.entity.SysPosition;
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
 * 岗位表 前端控制器
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
@RestController
@RequestMapping("/sysPosition")
public class SysPositionController {

    @Autowired
    SysPositionService sysPositionService;

    @GetMapping("/save")
    public Result save(@ModelAttribute SysPosition entity){

        sysPositionService.saveOrUpdate(entity);

        return Result.success();
    }

    @GetMapping(value = "/delete/{id}")
    public Result delete(@PathVariable String id) {

        sysPositionService.removeById(id);

        return Result.success();
    }

    @GetMapping(value = "/findById/{id}")
    public Result findById(@PathVariable("id") String id) {

        SysPosition entity = sysPositionService.getById(id);
        return Result.success(entity);
    }

    @GetMapping(value = "/pageQuery")
    public Result pageQuery(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @ModelAttribute SysPosition queryModel) {
        Page<SysPosition> page = sysPositionService.pageQuery(pageNo, pageSize, queryModel);
        return Result.success(page);
    }

    @GetMapping(value = "/findList")
    public Result findList(@ModelAttribute SysPosition queryModel) {

        List<SysPosition> list = sysPositionService.findList(queryModel);

        return Result.success(list);
    }

}
