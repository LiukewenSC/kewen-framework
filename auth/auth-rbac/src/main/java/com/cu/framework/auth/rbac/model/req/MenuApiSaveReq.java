package com.cu.framework.auth.rbac.model.req;

import com.cu.framework.auth.rbac.composite.model.SimpleAuthObject;
import com.cu.framework.auth.rbac.mp.entity.SysMenuApi;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 
 * @author kewen
 * @since 2023-04-21
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MenuApiSaveReq extends SysMenuApi {
    SimpleAuthObject authObject;
}
