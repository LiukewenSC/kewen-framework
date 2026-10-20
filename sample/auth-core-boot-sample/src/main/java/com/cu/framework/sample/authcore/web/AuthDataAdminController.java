package com.cu.framework.sample.authcore.web;

import com.cu.framework.auth.core.AuthDataOperation;
import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.AuthMenuDoc;
import com.cu.framework.basic.model.Result;
import com.cu.framework.sample.authcore.model.DemoRequests.GrantReq;
import com.cu.framework.sample.authcore.model.DemoRequests.RemoveReq;
import com.cu.framework.sample.authcore.service.DemoAuthAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 权限维护端点：不经注解、直接在 Service 层用 AuthDataAdaptor 读写数据权限。
 * <p>
 * 管辖声明打在<em>管理动作</em>自己的槽位（demo_admin / unified / 固定ID 900）上，
 * 与被维护的目标数据分开——框架注释里提醒的那条风险（编辑此接口本身的权限会把自己关掉）
 * 就是这么避开的
 *
 * @author kewen
 * @since 2026-10-20
 */
@RestController
@RequestMapping("/authData")
public class AuthDataAdminController {

    @Autowired
    private DemoAuthAdminService demoAuthAdminService;

    /**
     * 给某条目标数据在指定槽位上设置权限集合
     *
     * @param req 授权请求
     * @return 写入后的槽位内容
     */
    @AuthMenu
    @AuthMenuDoc(name = "设置数据权限")
    @AuthDataOperation(businessFunction = "demo_admin", operate = "unified")
    @PostMapping("/grant")
    public Result<Map<String, Object>> grant(@RequestBody GrantReq req) {
        return Result.success(demoAuthAdminService.grant(req));
    }

    /**
     * 清空某条目标数据的全部权限
     *
     * @param req 清空请求
     * @return 剩余情况
     */
    @AuthMenu
    @AuthMenuDoc(name = "清空数据权限")
    @AuthDataOperation(businessFunction = "demo_admin", operate = "unified")
    @PostMapping("/remove")
    public Result<Map<String, Object>> remove(@RequestBody RemoveReq req) {
        return Result.success(demoAuthAdminService.remove(req));
    }

    /**
     * 把某条数据在指定槽位上的权限读进权限集合体
     *
     * @param req 目标（复用授权请求体，忽略权限串两列）
     * @return 读回结果
     */
    @AuthMenu
    @AuthMenuDoc(name = "读回数据权限")
    @AuthDataOperation(businessFunction = "demo_admin", operate = "unified")
    @PostMapping("/fill")
    public Result<Map<String, Object>> fill(@RequestBody GrantReq req) {
        return Result.success(demoAuthAdminService.fill(req.getBizModule(), req.getAction(), req.getBizDataId()));
    }
}
