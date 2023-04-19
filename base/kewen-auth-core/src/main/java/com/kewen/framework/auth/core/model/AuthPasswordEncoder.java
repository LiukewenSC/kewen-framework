package com.kewen.framework.auth.core.model;
/**
 * 
 * @author kewen
 * @since 2023-04-19
 */
public interface AuthPasswordEncoder {
    String encode(CharSequence rawPassword);

    boolean matches(CharSequence rawPassword, String encodedPassword);
}
