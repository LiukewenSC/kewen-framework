package com.kewen.framework.auth.sys.controller;

import com.kewen.framework.auth.core.annotation.CheckDataEdit;
import com.kewen.framework.auth.core.annotation.CheckDataAuthEdit;
import com.kewen.framework.auth.core.annotation.CheckEndpoint;
import com.kewen.framework.auth.sys.annotation.SysApplicationBusiness;
import com.kewen.framework.auth.sys.annotation.SysAuthDataEditBusiness;
import com.kewen.framework.common.core.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kewen
 * @descrpition
 * @since 2022-11-26 18:16
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestAuthController {


    /**
     * 单个模块功能的编辑权限（不针对具体实例数据）
     * @return
     */
    @RequestMapping("/testMenuAuth")
    @CheckEndpoint
    public Result testMenuAuth(){
        log.info("测试 单个功能的编辑权限");
        return Result.success("测试编辑权限通过");
    }
    /**
     * 测试数据可见权限
     * @return
     */
    @RequestMapping("/testAuthRange")
    public Result testAuthVisible(){
        log.info("测试 测试数据可见权限");
        return Result.success();
    }
    /**
     * 单条数据的编辑权限
     * @param applicationBusiness
     */
    @RequestMapping("/testBusinessEdit")
    @CheckDataEdit(module = "test")
    public Result testBusinessEdit(@RequestBody SysApplicationBusiness applicationBusiness){
        log.info("测试 单条数据的编辑权限");

        return Result.success();
    }

    /**
     * 权限的编辑
     * @param applicationBusiness
     */
    @RequestMapping("/testAuthEdit")
    @CheckDataAuthEdit(module = "test")
    public Result testEditAuthEdit(@RequestBody SysAuthDataEditBusiness applicationBusiness){
        log.info("测试 权限的编辑");

        return Result.success();
    }



}
