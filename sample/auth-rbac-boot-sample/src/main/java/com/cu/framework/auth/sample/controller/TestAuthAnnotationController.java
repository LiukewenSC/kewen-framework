package com.cu.framework.auth.sample.controller;

import com.cu.framework.auth.core.AuthDataOperation;
import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.AuthMenuDoc;
import com.cu.framework.auth.core.data.edit.DataIdCarrier;
import com.cu.framework.auth.sample.mp.entity.TestauthAnnotationBusiness;
import com.cu.framework.auth.sample.mp.service.TestauthAnnotationBusinessMpService;
import com.cu.framework.basic.model.PageReq;
import com.cu.framework.basic.model.PageResult;
import com.cu.framework.basic.model.Result;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
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
     * 测试数据范围。
     * 数据范围声明在 TestauthAnnotationBusinessMpMapper 的查询方法上
     * @return
     */
    @GetMapping("/dataRange")
    public Object testDataRange() {
        List<TestauthAnnotationBusiness> list = testauthAnnotationBusinessMpService.listAuthRange();
        System.out.println(list);
        Assert.isTrue(list.size()==1, "菜单列表为空");
        return list;
    }


    /**
     * 测试数据范围的分页版本，列表与计数各是一条声明过的语句，总数与行集口径一致
     * @return
     */
    @GetMapping("/pageDataRange")
    public Object pageDataRange(PageReq pageReq) {
        PageResult<TestauthAnnotationBusiness> result = testauthAnnotationBusinessMpService.pageAuthRange(pageReq);
        Assert.isTrue(!result.getData().isEmpty(), "测试的表数据为空");
        return Result.success(result);
    }

    /**
     * 测试数据编辑
     * @return
     */
    @PostMapping("/dataEdit")
    @AuthDataOperation(businessFunction = "testedit")
    public Result testDataEdit(@RequestBody EditDataIdCarrier editBusinessData) {
        System.out.println("successEdit");
        return Result.success(editBusinessData);
    }

    /**
     * 测试批量数据编辑：一次提交多条业务数据ID，全部命中才放行，任一条无权限即整体拒绝
     * @return
     */
    @PostMapping("/dataEditBatch")
    @AuthDataOperation(businessFunction = "testedit")
    public Result testDataEditBatch(@RequestBody List<EditDataIdCarrier> editBusinessDataList) {
        System.out.println("successBatchEdit");
        return Result.success(editBusinessDataList);
    }


    /**
     * 测试菜单控制
     * @return
     */
    @AuthMenu
    @AuthMenuDoc(name = "测试菜单控制")
    @GetMapping("/checkMenu")
    public Result testCheckMenu() {
        return Result.success("成功通过注解AuthMenu完成控制");
    }
    /**
     * 测试菜单控制
     * @return
     */
    @AuthMenu
    @AuthMenuDoc(name = "测试菜单控制无权限")
    @GetMapping("/checkMenuNone")
    public String testCheckMenuNone() {
        return "没有权限执行";
    }

    /**
     * 测试数据编辑接口的请求体，实现 DataIdCarrier 交出业务数据 ID，
     * 供 @AuthDataOperation 切面校验当前登录人对该数据的操作权限
     * @author kewen
     * @since 2026-02-12
     */
    @Data
    public static class EditDataIdCarrier implements DataIdCarrier<Long> {

        /** 业务数据 ID，即数据权限校验时的 dataId */
        private Long id;

        private String name;

        @Override
        public Collection<Long> getDataIds() {
            return Collections.singletonList(id);
        }
    }
}
