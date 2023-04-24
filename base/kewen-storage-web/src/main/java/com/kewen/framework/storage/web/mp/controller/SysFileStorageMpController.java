package com.kewen.framework.storage.web.mp.controller;

import com.kewen.framework.storage.web.mp.service.SysFileStorageMpService;
import com.kewen.framework.storage.web.mp.entity.SysFileStorage;
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
 * 文件存储 前端控制器
 * </p>
 *
 * @author kewen
 * @since 2023-04-24
 */
@RestController
@RequestMapping("/mp/sysFileStorage")
public class SysFileStorageMpController {

    @Autowired
    SysFileStorageMpService sysFileStorageService;




    @PostMapping("/add")
    public Result add(@RequestBody SysFileStorage entity){

        sysFileStorageService.save(entity);

        return Result.success();
    }

    @PostMapping("/updateById")
    public Result updateById(@RequestBody SysFileStorage entity){

        sysFileStorageService.updateById(entity);

        return Result.success();
    }

    @PostMapping(value = "/deleteById")
    public Result deleteById(@RequestBody @Validated IdReq req ) {

        sysFileStorageService.removeById(req.getId());

        return Result.success();
    }

    @PostMapping("/deleteBatchByIds")
    public Result deleteBatch(@Validated @RequestBody IdsReq req ) {
        sysFileStorageService.removeBatchByIds(req.getIds());
        return Result.success();
    }

    @GetMapping(value = "/getById")
    public Result findById(@NotNull @RequestParam("id") Long id) {

        SysFileStorage entity = sysFileStorageService.getById(id);
        return Result.success(entity);
    }

    @GetMapping(value = "/page")
    public Result<PageResult<SysFileStorage>> pageQuery(@Validated @ModelAttribute PageReq pageReq,@ModelAttribute SysFileStorage model) {
        LambdaQueryWrapper<SysFileStorage> wrapper = new LambdaQueryWrapper<>(model);
        return Result.success(PageUtils.page(
            sysFileStorageService.page(PageUtils.page(pageReq), wrapper)
        ));
    }

    @GetMapping(value = "/list")
    public Result findList(@ModelAttribute SysFileStorage model) {

        LambdaQueryWrapper<SysFileStorage> wrapper = new LambdaQueryWrapper<>(model);
        List<SysFileStorage> list = sysFileStorageService.list(wrapper);
        return Result.success(list);
    }

}
