package com.kewen.framework.boot.auth;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AuthPasswordEncodeWrapperTest {
    AuthPasswordEncodeWrapper passwordEncode = new AuthPasswordEncodeWrapper(new BCryptPasswordEncoder(10));

    @Test
    public void encode() {
        String encode = passwordEncode.encode("123456");
        System.out.println(encode);
    }

    @Test
    public void matches() {
        System.out.println(passwordEncode.matches("123456", "$2a$10$aSta4ZzWzNFSOzBRewhzwO08xA3g48pLnK1.M0u48FUDsNnIvk/Xi"));
    }
}