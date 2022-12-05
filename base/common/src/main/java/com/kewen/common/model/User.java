package com.kewen.common.model;


import java.util.Objects;

/**
 * @descrpition 用户
 * @author kewen
 * @since 2022-11-23 10:32
 */
public class User implements IUser {
    private Long id;
    private String name;

    public User() {
    }

    public User(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
    public Long getUserId() {
        return id;
    }

    @Override
    public String getUserName() {
        return name;
    }
}
