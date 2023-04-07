package com.kewen.framework.base.authority.model;


import com.kewen.framework.base.common.utils.TreeUtil;
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
public class MenuResp extends MenuRespBase implements TreeUtil.TreeBase<MenuResp,Long>{

    private AuthorityObject authority;

    private List<MenuResp> children;


    @Override
    public void setChildren(List<MenuResp> children) {
        this.children = children;
    }
}
