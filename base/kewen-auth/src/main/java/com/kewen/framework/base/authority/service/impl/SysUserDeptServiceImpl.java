package com.kewen.framework.base.authority.service.impl;

import com.kewen.framework.base.authority.entity.SysUserDept;
import com.kewen.framework.base.authority.mapper.SysUserDeptMapper;
import com.kewen.framework.base.authority.service.SysUserDeptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kewen.framework.base.common.model.Dept;
import com.kewen.framework.base.common.model.DeptPrimary;
import com.kewen.framework.base.common.model.UserDept;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * <p>
 * 用户部门关联表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserDeptServiceImpl extends ServiceImpl<SysUserDeptMapper, SysUserDept> implements SysUserDeptService {

    @Override
    public Page<SysUserDept> pageQuery(Integer pageNo, Integer pageSize, SysUserDept queryModel) {
        Page<SysUserDept> page = new Page<>();
        Wrapper<SysUserDept> wrapper = new LambdaQueryWrapper<SysUserDept>()
                .orderByDesc(SysUserDept::getUpdateTime);
        return this.page(page,wrapper);
    }

    @Override
    public List<SysUserDept> findList(SysUserDept queryModel) {
        Wrapper<SysUserDept> wrapper = new LambdaQueryWrapper<SysUserDept>()
                .orderByDesc(SysUserDept::getUpdateTime);
        return this.list(wrapper);
    }

    @Override
    public UserDept getUserDept(Integer userId) {
        List<DeptPrimary> depts = baseMapper.listUserDept(userId);

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

}
