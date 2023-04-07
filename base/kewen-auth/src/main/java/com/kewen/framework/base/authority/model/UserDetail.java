package com.kewen.framework.base.authority.model;


import com.kewen.framework.base.common.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @descrpition 用户详情，包含岗位权限等
 * @author kewen
 * @since 2022-11-25 15:17
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetail implements IUser {
    protected User user;

    protected UserDept dept;
    protected Collection<Position> positions;
    protected Collection<Role> roles;


    public List<Dept> getDepts(){
        if (dept==null) {
            return Collections.emptyList();
        }
        Dept primary = dept.getPrimary();
        Collection<Dept> extras = dept.getExtras();
        ArrayList<Dept> depts = new ArrayList<>();
        depts.add(primary);
        if (!CollectionUtils.isEmpty(extras)){
            depts.addAll(extras);
        }
        return depts;
    }

    @Override
    public Long getUserId() {
        return user.getUserId();
    }

    @Override
    public String getUserName() {
        return user.getUserName();
    }
}
