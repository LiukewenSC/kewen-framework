package com.cu.framework.basic.utils;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author kewen
 * @descrpition 正则表达式工具
 * @since 2023-11-15 9:33
 */
public class RegUtil {

    /** 电子邮箱地址正则 */
    public static final String EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    /** 手机号正则，1 开头、第二位为 3/4/5/7/8，共 11 位数字 */
    public static final String PHONE = "^1[34578]\\d{9}$";
    /** 18 位身份证号正则，出生年份限 18xx/19xx/20xx，末位可为 X/x */
    public static final String ID_CARD = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";
    /** IPv4 地址正则，每段 0-255 */
    public static final String IP_ADDR = "^((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)$";
    /** URL 正则，仅匹配 http:// 开头的地址 */
    public static final String URL = "^http://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$";
    /** QQ 号正则，不以 0 开头的 5-11 位数字 */
    public static final String QQ = "^[1-9]\\d{4,10}$";
    /** 手机号正则，按号段限制：13x、145/147、15x（不含 154）、18x，共 11 位数字 */
    public static final String MOBILE = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8}$";
    /** 邮政编码正则，不以 0 开头的 6 位数字 */
    public static final String ZIP_CODE = "^[1-9]\\d{5}$";
    public static final String CHINESE = "^[\u4e00-\u9fa5],{0,}$";
    /** 仅由英文字母组成的正则 */
    public static final String ENGLISH = "^[A-Za-z]+$";
    /** 仅由数字组成的正则 */
    public static final String NUMBER = "^[0-9]+$";
    /** 可带正负号的整数正则 */
    public static final String INTEGER = "^[-\\+]?[\\d]*$";
    /** 可带正负号、可含小数点的数值正则 */
    public static final String FLOAT = "^[-\\+]?[.\\d]*$";
    /** 可带正负号、可含小数点的数值正则，与 FLOAT 相同 */
    public static final String DOUBLE = "^[-\\+]?[.\\d]*$";
    public static final String SPECIAL_CHAR = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w]+$";
    public static final String SPECIAL_CHAR_NO_EN = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w]+$";
    public static final String SPECIAL_CHAR_NO_CN = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w]+$";
    public static final String SPECIAL_CHAR_NO_EN_CN = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w]+$";
    public static final String SPECIAL_CHAR_NO_EN_CN_NO_NUM = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w]+$";
    public static final String SPECIAL_CHAR_NO_EN_CN_NO_NUM_NO_SPECIAL = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w]+$";
    public static final String SPECIAL_CHAR_NO_EN_CN_NO_NUM_NO_SPECIAL_NO_SPACE = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w]+$";
    public static final String SPECIAL_CHAR_NO_EN_CN_NO_NUM_NO_SPECIAL_NO_SPACE_NO_DASH = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w]+$";

    /**
     * MARKDOWN 图片匹配表达式
     */
    public static final String MARKDOWN_IMAGE = "!\\[(.*?)\\]\\((.*?)\\)";

    /**
     * 判断字符串是否整体匹配给定正则（基于 String.matches）
     * @param regex 正则表达式
     * @param str 待匹配字符串
     * @return 整体匹配时返回 true
     */
    public static boolean isMatch(String regex, String str) {
        return str.matches(regex);
    }

    /**
     * 从Markdown文本中提取图片标签并返回图片链接列表
     * @param markdown Markdown文本
     * @return 图片链接列表
     */
    public static List<MarkdownImage> extractMarkdownImages(String markdown) {
        Pattern compile = Pattern.compile(MARKDOWN_IMAGE);
        Matcher matcher = compile.matcher(markdown);
        ArrayList<MarkdownImage> markdownImages = new ArrayList<>();
        while (matcher.find()) {
            if (matcher.groupCount() > 0) {
                String original = matcher.group(0);
                String name = matcher.group(1);
                String url = matcher.group(2);
                MarkdownImage markdownImage = new MarkdownImage(original,name,url);
                markdownImages.add(markdownImage);
            }
        }
        return markdownImages;
    }


    /**
     * @author kewen
     * @descrpition markdown图片解析后的地址
     * @since 2023-11-15 9:59
     */
    @Getter
    public static class MarkdownImage {
        public MarkdownImage(String original, String name, String url) {
            this.original = original;
            this.name = name;
            this.url = url;
        }

        /**
         * 原始地址 ![name](url) 格式
         */
        private String original;
        /**
         * 图片名称
         */
        private String name;
        /**
         * 图片地址
         */
        private String url;

        @Override
        public String toString() {
            return "MarkdownImage{" +
                    "original='" + original + '\'' +
                    ", name='" + name + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }
}
