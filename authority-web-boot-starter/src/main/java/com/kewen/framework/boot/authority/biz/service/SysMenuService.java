package com.kewen.framework.boot.authority.biz.service;



import com.baomidou.mybatisplus.extension.service.IService;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysMenu;

import java.util.Collection;

public interface SysMenuService extends IService<SysMenu> {

    boolean hasAuth(Collection<String> authorities, String url);
}

