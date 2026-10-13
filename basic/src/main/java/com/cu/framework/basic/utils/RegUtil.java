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
    /** 手机号正则，11 位、1 开头、第二位 3-9，覆盖当前及后续在用的全部号段 */
    public static final String PHONE = "^1[3-9]\\d{9}$";
    /** 18 位身份证号正则，出生年份限 18xx/19xx/20xx，末位可为 X/x */
    public static final String ID_CARD = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";
    /** IPv4 地址正则，每段 0-255 */
    public static final String IP_ADDR = "^((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)$";
    /** URL 正则，仅匹配 http:// 开头的地址 */
    public static final String URL = "^http://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$";
    /** QQ 号正则，不以 0 开头的 5-11 位数字 */
    public static final String QQ = "^[1-9]\\d{4,10}$";
    /** 手机号正则，与 PHONE 同义；保留该名称只为兼容历史叫法，不再单独维护一份表达式 */
    public static final String MOBILE = PHONE;
    /** 邮政编码正则，不以 0 开头的 6 位数字 */
    public static final String ZIP_CODE = "^[1-9]\\d{5}$";
    /** 仅由中文汉字（基本区）组成的正则；原实现把量词写成了 `,{0,}`，会额外放行结尾的英文逗号 */
    public static final String CHINESE = "^[\\u4e00-\\u9fa5]+$";
    /** 仅由英文字母组成的正则 */
    public static final String ENGLISH = "^[A-Za-z]+$";
    /** 仅由数字组成的正则 */
    public static final String NUMBER = "^[0-9]+$";
    /** 可带正负号的整数正则 */
    public static final String INTEGER = "^[-\\+]?[\\d]*$";
    /** 可带正负号、可含小数点的数值正则 */
    public static final String FLOAT = "^[-\\+]?[.\\d]*$";
    /** 可带正负号、可含小数点的数值正则；与 FLOAT 同义，保留该名称只为兼容历史叫法 */
    public static final String DOUBLE = FLOAT;
    /**
     * 允许汉字、CJK 兼容表意文字、英文字母、数字与下划线，不允许任何标点与空白字符。
     * <p>名称中的 SPECIAL_CHAR 并不表示"允许特殊字符"，实际匹配范围以上述为准。
     * <p>此集合原有 8 个按 NO_EN / NO_CN / NO_NUM 等名字暗示逐级排除的常量，
     * 但它们的正则逐字节相同、从未做过任何排除，仓内引用数为 0，已全部删除；
     * 需要真正的字符集排除规则时应另写表达式，而不是复用这些名字。
     */
    public static final String SPECIAL_CHAR = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w]+$";

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
