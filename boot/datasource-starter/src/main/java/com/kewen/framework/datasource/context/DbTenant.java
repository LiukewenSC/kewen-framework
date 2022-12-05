package com.kewen.framework.datasource.context;

import java.util.Collections;
import java.util.List;

/**
 * 租户上下文
 *
 * @author liukewen
 * @since 2022/9/1
 */
public interface DbTenant {

    /**
     * 获取租户id
     * @return
     */
    Long getTenantId();

    /**
     * 获取租户数据库列名
     * @return
     */
    default String getTenantIdColumn(){
        return "tenant_id";
    }

    /**
     * 排除表名前缀
     * @return
     */
    default List<String> ignorePrefixTables(){
        return Collections.singletonList("uucs_tenant");
    }
}
