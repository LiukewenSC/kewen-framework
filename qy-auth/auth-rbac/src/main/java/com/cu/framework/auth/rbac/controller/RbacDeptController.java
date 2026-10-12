package com.cu.framework.auth.rbac.controller;

import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.rbac.model.RbacIdReq;
import com.cu.framework.auth.rbac.model.RbacPageConverter;
import com.cu.framework.auth.rbac.model.RbacPageReq;
import com.cu.framework.auth.rbac.model.RbacPageResult;
import com.cu.framework.auth.rbac.model.RbacResult;
import com.cu.framework.auth.rbac.mp.entity.SysDept;
import com.cu.framework.auth.rbac.mp.service.SysDeptMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rbac/dept")
public class RbacDeptController {

    @Autowired
    SysDeptMpService sysDeptMpService;


    @GetMapping("/list")
    @AuthMenu(name = "部门列表")
    public RbacResult<List<SysDept>> listDept(){
        List<SysDept> list = sysDeptMpService.list();
        return RbacResult.success(list);
    }
    @GetMapping("/page")
    @AuthMenu(name = "部门分页")
    public RbacResult<RbacPageResult<SysDept>> pageDept(@Validated RbacPageReq req){
        return RbacResult.success(RbacPageConverter.pageAndConvert(req, sysDeptMpService));
    }
    @PostMapping("/add")
    @AuthMenu(name = "添加部门")
    public RbacResult<Boolean> add(@RequestBody SysDept sysDept){
        boolean b = sysDeptMpService.save(sysDept);
        return RbacResult.success(b);
    }
    @PostMapping("/update")
    @AuthMenu(name = "部门列表")
    public RbacResult<Boolean> update(@RequestBody SysDept sysDept){
        if (sysDept.getId()==null){
            throw new RuntimeException("用户ID为空");
        }
        boolean b = sysDeptMpService.updateById(sysDept);
        return RbacResult.success(b);
    }
    @PostMapping("/delete")
    @AuthMenu(name = "删除部门")
    public RbacResult<Boolean> delete(@RequestBody @Validated RbacIdReq rbacIdReq){
        boolean b = sysDeptMpService.removeById(rbacIdReq.getId());
        return RbacResult.success(b);
    }
}
