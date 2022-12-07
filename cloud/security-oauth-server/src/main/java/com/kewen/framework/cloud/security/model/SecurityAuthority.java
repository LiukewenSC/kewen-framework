package com.kewen.framework.cloud.security.model;

import com.kewen.framework.base.authority.model.Authority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author kewen
 * @descrpition
 * @since 2022-12-07 17:15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SecurityAuthority extends Authority implements GrantedAuthority {

    public SecurityAuthority(String authority, String description) {
        super(authority, description);
    }

    public SecurityAuthority() {
    }
}
