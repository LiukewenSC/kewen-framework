package com.cu.framework.sample.controller;

import com.cu.framework.auth.core.AuthDataOperation;
import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.AuthMenuDoc;
import com.cu.framework.auth.core.data.edit.DataIdCarrier;
import com.cu.framework.basic.model.PageReq;
import com.cu.framework.basic.model.PageResult;
import com.cu.framework.basic.model.Result;
import com.cu.framework.sample.mp.entity.SampleBusiness;
import com.cu.framework.sample.mp.service.SampleBusinessMpService;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 权限注解演示：数据范围、行级数据操作、菜单正反例。
 * <p>
 * 本控制器不声明请求级身份，登录人由 Spring Security 会话提供，权限由 RBAC 体系在登录时装载。
 *
 * @author kewen
 * @since 2026-10-30
 */
@RestController
@RequestMapping("/test")
public class TestAuthAnnotationController {

    private final SampleBusinessMpService sampleBusinessMpService;

    public TestAuthAnnotationController(SampleBusinessMpService sampleBusinessMpService) {
        this.sampleBusinessMpService = sampleBusinessMpService;
    }

    /**
     * 数据范围查询：范围声明在 Mapper 的查询方法上，这里只取结果
     *
     * @return 当前登录人可见的业务数据
     */
    @GetMapping("/dataRange")
    public Result<List<SampleBusiness>> dataRange() {
        return Result.success(sampleBusinessMpService.listAuthRange());
    }

    /**
     * 数据范围的分页版本，列表与计数各是一条声明过的语句
     *
     * @param pageReq 分页参数
     * @return 当前登录人可见的业务数据分页
     */
    @GetMapping("/pageDataRange")
    public Result<PageResult<SampleBusiness>> pageDataRange(@Validated PageReq pageReq) {
        return Result.success(sampleBusinessMpService.pageAuthRange(pageReq));
    }

    /**
     * 行级数据操作：请求体交出业务数据 ID，切面在方法执行前判定
     * <p>
     * 方法体刻意不写任何数据，让验证台可以把这条调用当作只读用例
     *
     * @param req 业务数据 ID
     * @return 原样返回请求体
     */
    @PostMapping("/dataEdit")
    @AuthDataOperation(businessFunction = "testedit")
    public Result<DataEditReq> dataEdit(@RequestBody @Validated DataEditReq req) {
        return Result.success(req);
    }

    /**
     * 菜单鉴权正例：自身 auth_type=2，继承父菜单 /test 的 ROLE__1
     *
     * @return 固定文案
     */
    @GetMapping("/checkMenu")
    @AuthMenu
    @AuthMenuDoc(name = "测试菜单控制")
    public Result<String> checkMenu() {
        return Result.success("成功通过注解AuthMenu完成控制");
    }

    /**
     * 菜单鉴权反例：auth_type=1 且没有任何授权行
     *
     * @return 固定文案
     */
    @GetMapping("/checkMenuNone")
    @AuthMenu
    @AuthMenuDoc(name = "测试菜单控制无权限")
    public Result<String> checkMenuNone() {
        return Result.success("没有权限执行");
    }

    /**
     * 数据编辑请求体，实现 DataIdCarrier 交出业务数据 ID。
     */
    @Data
    public static class DataEditReq implements DataIdCarrier<Long> {

        /** 业务数据 ID */
        @NotNull
        private Long id;

        /** 演示字段 */
        private String name;

        @Override
        public Collection<Long> getDataIds() {
            return Collections.singletonList(id);
        }
    }
}
