package com.kewen.framework.auth.sys.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @descrpition 岗位
 * @author kewen
 * @since 2022-11-23 10:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Position {
    private Long id;
    private String name;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return Objects.equals(id, position.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
