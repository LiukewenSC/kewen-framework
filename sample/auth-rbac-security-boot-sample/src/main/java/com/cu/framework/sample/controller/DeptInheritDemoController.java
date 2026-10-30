package com.cu.framework.sample.controller;

import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.AuthMenuDoc;
import com.cu.framework.basic.model.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 部门继承权限演示：菜单上只挂一条 {@code RELATIONDEPT__2}（总部）。
 * <p>
 * 类上的 {@link AuthMenu} 批量管辖本类全部端点。路径与授权行预先写在
 * {@code sql/seed-data.sql} 里并显式标为 auth_type=1——菜单注册器会把非根行默认标成
 * “基于父权限”，若交给它生成，这个端点会继承父菜单的 ROLE__1，演示就不成立了。
 *
 * @author kewen
 * @since 2026-10-30
 */
@RestController
@RequestMapping("/deptInherit")
@AuthMenu
@AuthMenuDoc(name = "部门继承演示")
public class DeptInheritDemoController {

    /**
     * 只有总部的下级部门成员能进来；总部自己的成员走不出这条授权，因为 RELATIONDEPT 不含本部门
     *
     * @return 演示用的固定列表
     */
    @GetMapping("/rooms")
    @AuthMenuDoc(name = "部门继承演示>总部下级可见")
    public Result<List<String>> roomsOfSubordinateDepts() {
        return Result.success(Arrays.asList("研发部会议室", "外部合作部会议室"));
    }
}
