package com.kewen.framework.base.authority.support.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kewen.framework.base.authority.support.mapper.SysUserCompositeMapper;
import com.kewen.framework.base.authority.model.Authority;
import com.kewen.framework.base.authority.model.AuthorityObject;
import com.kewen.framework.base.authority.mp.entity.SysMenuAuth;
import com.kewen.framework.base.authority.mp.service.SysMenuAuthMpService;
import com.kewen.framework.base.authority.support.SysUserComposite;
import com.kewen.framework.base.authority.utils.AuthorityConvertUtil;
import com.kewen.framework.base.common.model.Dept;
import com.kewen.framework.base.common.model.DeptPrimary;
import com.kewen.framework.base.common.model.Position;
import com.kewen.framework.base.common.model.Role;
import com.kewen.framework.base.common.model.UserDept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @descrpition 系统用户相关联合查询
 * @author kewen
 * @since 2023-04-07
 */
@Service
public class SysUserCompositeImpl implements SysUserComposite {

    @Autowired
    SysUserCompositeMapper userCompositeMapper;

    @Autowired
    SysMenuAuthMpService sysMenuAuthMpService;


    @Override
    public UserDept getUserDept(Long userId) {
        List<DeptPrimary> depts = userCompositeMapper.listUserDept(userId);

        if (depts==null){
            return null;
        }
        boolean hasPrimary=false;
        Dept deptPrimary=null;
        List<Dept> extras = new ArrayList<>();

        Iterator<DeptPrimary> iterator = depts.iterator();
        while (iterator.hasNext()){
            DeptPrimary next = iterator.next();
            if (next.getIsPrimary()){
                hasPrimary = true;
                deptPrimary=next;
                iterator.remove();
            } else {
                extras.add(next);
            }
        }

        //数据为空返回null
        if (deptPrimary == null && extras.size()==0){
            return null;
        }

        //如果没有主要机构，取最后一个，移除原有
        if (!hasPrimary && extras.size()>0){
            deptPrimary = extras.remove(extras.size() - 1);
        }

        UserDept userDept = new UserDept();
        userDept.setPrimary(deptPrimary);
        if (!CollectionUtils.isEmpty(extras)){
            userDept.setExtras(extras);
        }
        return userDept;
    }

    @Override
    public List<Role> listUserRole(Long userId) {
        List<Role> roles = userCompositeMapper.listUserRole(userId);
        return roles==null? Collections.emptyList():roles;
    }

    @Override
    public List<Position> listUserPosition(Long userId) {
        List<Position> positions = userCompositeMapper.listUserPosition(userId);
        return positions==null? Collections.emptyList():positions;
    }

    @Override
    public boolean hasAuth(Collection<String> auths, String module, String operate, Long businessId) {
        Integer integer = userCompositeMapper.hasAuth(auths, module, operate, businessId);
        return integer > 0;
    }

    @Override
    public void editMenuAuthorities(Long menuId, AuthorityObject authorityObject) {
        List<Authority> to = AuthorityConvertUtil.to(authorityObject);
        //移除原有的
        sysMenuAuthMpService.remove(
                new LambdaQueryWrapper<SysMenuAuth>().eq(SysMenuAuth::getMenuId,menuId)
        );
        //批量插入新的
        if (!CollectionUtils.isEmpty(to)){
            sysMenuAuthMpService.saveBatch(
                    to.stream()
                            .map(a->
                                    new SysMenuAuth()
                                            .setMenuId(menuId)
                                            .setAuthority(a.getAuthority())
                                            .setDescription(a.getDescription())
                            ).collect(Collectors.toList())
            );
        }
    }

}
