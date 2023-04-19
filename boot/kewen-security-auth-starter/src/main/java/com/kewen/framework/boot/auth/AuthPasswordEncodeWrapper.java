package com.kewen.framework.boot.auth;

import com.kewen.framework.auth.core.model.AuthPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 
 * @author kewen
 * @since 2023-04-19
 */
public class AuthPasswordEncodeWrapper implements AuthPasswordEncoder, PasswordEncoder {

    private final PasswordEncoder wrapper;

    public AuthPasswordEncodeWrapper(PasswordEncoder passwordEncoder) {
        this.wrapper = passwordEncoder;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return wrapper.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return wrapper.matches(rawPassword, encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return wrapper.upgradeEncoding(encodedPassword);
    }
}
