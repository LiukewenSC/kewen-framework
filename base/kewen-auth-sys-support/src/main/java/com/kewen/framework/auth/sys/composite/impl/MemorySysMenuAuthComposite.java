package com.kewen.framework.auth.sys.composite.impl;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kewen.framework.auth.sys.composite.SysMenuAuthComposite;
import com.kewen.framework.auth.sys.composite.mapper.SysUserCompositeMapper;
import com.kewen.framework.auth.sys.constant.MenuTypeConstant;
import com.kewen.framework.auth.sys.model.SysAuthority;
import com.kewen.framework.auth.sys.model.req.MenuSaveReq;
import com.kewen.framework.auth.sys.model.resp.MenuResp;
import com.kewen.framework.auth.sys.model.resp.MenuRespBase;
import com.kewen.framework.auth.sys.mp.entity.SysApplicationAuth;
import com.kewen.framework.auth.sys.mp.entity.SysMenu;
import com.kewen.framework.auth.sys.mp.entity.SysMenuAuth;
import com.kewen.framework.auth.sys.utils.AuthorityConvertUtil;
import com.kewen.framework.auth.sys.mp.service.SysApplicationAuthMpService;
import com.kewen.framework.auth.sys.mp.service.SysMenuAuthMpService;
import com.kewen.framework.auth.sys.mp.service.SysMenuMpService;
import com.kewen.framework.common.core.exception.BizException;
import com.kewen.framework.common.core.model.IdReq;
import com.kewen.framework.common.core.utils.BeanUtil;
import com.kewen.framework.common.core.utils.TreeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
@Component
public class MemorySysMenuAuthComposite implements SysMenuAuthComposite {
    @Autowired
    SysUserCompositeMapper userCompositeMapper;
    @Autowired
    private SysMenuMpService sysMenuService;
    @Autowired
    private SysMenuAuthMpService menuAuthService;
    @Autowired
    private SysApplicationAuthMpService applicationAuthService;


    /**
     * 接口为懒加载， 第一次调用 getSysMenuAuths() 方法生效，不要直接调用属性
     */
    private List<SysMenuAuth> sysMenuAuths;
    /**
     * 接口为懒加载， 第一次调用 sysMenus() 方法生效，不要直接调用属性
     */
    private List<SysMenu> sysMenus ;

    private boolean usedCache = false;

    @Override
    public boolean hasMenuAuth(Collection<String> authorities, String url) {
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
                .map(l -> BeanUtil.convert(l, MenuResp.class))
                .peek(m-> {
                    List<SysMenuAuth> sysMenuAuths = authByMenuMap.get(m.getId());
                    if (sysMenuAuths!=null){
                        m.setAuthority(AuthorityConvertUtil.parse(
                                sysMenuAuths.stream().map(a-> new SysAuthority(a.getAuthority(),a.getDescription())).collect(Collectors.toList())
                        ));
                    }
                })
                .collect(Collectors.toList());
        return TreeUtil.transfer(collect, 0L);
    }

