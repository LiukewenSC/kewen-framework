package com.cu.framework.auth.rbac.model.resp;

import com.cu.framework.auth.rbac.composite.model.SimpleAuthObject;
import com.cu.framework.auth.rbac.mp.entity.SysMenuApi;
import com.cu.framework.basic.utils.BeanUtil;
import com.cu.framework.basic.utils.TreeUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


/**
 * API 菜单及其权限集合体的响应对象，继承 SysMenuApi 全部字段，并携带树形子节点与菜单权限对象
 * @author kewen
 * @since 2026-02-12
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MenuApiAndAuthResp extends SysMenuApi implements TreeUtil.TreeBase<MenuApiAndAuthResp,Long>{

    /** 子菜单节点 */
    protected List<MenuApiAndAuthResp> children;

    /** 该菜单挂载的权限集合体，由 sys_auth_menu 中该菜单的权限记录组装 */
    private SimpleAuthObject authObject;

    /**
     * 由 SysMenuApi 实体转换（JSON 序列化再反序列化）为响应对象
     * @param sysMenuRequest 菜单 API 实体
     * @return 响应对象
     */
    public static MenuApiAndAuthResp of(SysMenuApi sysMenuRequest) {
        return BeanUtil.toBean(sysMenuRequest, MenuApiAndAuthResp.class);
    }

}
