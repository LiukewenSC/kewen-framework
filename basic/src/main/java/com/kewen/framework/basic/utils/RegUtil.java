package com.kewen.framework.basic.utils;

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

    public static final String EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    public static final String PHONE = "^1[34578]\\d{9}$";
    public static final String ID_CARD = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";
    public static final String IP_ADDR = "^((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)$";
    public static final String URL = "^http://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$";
    public static final String QQ = "^[1-9]\\d{4,10}$";
    public static final String MOBILE = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8}$";
    public static final String ZIP_CODE = "^[1-9]\\d{5}$";
    public static final String CHINESE = "^[\u4e00-\u9fa5],{0,}$";
    public static final String ENGLISH = "^[A-Za-z]+$";
    public static final String NUMBER = "^[0-9]+$";
    public static final String INTEGER = "^[-\\+]?[\\d]*$";
    public static final String FLOAT = "^[-\\+]?[.\\d]*$";
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
