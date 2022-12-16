package com.kewen.framework.base.common.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @descrpition 用户
 * @author kewen
 * @since 2022-11-23 10:32
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements IUser {
    protected Integer id;
    protected String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public Integer getUserId() {
        return id;
    }

    @Override
    public String getUserName() {
        return name;
    }
}
