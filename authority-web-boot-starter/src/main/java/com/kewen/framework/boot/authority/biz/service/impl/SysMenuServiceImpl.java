package com.kewen.framework.boot.authority.biz.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kewen.framework.boot.authority.biz.mapper.SysMenuMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysMenu;
import com.kewen.framework.boot.authority.biz.service.SysMenuService;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {


    @Override
    public boolean hasAuth(Collection<String> authorities, String url) {
        Integer auth = baseMapper.hasAuth(authorities, url);
        return auth != null && auth > 0;
    }
}

