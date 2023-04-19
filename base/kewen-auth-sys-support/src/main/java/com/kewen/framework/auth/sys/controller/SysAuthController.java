package com.kewen.framework.auth.sys.controller;



import com.kewen.framework.auth.sys.model.req.UpdatePasswordReq;
import com.kewen.framework.auth.sys.mp.entity.SysMenu;
import com.kewen.framework.auth.sys.model.resp.MenuResp;
import com.kewen.framework.auth.sys.composite.SysMenuAuthComposite;
import com.kewen.framework.auth.sys.utils.AuthorityConvertUtil;
import com.kewen.framework.common.core.model.IdReq;
import com.kewen.framework.common.core.model.Result;
import com.kewen.framework.auth.core.annotation.CheckEndpoint;
import com.kewen.framework.auth.sys.model.req.BusinessAuthorityEditReq;
import com.kewen.framework.auth.sys.model.req.MenuAuthorityEditReq;
import com.kewen.framework.auth.context.AuthUserContext;
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


	@PostMapping("/resetPassword")
	public Result resetPassword(@RequestBody IdReq req){
		sysMenuAuthComposite.resetPassword(req);
		return Result.success();
	}
	@PostMapping("/updatePassword")
	public Result updatePassword(@RequestBody @Validated UpdatePasswordReq req){
		sysMenuAuthComposite.updatePassword(req);
		return Result.success();
	}

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
		sysMenuAuthComposite.editMenuAuthorities(req.getMenuId(), AuthorityConvertUtil.to(req.getAuthority()));
		return Result.success();
	}
	@PostMapping("/editBusinessAuthority")
	@CheckEndpoint
	public Result<Void> editBusinessAuthority(@RequestBody @Validated BusinessAuthorityEditReq req){
		sysMenuAuthComposite.editBusinessAuthority(req.getBusinessId(),req.getModule(),req.getOperate(), AuthorityConvertUtil.to(req.getAuthority()));
		return Result.success();
	}

}
