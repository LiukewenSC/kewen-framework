package com.kewen.framework.auth.sample.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kewen.framework.auth.core.AuthDataOperation;
import com.kewen.framework.auth.core.AuthDataRange;
import com.kewen.framework.auth.core.AuthMenu;
import com.kewen.framework.auth.core.data.edit.IdDataEdit;
import com.kewen.framework.auth.rabc.model.RabcPageReq;
import com.kewen.framework.auth.rabc.model.RabcPageResult;
import com.kewen.framework.auth.sample.model.Result;
import com.kewen.framework.auth.sample.mp.entity.TestauthAnnotationBusiness;
import com.kewen.framework.auth.sample.mp.service.TestauthAnnotationBusinessMpService;
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
    public Object pageDataRange(RabcPageReq RabcPageReq) {
        Page<TestauthAnnotationBusiness> page = new Page<>(RabcPageReq.getPage(),RabcPageReq.getSize());
        Page<TestauthAnnotationBusiness> RabcPageResult = testauthAnnotationBusinessMpService.page(page);
        Assert.isTrue(!RabcPageResult.getRecords().isEmpty(), "测试的表数据为空");
        RabcPageResult<TestauthAnnotationBusiness> result = new RabcPageResult<>();
        result.setTotal(RabcPageResult.getTotal());
        result.setData(RabcPageResult.getRecords());
        result.setPage(RabcPageReq.getPage());
        result.setSize(RabcPageResult.getSize());
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

    @Data
    public static class EditIdDataEdit implements IdDataEdit<Long> {

        private Long id;

        private String name;

        @Override
        public Long getDataId() {
            return id;
        }
    }
}
