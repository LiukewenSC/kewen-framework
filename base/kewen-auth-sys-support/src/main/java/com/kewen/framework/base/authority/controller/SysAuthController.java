package com.kewen.framework.base.authority.controller;



import com.kewen.framework.base.authority.model.resp.MenuResp;
import com.kewen.framework.base.authority.mp.entity.SysMenu;
import com.kewen.framework.base.authority.support.SysMenuAuthComposite;
import com.kewen.framework.base.common.model.Result;
import com.kewen.framework.base.auth.annotation.CheckEndpoint;
import com.kewen.framework.base.authority.model.req.BusinessAuthorityEditReq;
import com.kewen.framework.base.authority.model.req.MenuAuthorityEditReq;
import com.kewen.framework.base.auth.context.AuthUserContext;
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
@RestController
@RequestMapping("/auth")
public class SysAuthController {

	@Autowired
	private SysMenuAuthComposite sysMenuAuthComposite;



	@GetMapping("/getVisibleMenus")
	public Result<List<MenuResp>> getVisibleMenus() {
		Collection<String> userAuthorities = AuthUserContext.getAuthorities();
		List<MenuResp> menuTree = sysMenuAuthComposite.getCurrentUserMenuTree(userAuthorities);

		return Result.success(menuTree);
	}
	@GetMapping("/getMenuTree")
	@CheckEndpoint
	public Result<List<MenuResp>> getMenuTree(){
		List<MenuResp> menuTree = sysMenuAuthComposite.getMenuTree();
		return Result.success(menuTree);
	}
	@PostMapping("/updateMenu")
	@CheckEndpoint
	public Result<Void> updateMenu(@RequestBody SysMenu sysMenu){
		sysMenuAuthComposite.updateMenu(sysMenu);
		return Result.success();
	}
	@PostMapping("/editMenuAuthorities")
	@CheckEndpoint
	public Result<Void> editMenuAuthorities(@RequestBody MenuAuthorityEditReq req){
		sysMenuAuthComposite.editMenuAuthorities(req.getMenuId(),req.getAuthority());
		return Result.success();
	}
	@PostMapping("/editBusinessAuthority")
	@CheckEndpoint
	public Result<Void> editBusinessAuthority(@RequestBody @Validated BusinessAuthorityEditReq req){
		sysMenuAuthComposite.editBusinessAuthority(req.getBusinessId(),req.getModule(),req.getOperate(),req.getAuthority());
		return Result.success();
	}

}
