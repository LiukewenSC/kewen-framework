package com.kewen.framework.boot.authority.biz.controller;

import com.kewen.framework.boot.authority.advance.authcheck.AuthBusinessOperate;
import com.kewen.framework.boot.authority.advance.authcheck.DefaultApplicationBusiness;
import com.kewen.framework.boot.authority.advance.menucheck.AuthMenu;
import com.kewen.framework.base.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author kewen
 * @descrpition
 * @since 2022-11-26 18:16
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {


    /**
     * 单个模块功能的编辑权限（不针对具体实例数据）
     * @return
     */
    @RequestMapping("/testMenuAuth")
    @AuthMenu
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
    @RequestMapping("/testAuthEdit")
    @AuthBusinessOperate(module = "test")
    public Result testAuthEdit(@RequestBody DefaultApplicationBusiness applicationBusiness){
        log.info("测试 单条数据的编辑权限");

        return Result.success();
    }


}
