package com.cu.framework.auth.rbac.composite.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.cu.framework.auth.rbac.model.req.MenuApiSaveReq;
import com.cu.framework.auth.rbac.mp.entity.SysMenuApi;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单更新接口的字段白名单测试：请求路径是鉴权身份，不进 SET。
 *
 * @author kewen
 * @since 2026-10-20
 */
public class MenuApiUpdatableColumnsTest {

    @BeforeClass
    public static void initTableInfo() {
        // 让 Lambda 列名解析可用，不依赖 MyBatis 会话
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SysMenuApi.class);
    }

    @Test
    public void whitelistHoldsExactlyTheUpdatableColumns() {
        Set<String> getters = DefaultSysAuthMenuComposite.UPDATABLE_COLUMNS.stream()
                .map(column -> LambdaUtils.extract(column).getImplMethodName())
                .collect(Collectors.toSet());
        Assert.assertEquals(4, DefaultSysAuthMenuComposite.UPDATABLE_COLUMNS.size());
        Assert.assertEquals(
                new HashSet<>(Arrays.asList("getName", "getRemark", "getAuthType", "getParentId")),
                getters);
        Assert.assertFalse("请求路径不得出现在白名单里", getters.contains("getPath"));
    }

    @Test
    public void sqlSetCarriesWhitelistedColumnsAndNeverTheRequestPath() {
        MenuApiSaveReq req = new MenuApiSaveReq();
        req.setId(1L);
        req.setPath("/hijacked/path");
        req.setName("菜单名");
        req.setRemark("备注");
        req.setAuthType(1);
        req.setParentId(0L);

        String sqlSet = DefaultSysAuthMenuComposite.buildUpdatableWrapper(req).getSqlSet();

        Assert.assertTrue(sqlSet, sqlSet.contains("name="));
        Assert.assertTrue(sqlSet, sqlSet.contains("remark="));
        Assert.assertTrue(sqlSet, sqlSet.contains("auth_type="));
        Assert.assertTrue(sqlSet, sqlSet.contains("parent_id="));
        Assert.assertFalse("请求路径不得进 SET: " + sqlSet, sqlSet.contains("path="));
    }

    @Test
    public void nullValuedColumnsAreNotWritten() {
        MenuApiSaveReq req = new MenuApiSaveReq();
        req.setId(1L);
        req.setPath("/hijacked/path");
        req.setRemark("只改备注");

        LambdaUpdateWrapper<SysMenuApi> wrapper = DefaultSysAuthMenuComposite.buildUpdatableWrapper(req);

        Assert.assertTrue(wrapper.getSqlSet().contains("remark="));
        Assert.assertFalse("值为 null 的字段应保持不更新", wrapper.getSqlSet().contains("name="));
        Assert.assertFalse(wrapper.getSqlSet().contains("path="));
    }
}
