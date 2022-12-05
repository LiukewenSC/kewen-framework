package com.kewen.framework.boot.authority.biz.model.resp;


import com.kewen.framework.boot.authority.biz.mapper.entity.SysMenuAuth;
import com.kewen.common.utils.TreeUtil;
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
public class MenuResp extends MenuRespBase implements TreeUtil.TreeBase<MenuResp,Integer>{

    private MenuRespBase parent;

    private List<SysMenuAuth> auths;

    private List<MenuResp> subs;

    @Override
    public void setSubs(List<MenuResp> subs) {
        this.subs = subs;
    }
}
