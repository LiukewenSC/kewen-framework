package com.cu.framework.auth.rbac.extension;

import cn.hutool.core.util.IdUtil;
import com.cu.framework.auth.core.menu.MenuApiEntity;
import com.cu.framework.auth.core.menu.MenuApiService;
import com.cu.framework.auth.rbac.model.MenuTypeConstant;
import com.cu.framework.auth.rbac.mp.entity.SysMenuApi;
import com.cu.framework.auth.rbac.mp.service.SysMenuApiMpService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于RBAC的菜单API存储
 * @author kewen
 * @since 2024-08-09
 */
public class RbacMenuApiService implements MenuApiService<Long> {

    private final SysMenuApiMpService sysMenuApiMpService;

    public RbacMenuApiService(SysMenuApiMpService sysMenuApiMpService) {
        this.sysMenuApiMpService = sysMenuApiMpService;
    }

    @Override
    public List<MenuApiEntity<Long>> list() {
        List<SysMenuApi> list = sysMenuApiMpService.list();
        return list.stream().map(l-> new MenuApiEntity<Long>()
                .setId(l.getId())
                .setName(l.getName())
                .setPath(l.getPath())
                .setParentId(l.getParentId())
        ).collect(Collectors.toList());
    }

    @Override
    public void saveBatch(List<MenuApiEntity<Long>> menuApiEntities) {
        List<SysMenuApi> list = menuApiEntities.stream().map(m -> new SysMenuApi()
                        .setId(m.getId())
                        .setName(m.getName())
                        .setPath(m.getPath())
                        .setParentId(m.getParentId())
                        .setAuthType(fillAuthType(m.getParentId())))
                .collect(Collectors.toList());
        sysMenuApiMpService.saveBatch(list);
    }
    //填充类型， 父ID为0 则填充自己
    /**
     * 根据父 ID 填充菜单权限类型：顶级菜单（父 ID 为 null 或 0）返回 OWNER(1)-基于自身权限，
     * 否则返回 PARENT(2)-基于父菜单权限
     * @param parentId 父菜单 ID
     * @return 权限类型值，对应 MenuTypeConstant.AUTH_TYPE
     */
    public Integer fillAuthType(Long parentId){
        return (parentId==null || parentId == 0L)
                ? MenuTypeConstant.AUTH_TYPE.OWNER.getCode()
                : MenuTypeConstant.AUTH_TYPE.PARENT.getCode();
    }
    @Override
    public Long getRootParentId() {
        return 0L;
    }

    /**
     * 雪花算法生成id
     * @return
     */
    @Override
    public Long generateId() {
        return IdUtil.getSnowflakeNextId();
    }
}
