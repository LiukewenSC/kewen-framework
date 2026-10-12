package com.cu.framework.auth.rbac.model.resp;



import com.cu.framework.auth.rbac.mp.entity.SysMenuRoute;
import com.cu.framework.auth.rbac.utils.BeanUtil;
import com.cu.framework.auth.rbac.utils.TreeUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @descrpition
 * @author kewen
 * @since 2022-12-01 10:44
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MenuRouteResp extends SysMenuRoute implements TreeUtil.TreeBase<MenuRouteResp,Long>{

    /** 子路由节点 */
    protected List<MenuRouteResp> children;

    /**
     * 由 SysMenuRoute 实体转换（JSON 序列化再反序列化）为路由响应对象
     * @param sysMenuRoute 菜单路由实体
     * @return 路由响应对象
     */
    public static MenuRouteResp from(SysMenuRoute sysMenuRoute) {
        return BeanUtil.toBean(sysMenuRoute, MenuRouteResp.class);
    }

}
