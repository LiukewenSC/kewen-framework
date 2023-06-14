package com.kewen.framework.boot.auth.security.annotation;

import java.lang.annotation.*;

/**
 * 允许放行的url
 * @author kewen
 * @since 2023-06-14
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface SecurityIgnore {

}
