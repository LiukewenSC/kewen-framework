package com.cu.framework.auth.core.data.range;

import com.cu.framework.auth.core.entity.FlatteningAuthority;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * 范围查询定义
 * @author kewen
 * @since 2024-08-08
 */
@Data
@Accessors(chain = true)
class AuthRange {
    /**
     * 应用/模块标识
     */
    private String businessFunction;
    /**
     * 操作 unified  modify update 等
     */
    private String operate;
    /**
     * 表名，在多个表联查的时候指定哪一个表执行
     */
    private String table;
    /**
     * 表别名
     */
    private String tableAlias;
    /**
     * 业务主键名 一般为 id
     */
    private String dataIdColumn;

    /**
     * 用户权限字符串
     */
    private Collection<FlatteningAuthority> authorities;


    /**
     * 权限匹配方式， IN或者exists
     */
    private MatchMethod matchMethod;

    /**
     * 复制出一份不含权限集合的声明副本。
     * 缓存中保存的是多条语句共用的声明模板，执行期必须先复制再写入当次请求的权限集合，
     * 否则会污染缓存、把权限范围跨请求共享。
     * @return 仅含注解静态信息的新实例
     */
    AuthRange copy() {
        return new AuthRange()
                .setBusinessFunction(businessFunction)
                .setOperate(operate)
                .setTable(table)
                .setTableAlias(tableAlias)
                .setDataIdColumn(dataIdColumn)
                .setMatchMethod(matchMethod);
    }
}
