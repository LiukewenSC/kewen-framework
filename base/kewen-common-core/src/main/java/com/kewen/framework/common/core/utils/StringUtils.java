package com.kewen.framework.common.core.utils;
/**
 * 
 * @author kewen
 * @since 2023-04-26
 */
public class StringUtils {

    public static String strCompact(byte[] bytes){
        String s = new String(bytes);
        s = s.replace("\\s","");
        s = s.replace("\n","");
        s = s.replace("\t","");
        s = s.replace("\r","");
        s = s.replace(" ","");
        return s;
    }
}