    @Override
    public List<MenuResp> getCurrentUserMenuTree(Collection<String> authorities) {
        //得到全部树
        List<MenuResp> trees = getMenuTree();
        //克隆副本
        List<MenuResp> menuTree = BeanUtil.cloneList(trees);
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


    @Override
    public void editMenuAuthorities(Long menuId, List<SysAuthority> to) {
        //移除原有的
        menuAuthService.remove(
                new LambdaQueryWrapper<SysMenuAuth>().eq(SysMenuAuth::getMenuId,menuId)
        );
        //批量插入新的
        if (!CollectionUtils.isEmpty(to)){
            menuAuthService.saveBatch(
                    to.stream()
                            .map(a->
                                    new SysMenuAuth()
                                            .setMenuId(menuId)
                                            .setAuthority(a.getAuthority())
                                            .setDescription(a.getDescription())
                            ).collect(Collectors.toList())
            );
        }
    }

    @Override
    public void editBusinessAuthority(Long businessId,String module,String operate, List<SysAuthority> to) {
        //移除原有的
        applicationAuthService.remove(
                new LambdaQueryWrapper<SysApplicationAuth>().eq(SysApplicationAuth::getBusinessId,businessId)
        );
        //批量插入新的
        if (!CollectionUtils.isEmpty(to)){
            applicationAuthService.saveBatch(
                    to.stream()
                            .map(a->
                                    new SysApplicationAuth()
                                            .setBusinessId(businessId)
                                            .setModule(module)
                                            .setOperate(operate)
                                            .setAuthority(a.getAuthority())
                                            .setDescription(a.getDescription())
                            ).collect(Collectors.toList())
            );
        }
    }

    @Override
    public boolean hasBusinessAuth(Collection<String> auths, String module, String operate, Long businessId) {
        Integer integer = userCompositeMapper.hasAuth(auths, module, operate, businessId);
        return integer > 0;
    }

    @Override
    @Transactional
    public void addMenu(MenuSaveReq req) {
        sysMenuService.save(req);
        Long menuId = req.getId();
        List<SysAuthority> to = AuthorityConvertUtil.to(req.getAuthority());
        editMenuAuthorities(menuId,to);
    }

    @Override
    @Transactional
    public void updateMenu(MenuSaveReq req) {
        sysMenuService.updateById(req);
        Long menuId = req.getId();
        List<SysAuthority> to = AuthorityConvertUtil.to(req.getAuthority());
        editMenuAuthorities(menuId,to);
    }

    @Override
    @Transactional
    public void deleteMenu(IdReq req) {
        List<MenuResp> menuTrees = getMenuTree();
        MenuResp menuResp = TreeUtil.fetchSubTree(menuTrees, req.getId());
        if (menuResp==null){
            throw new BizException("菜单为空");
        }
        //获取平菜单列表
        List<MenuResp> deleteMenus = TreeUtil.unTransfer(menuResp);
        //找到需要删除的菜单id
        List<Long> menuIds = deleteMenus.stream().map(MenuRespBase::getId).collect(Collectors.toList());

        //移除菜单
        sysMenuService.removeBatchByIds(menuIds);
        //移除菜单权限
        menuAuthService.remove(
                new LambdaQueryWrapper<SysMenuAuth>()
                        .in(SysMenuAuth::getMenuId,menuIds)
        );
    }


    /**
     * 是否需要移除本菜单，并在内部递归移除掉不属于自己的
     * @param menuResp 当前菜单
     * @param authorities 用户权限
     * @return
     */
    private boolean needRemove(MenuResp menuResp,Collection<String> authorities){
        List<MenuResp> subs = menuResp.getChildren();

        if (CollectionUtils.isEmpty(subs)){
            //已经到了最底层，判断自己是否需要移除
            boolean hasAuth = hasMenuAuth(authorities, BeanUtil.convert(menuResp, SysMenu.class));
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
                boolean hasAuth = hasMenuAuth(authorities, BeanUtil.convert(menuResp, SysMenu.class));
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
        if (MenuTypeConstant.AUTH_TYPE.SELF.equals(sysMenu.getAuthType())){
            Long menuId = sysMenu.getId();
            return getSysMenuAuths().stream()
                    .filter(a -> a.getMenuId().equals(menuId))
                    .anyMatch(a -> authorities.contains(a.getAuthority()));
        } else if (MenuTypeConstant.AUTH_TYPE.PARENT.equals(sysMenu.getAuthType())){
            //基于父菜单的权限
            Long parentId = sysMenu.getParentId();
            Optional<SysMenu> first = getSysMenus().stream().filter(m -> m.getId().equals(parentId)).findFirst();
            if (!first.isPresent()){
                //已经到顶了，找不到 top对应的parent的菜单
                log.info("上层追溯到根菜单，仍未找到对应的权限");
                return false;
            }
            return hasMenuAuth(authorities,first.get());
        } else {
            throw new BizException("菜单权限类型错误: "+sysMenu.getAuthType());
        }
    }

    /**
     * 懒加载获取菜单列表
     * @return
     */
    private List<SysMenu> getSysMenus(){
        if (!usedCache){
            return sysMenuService.list();
        }
        if (this.sysMenus !=null){
            return this.sysMenus;
        }
        List<SysMenu> sysMenus = sysMenuService.list();
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
        if (!usedCache){
            return menuAuthService.list();
        }
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
