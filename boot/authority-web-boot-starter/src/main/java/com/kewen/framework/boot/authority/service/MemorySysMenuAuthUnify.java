package com.kewen.framework.boot.authority.service;

import com.kewen.framework.base.authority.entity.SysMenu;
import com.kewen.framework.base.authority.entity.SysMenuAuth;
import com.kewen.framework.base.authority.model.MenuResp;
import com.kewen.framework.base.authority.service.SysMenuAuthService;
import com.kewen.framework.base.authority.service.SysMenuService;
import com.kewen.framework.base.common.utils.BeanUtil;
import com.kewen.framework.base.common.utils.TreeUtil;
import com.kewen.framework.base.authority.enums.MenuAuthType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @descrpition 基于菜单存于内存的实现
 * @author kewen
 * @since 2022-12-01 10:21
 */
@Slf4j
public class MemorySysMenuAuthUnify implements SysMenuAuthUnify {

    private final SysMenuService menuService;
    private final SysMenuAuthService menuAuthService;

    /**
     * 接口为懒加载， 第一次调用 getSysMenuAuths() 方法生效，不要直接调用属性
     */
    private List<SysMenuAuth> sysMenuAuths;
    /**
     * 接口为懒加载， 第一次调用 sysMenus() 方法生效，不要直接调用属性
     */
    private List<SysMenu> sysMenus ;

    public MemorySysMenuAuthUnify(SysMenuService menuService, SysMenuAuthService menuAuthService) {
        this.menuService = menuService;
        this.menuAuthService = menuAuthService;
    }

    @Override
    public boolean hasAuth(Collection<String> authorities, String url) {
        //SysMenu sysMenu = menuService.getMenuByUrl(url);
        Optional<SysMenu> sysMenuOptional = getSysMenus().stream().filter(m -> Objects.equals(m.getUrl(),url)).findFirst();
        if (!sysMenuOptional.isPresent()){
            return false;
        }
        SysMenu sysMenu = sysMenuOptional.get();
        return hasMenuAuth(authorities,sysMenu);
    }

    @Override
    public List<MenuResp> getMenuTree() {
        List<SysMenu> sysMenus = getSysMenus();
        Map<Long, List<SysMenuAuth>> authByMenuMap = getSysMenuAuths().stream()
                .collect(Collectors.groupingBy(SysMenuAuth::getMenuId));
        List<MenuResp> collect = sysMenus.stream()
                .map(l -> BeanUtil.toBean(l, MenuResp.class))
                .peek(m->m.setAuths(authByMenuMap.get(m.getId())))
                .collect(Collectors.toList());
        return TreeUtil.transfer(collect, 0);
    }

    @Override
    public List<MenuResp> getCurrentUserMenuTree(Collection<String> authorities) {
        //得到全部树
        List<MenuResp> trees = getMenuTree();
        //克隆副本
        List<MenuResp> menuTree = BeanUtil.copyList(trees);
        //将不属于自己的移除
        Iterator<MenuResp> iterator = menuTree.iterator();
        while (iterator.hasNext()) {
            MenuResp next = iterator.next();
            boolean needRemove = needRemove(next, authorities);
            if (needRemove){
                iterator.remove();
            }
        }
        return menuTree;
    }

    /**
     * 是否需要移除本菜单，并在内部递归移除掉不属于自己的
     * @param menuResp 当前菜单
     * @param authorities 用户权限
     * @return
     */
    private boolean needRemove(MenuResp menuResp,Collection<String> authorities){
        List<MenuResp> subs = menuResp.getSubs();

        if (CollectionUtils.isEmpty(subs)){
            //已经到了最底层，判断自己是否需要移除
            boolean hasAuth = hasMenuAuth(authorities, BeanUtil.toBean(menuResp, SysMenu.class));
            //无权限则移除
            return !hasAuth;
        } else {
            //有子菜单，继续遍历子菜单
            Iterator<MenuResp> iterator = subs.iterator();
            while (iterator.hasNext()) {
                MenuResp next = iterator.next();
                boolean needRemove = needRemove(next,authorities);
                if (needRemove){
                    iterator.remove();
                }
            }
            //子菜单已经移除完了，则需要校验自己是否需要移除
            if (CollectionUtils.isEmpty(subs)){
                boolean hasAuth = hasMenuAuth(authorities, BeanUtil.toBean(menuResp, SysMenu.class));
                //无权限则移除
                return !hasAuth;
            } else {
                //子菜单未移除完，那么子菜单需要展示，自己也一定需要展示
                return false;
            }
        }
    }

    /**
     * 校验菜单权限 ，可递归校验，直到追踪到树根
     * @param authorities
     * @param sysMenu
     * @return
     */
    private boolean hasMenuAuth(Collection<String> authorities, SysMenu sysMenu){
        //基于自己的权限
        if (MenuAuthType.SELF==sysMenu.getAuthType()){
            Long menuId = sysMenu.getId();
            return getSysMenuAuths().stream()
                    .filter(a -> a.getMenuId().equals(menuId))
                    .anyMatch(a -> authorities.contains(a.getAuthority()));
        } else {
            //基于父菜单的权限
            Long parentId = sysMenu.getParentId();
            Optional<SysMenu> first = getSysMenus().stream().filter(m -> m.getId().equals(parentId)).findFirst();
            if (!first.isPresent()){
                //已经到顶了，找不到 top对应的parent的菜单
                log.info("上层追溯到根菜单，仍未找到对应的权限");
                return false;
            }
            return hasMenuAuth(authorities,first.get());
        }
    }

    /**
     * 懒加载获取菜单列表
     * @return
     */
    private List<SysMenu> getSysMenus(){
        if (this.sysMenus !=null){
            return this.sysMenus;
        }
        List<SysMenu> sysMenus = menuService.list();
        if (sysMenus==null){
            this.sysMenus=Collections.emptyList();
        }
        this.sysMenus=sysMenus;
        return this.sysMenus;
    }

    /**
     * 懒加载获取菜单权限列表
     * @return
     */
    private List<SysMenuAuth> getSysMenuAuths(){
        if (this.sysMenuAuths !=null){
            return this.sysMenuAuths;
        }
        List<SysMenuAuth> sysMenuAuths = menuAuthService.list();
        if (sysMenuAuths==null){
            this.sysMenuAuths=Collections.emptyList();
        }
        this.sysMenuAuths=sysMenuAuths;
        return this.sysMenuAuths;
    }



}
