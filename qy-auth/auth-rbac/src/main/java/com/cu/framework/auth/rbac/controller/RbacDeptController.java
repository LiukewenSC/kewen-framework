package com.cu.framework.auth.rbac.controller;

import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.rbac.mp.entity.SysDept;
import com.cu.framework.auth.rbac.mp.service.SysDeptMpService;
import com.cu.framework.basic.model.IdReq;
import com.cu.framework.basic.model.PageReq;
import com.cu.framework.basic.model.PageResult;
import com.cu.framework.basic.model.Result;
import com.cu.framework.basic.support.utils.MybatisPageExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 部门管理接口（/rbac/dept），提供部门的列表、分页、新增、修改与删除，方法上的 @AuthMenu 用于生成菜单权限标识
 * @author kewen
 * @since 2026-02-12
 */
@RestController
@RequestMapping("/rbac/dept")
public class RbacDeptController {

    @Autowired
    SysDeptMpService sysDeptMpService;


    /**
     * 查询全部部门列表（GET /rbac/dept/list，菜单权限"部门列表"）
     * @return 部门列表
     */
    @GetMapping("/list")
    @AuthMenu(name = "部门列表")
    public Result<List<SysDept>> listDept(){
        List<SysDept> list = sysDeptMpService.list();
        return Result.success(list);
    }
    /**
     * 分页查询部门（GET /rbac/dept/page，菜单权限"部门分页"）
     * @param req 分页请求参数
     * @return 部门分页结果
     */
    @GetMapping("/page")
    @AuthMenu(name = "部门分页")
    public Result<PageResult<SysDept>> pageDept(@Validated PageReq req){
        return Result.success(MybatisPageExecutor.pageAndConvert(req, sysDeptMpService));
    }
    /**
     * 新增部门（POST /rbac/dept/add，菜单权限"添加部门"）
     * @param sysDept 待新增的部门
     * @return 是否新增成功
     */
    @PostMapping("/add")
    @AuthMenu(name = "添加部门")
    public Result<Boolean> add(@RequestBody SysDept sysDept){
        boolean b = sysDeptMpService.save(sysDept);
        return Result.success(b);
    }
    /**
     * 按 ID 修改部门（POST /rbac/dept/update），请求体未携带 ID 时抛出异常
     * @param sysDept 待更新的部门，ID 必填
     * @return 是否修改成功
     */
    @PostMapping("/update")
    @AuthMenu(name = "部门列表")
    public Result<Boolean> update(@RequestBody SysDept sysDept){
        if (sysDept.getId()==null){
            throw new RuntimeException("部门ID为空");
        }
        boolean b = sysDeptMpService.updateById(sysDept);
        return Result.success(b);
    }
    /**
     * 按 ID 删除部门（POST /rbac/dept/delete，菜单权限"删除部门"）
     * @param idReq 携带部门 ID 的请求体
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @AuthMenu(name = "删除部门")
    public Result<Boolean> delete(@RequestBody @Validated IdReq idReq){
        boolean b = sysDeptMpService.removeById(idReq.getId());
        return Result.success(b);
    }
}
