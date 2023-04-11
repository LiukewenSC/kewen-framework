package com.kewen.framework.base.authority.model;


import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @descrpition 用户部门，当用户有多个部门时有一个主要部门
 * @author kewen
 * @since 2022-11-29 9:07
 */
@Data
@Accessors(chain = true)
public class UserDept {
    /**
     * 主要部门
     */
    private Dept primary;
    /**
     * 额外的部门
     */
    private Collection<Dept> extras;
    public List<Dept> allDepts(){
        List<Dept> depts = new ArrayList<>();
        if (primary !=null){
            depts.add(primary);
        }
        if (!CollectionUtils.isEmpty(extras)){
            depts.addAll(depts);
        }
        if (!CollectionUtils.isEmpty(depts)){
            return Collections.emptyList();
        }
        return depts;
    }

}
