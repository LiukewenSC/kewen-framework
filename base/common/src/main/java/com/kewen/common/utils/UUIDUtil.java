package com.kewen.common.utils;

import java.util.UUID;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-12-05 10:32
 */
public class UUIDUtil {
    public static String generate(){
        return UUID.randomUUID().toString().replace("-", "");
    }
}
