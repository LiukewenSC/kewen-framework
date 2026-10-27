package com.cu.framework.auth.rbac.model.resp;



import com.cu.framework.auth.rbac.mp.entity.SysMenuApi;
import com.cu.framework.basic.utils.BeanUtil;
import com.cu.framework.basic.utils.TreeUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @descrpition
 * {@link SysMenuApi}
 * @author kewen
 * @since 2022-12-01 10:44
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MenuApiResp extends SysMenuApi implements TreeUtil.TreeBase<MenuApiResp,Long>{

    /** 子菜单节点 */
    protected List<MenuApiResp> children;

    /**
     * 转换为 SysMenuApi 实体（JSON 序列化再反序列化）
     * @return 菜单 API 实体
     */
    public SysMenuApi toSysMenuRequest() {
        return BeanUtil.toBean(this, SysMenuApi.class);
    }

}
