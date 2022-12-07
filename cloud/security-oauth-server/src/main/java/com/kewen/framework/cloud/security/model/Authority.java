package com.kewen.framework.cloud.security.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author kewen
 * @descrpition
 * @since 2022-12-07 17:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Authority implements GrantedAuthority {
    private String authority;
    private String description;
}
