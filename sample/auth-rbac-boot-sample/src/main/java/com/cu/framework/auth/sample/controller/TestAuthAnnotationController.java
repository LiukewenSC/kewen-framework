package com.cu.framework.auth.sample.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cu.framework.auth.core.AuthDataOperation;
import com.cu.framework.auth.core.AuthDataRange;
import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.data.edit.IdDataEdit;
import com.cu.framework.auth.sample.mp.entity.TestauthAnnotationBusiness;
import com.cu.framework.auth.sample.mp.service.TestauthAnnotationBusinessMpService;
import com.cu.framework.basic.model.PageReq;
import com.cu.framework.basic.model.PageResult;
import com.cu.framework.basic.model.Result;
import com.cu.framework.basic.support.utils.MybatisPageExecutor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试权限数据范围Controller
 * @author kewen
 * @descrpition
 * @since 2023-12-26
 */
@RestController
@RequestMapping("/test")
public class TestAuthAnnotationController {

    @Autowired
    TestauthAnnotationBusinessMpService testauthAnnotationBusinessMpService;

    /**
     * 测试数据范围
     * @return
     */
    @AuthDataRange(businessFunction = "testrange")
    @GetMapping("/dataRange")
    public Object testDataRange() {
        List<TestauthAnnotationBusiness> list = testauthAnnotationBusinessMpService.list();
        System.out.println(list);
        Assert.isTrue(list.size()==1, "菜单列表为空");
        return list;
    }


    /**
     * 测试数据范围
     * @return
     */
    @AuthDataRange(businessFunction = "testrange")
    @GetMapping("/pageDataRange")
    public Object pageDataRange(PageReq pageReq) {
        Page<TestauthAnnotationBusiness> page = new Page<>(pageReq.getPage(),pageReq.getSize());
        Page<TestauthAnnotationBusiness> mpPage = testauthAnnotationBusinessMpService.page(page);
        Assert.isTrue(!mpPage.getRecords().isEmpty(), "测试的表数据为空");
        PageResult<TestauthAnnotationBusiness> result = MybatisPageExecutor.baomidouPage2PageResult(mpPage);
        return Result.success(result);
    }

    /**
     * 测试数据编辑
     * @return
     */
    @PostMapping("/dataEdit")
    @AuthDataOperation(businessFunction = "testedit")
    public Result testDataEdit(@RequestBody EditIdDataEdit editBusinessData) {
        System.out.println("successEdit");
        return Result.success(editBusinessData);
    }


    /**
     * 测试菜单控制
     * @return
     */
    @AuthMenu(name = "测试菜单控制")
    @GetMapping("/checkMenu")
    public Result testCheckMenu() {
        return Result.success("成功通过注解AuthMenu完成控制");
    }
    /**
     * 测试菜单控制
     * @return
     */
    @AuthMenu(name = "测试菜单控制无权限")
    @GetMapping("/checkMenuNone")
    public String testCheckMenuNone() {
        return "没有权限执行";
    }

    /**
     * 测试数据编辑接口的请求体，实现 IdDataEdit 以提供业务数据 ID，
     * 供 @AuthDataOperation 切面校验当前登录人对该数据的操作权限
     * @author kewen
     * @since 2026-02-12
     */
    @Data
    public static class EditIdDataEdit implements IdDataEdit<Long> {

        /** 业务数据 ID，即数据权限校验时的 dataId */
        private Long id;

        private String name;

        @Override
        public Long getDataId() {
            return id;
        }
    }
}
