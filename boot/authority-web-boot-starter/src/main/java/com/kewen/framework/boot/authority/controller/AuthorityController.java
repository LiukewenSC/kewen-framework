package com.kewen.framework.boot.authority.controller;


import com.kewen.framework.base.authority.context.CurrentUserContext;
import com.kewen.framework.base.authority.entity.SysMenu;
import com.kewen.framework.base.authority.model.MenuResp;
import com.kewen.framework.base.authority.service.SysMenuAuthUnify;
import com.kewen.framework.base.common.model.Result;
import com.kewen.framework.boot.authority.advance.menucheck.AuthMenu;
import com.kewen.framework.boot.authority.model.BusinessAuthorityEditReq;
import com.kewen.framework.boot.authority.model.MenuAuthorityEditReq;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

/**
 * @descrpition 权限控制器
 * @author kewen
 * @since 2022-12-01 15:23
 */
@Api(tags = "权限")
@RestController
@RequestMapping("/authority")
public class AuthorityController {

	@Autowired
	private SysMenuAuthUnify sysMenuAuthUnify;

	@ApiOperation("获取有权限菜单")
	@GetMapping("/getVisibleMenus")
	public Result<List<MenuResp>> getVisibleMenus() {
		Collection<String> userAuthorities = CurrentUserContext.getCurrentUserAuths();
		List<MenuResp> menuTree = sysMenuAuthUnify.getCurrentUserMenuTree(userAuthorities);

		return Result.success(menuTree);
	}
	@ApiOperation("查询所有菜单(树形结构返回)")
	@GetMapping("/getMenuTree")
	@AuthMenu
	public Result<List<MenuResp>> getMenuTree(){
		List<MenuResp> menuTree = sysMenuAuthUnify.getMenuTree();
		return Result.success(menuTree);
	}
	@ApiOperation("修改菜单信息")
	@PostMapping("/updateMenu")
	@AuthMenu
	public Result<Void> updateMenu(@RequestBody SysMenu sysMenu){
		sysMenuAuthUnify.updateMenu(sysMenu);
		return Result.success();
	}
	@ApiOperation("编辑菜单权限")
	@PostMapping("/editMenuAuthorities")
	@AuthMenu
	public Result<Void> editMenuAuthorities(@RequestBody MenuAuthorityEditReq req){
		sysMenuAuthUnify.editMenuAuthorities(req.getMenuId(),req.getAuthority());
		return Result.success();
	}
	@ApiOperation("编辑业务权限")
	@PostMapping("/editBusinessAuthority")
	@AuthMenu
	public Result<Void> editBusinessAuthority(@RequestBody @Validated BusinessAuthorityEditReq req){
		sysMenuAuthUnify.editBusinessAuthority(req.getBusinessId(),req.getModule(),req.getOperate(),req.getAuthority());
		return Result.success();
	}

}
