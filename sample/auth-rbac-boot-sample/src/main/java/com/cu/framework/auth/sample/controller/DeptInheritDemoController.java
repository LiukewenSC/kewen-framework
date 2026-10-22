package com.cu.framework.auth.sample.controller;

import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.AuthMenuDoc;
import com.cu.framework.basic.model.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 部门继承权限演示：菜单上只挂一条 <code>RELATIONDEPT__&lt;总部ID&gt;</code>，
 * 用来对照"总部的成员"与"总部子部门的成员"访问同一个端点的不同结果。
 * <p>
 * 类上的 {@link AuthMenu} 批量管辖本类全部端点。路径与授权行都预先写在
 * <code>sql/seed-data.sql</code> 里并给出固定 ID——菜单注册只会补种子里没有的路径，
 * 若让它现场生成雪花 ID，授权行就没有可引用的 api_id 了
 *
 * @author kewen
 * @since 2026-10-22
 */
@RestController
@RequestMapping("/deptInherit")
@AuthMenu
@AuthMenuDoc(name = "部门继承演示")
public class DeptInheritDemoController {

    /**
     * 只有总部的<b>下级部门</b>成员能进来；总部自己的成员走不出这条授权，
     * 因为 RELATIONDEPT 不含本部门
     * @return 演示用的固定列表
     */
    @GetMapping("/rooms")
    @AuthMenuDoc(name = "部门继承演示>总部下级可见")
    public Result<List<String>> roomsOfSubordinateDepts() {
        return Result.success(Arrays.asList("研发部会议室", "外部合作部会议室"));
    }
}
