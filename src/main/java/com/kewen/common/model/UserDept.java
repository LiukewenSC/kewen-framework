package com.kewen.common.model;


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
public class UserDept {
    /**
     * 主要部门
     */
    private Dept primary;
    /**
     * 额外的部门
     */
    private Collection<Dept> extras;

    public UserDept() {
    }

    public UserDept(Dept primary, Collection<Dept> extras) {
        this.primary = primary;
        this.extras = extras;
    }

    public Dept getPrimary() {
        return primary;
    }

    public void setPrimary(Dept primary) {
        this.primary = primary;
    }

    public Collection<Dept> getExtras() {
        return extras;
    }

    public void setExtras(Collection<Dept> extras) {
        this.extras = extras;
    }
    public Collection<Dept> getDepts(){
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
