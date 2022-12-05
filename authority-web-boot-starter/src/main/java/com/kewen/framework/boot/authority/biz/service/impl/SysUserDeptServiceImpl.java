package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.common.model.Dept;
import com.kewen.common.model.DeptPrimary;
import com.kewen.common.model.UserDept;
import com.kewen.framework.boot.authority.biz.mapper.SysUserDeptMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysUserDept;
import com.kewen.framework.boot.authority.biz.service.SysUserDeptService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.util.CollectionUtils;

@Service
public class SysUserDeptServiceImpl extends ServiceImpl<SysUserDeptMapper, SysUserDept> implements SysUserDeptService {

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

    @Override
    public int updateBatch(List<SysUserDept> list) {
        return baseMapper.updateBatch(list);
    }
    @Override
    public int updateBatchSelective(List<SysUserDept> list) {
        return baseMapper.updateBatchSelective(list);
    }
    @Override
    public int batchInsert(List<SysUserDept> list) {
        return baseMapper.batchInsert(list);
    }
    @Override
    public int insertOrUpdate(SysUserDept record) {
        return baseMapper.insertOrUpdate(record);
    }
    @Override
    public int insertOrUpdateSelective(SysUserDept record) {
        return baseMapper.insertOrUpdateSelective(record);
    }
}
