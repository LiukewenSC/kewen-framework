package com.cu.framework.basic.utils;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @descrpition RegUtil 正则常量集合的语义契约
 * @author kewen
 * @since 2026-10-13
 */
public class RegUtilTest {

    private boolean match(String regex, String input) {
        return RegUtil.isMatch(regex, input);
    }

    /**
     * 集合中允许等值的别名对。除此之外不得出现"名称不同、正则相同"的成员，
     * 这正是原 8 个 SPECIAL_CHAR* 常量的问题。
     */
    private static final List<List<String>> SANCTIONED_ALIASES = Arrays.asList(
            Arrays.asList("PHONE", "MOBILE"),
            Arrays.asList("FLOAT", "DOUBLE")
    );

    @Test
    public void noUnexpectedDuplicateConstants() {
        Map<String, List<String>> byValue = new LinkedHashMap<>();
        for (Field field : RegUtil.class.getDeclaredFields()) {
            if (!isPublicStaticFinalString(field)) {
                continue;
            }
            try {
                String value = (String) field.get(null);
                byValue.computeIfAbsent(value, k -> new ArrayList<>()).add(field.getName());
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }

        for (Map.Entry<String, List<String>> entry : byValue.entrySet()) {
            List<String> names = entry.getValue();
            if (names.size() <= 1) {
                continue;
            }
            Assert.assertTrue("常量 " + new TreeSet<>(names) + " 名称不同但正则完全相同，且不属于已认可的别名对",
                    SANCTIONED_ALIASES.contains(new ArrayList<>(names)));
        }
    }

    @Test
    public void aliasesShareOneDefinition() {
        Assert.assertEquals("MOBILE 应与 PHONE 同义", RegUtil.PHONE, RegUtil.MOBILE);
        Assert.assertEquals("DOUBLE 应与 FLOAT 同义", RegUtil.FLOAT, RegUtil.DOUBLE);
    }

    @Test
    public void graduatedExclusionConstantsAreGone() {
        for (Field field : RegUtil.class.getDeclaredFields()) {
            Assert.assertFalse("已删除的假排除常量不应再存在：" + field.getName(),
                    field.getName().startsWith("SPECIAL_CHAR_NO_"));
        }
    }

    @Test
    public void chineseRejectsTrailingCommas() {
        Assert.assertTrue(match(RegUtil.CHINESE, "你好"));
        Assert.assertTrue(match(RegUtil.CHINESE, "中"));
        Assert.assertFalse("量词笔误会额外放行结尾的英文逗号", match(RegUtil.CHINESE, "你好,,,"));
        Assert.assertFalse(match(RegUtil.CHINESE, "你好a"));
        Assert.assertFalse(match(RegUtil.CHINESE, ""));
    }

    @Test
    public void phoneMatchesCurrentSegments() {
        String[] valid = {
                "13800138000", "14512345678", "14912345678", "15812345678",
                "16612345678", "17512345678", "18812345678", "19912345678"
        };
        for (String phone : valid) {
            Assert.assertTrue("应匹配在用号段 " + phone, match(RegUtil.PHONE, phone));
            Assert.assertTrue("MOBILE 应与 PHONE 行为一致 " + phone, match(RegUtil.MOBILE, phone));
        }
    }

    @Test
    public void phoneRejectsMalformedInput() {
        String[] invalid = {
                "18,0123456789",
                "12812345678",
                "1380013800",
                "138001380000",
                "1380013800a"
        };
        for (String phone : invalid) {
            Assert.assertFalse("不应匹配 " + phone, match(RegUtil.PHONE, phone));
            Assert.assertFalse("MOBILE 不应匹配 " + phone, match(RegUtil.MOBILE, phone));
        }
    }

    @Test
    public void specialCharAllowsNoPunctuation() {
        Assert.assertTrue(match(RegUtil.SPECIAL_CHAR, "中文abc123_"));
        Assert.assertFalse("名称不含'允许特殊字符'的语义，标点应被拒绝", match(RegUtil.SPECIAL_CHAR, "abc-def"));
        Assert.assertFalse(match(RegUtil.SPECIAL_CHAR, "abc def"));
        Assert.assertFalse(match(RegUtil.SPECIAL_CHAR, "abc!"));
    }

    private boolean isPublicStaticFinalString(Field field) {
        int mod = field.getModifiers();
        return Modifier.isPublic(mod) && Modifier.isStatic(mod) && Modifier.isFinal(mod)
                && field.getType() == String.class;
    }
}
