package com.cu.framework.sample.authcore.web;

import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.AuthMenuDoc;
import com.cu.framework.basic.model.Result;
import com.cu.framework.sample.authcore.authobject.DemoInheritDept;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 继承型权限串的管辖演示。
 * <p>
 * {@code /deptInherit/ledger} 在 {@code demo_menu_auth} 里只被授予了
 * {@code DEPTINHERIT__100} 一条串（见 {@link DemoInheritDept#FLAG}），
 * 于是"谁能进来"完全取决于谁持有这条串——核心层不认识"下级部门"这个语义，
 * 它只保证这条串能被编解码、能被逐值比对。语义由具体权限模型赋予，见 auth-rbac 示例
 *
 * @author kewen
 * @since 2026-10-22
 */
@RestController
@RequestMapping("/deptInherit")
public class DeptInheritController {

    /**
     * 受菜单管辖的账本端点，无声明的类里只管辖这一个方法
     *
     * @return 固定文案，判据在结果码而不在响应内容
     */
    @AuthMenu
    @AuthMenuDoc(name = "继承串管辖端点")
    @GetMapping("/ledger")
    public Result<String> ledger() {
        return Result.success("ledger-ok");
    }
}
