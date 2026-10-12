package com.cu.framework.auth.rbac.model.resp;

import com.cu.framework.auth.rbac.composite.model.SimpleAuthObject;
import com.cu.framework.auth.rbac.mp.entity.SysMenuApi;
import com.cu.framework.auth.rbac.utils.BeanUtil;
import com.cu.framework.auth.rbac.utils.TreeUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
public class MenuApiAndAuthResp extends SysMenuApi implements TreeUtil.TreeBase<MenuApiAndAuthResp,Long>{

    protected List<MenuApiAndAuthResp> children;

    private SimpleAuthObject authObject;

    public static MenuApiAndAuthResp of(SysMenuApi sysMenuRequest) {
        return BeanUtil.toBean(sysMenuRequest, MenuApiAndAuthResp.class);
    }

}
