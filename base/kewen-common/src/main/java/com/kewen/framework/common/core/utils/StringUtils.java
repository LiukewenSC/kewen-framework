package com.kewen.framework.common.core.utils;
/**
 * 
 * @author kewen
 * @since 2023-04-26
 */
public class StringUtils {

        /**
     * 将字节数组转换为字符串，并去除空白字符和换行符
     * @param bytes 字节数组
     * @return 去除空白字符和换行符后的字符串
     */
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
