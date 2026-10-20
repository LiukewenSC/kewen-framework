package com.cu.framework.auth.core.data;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 数据范围数据库字段定义
 * @author kewen
 * @since 2023-12-04
 */
@Data
@Accessors(chain = true)
public class AuthDataDO {

    /**
     * 数据权限记录的主键ID。
     * 由查询按列的实际类型读回，删除时原样绑定，不参与业务数据ID的类型判断
     */
    private Object id;
    /**
     * 业务功能
     */
    private String businessFunction;
    /**
     * 业务ID存储，取自查询结果，类型随业务主键
     */
    private Object dataId;
    /**
     * 操作
     */
    private String operate;


    /**
     * 权限存储
     */
    private String authority;
    /**
     * 权限描述
     */
    private String description;

    public AuthDataDO(String businessFunction, Object dataId, String operate, String authority, String description) {
        this.businessFunction = businessFunction;
        this.dataId = dataId;
        this.operate = operate;
        this.authority = authority;
        this.description = description;
    }
    public AuthDataDO(Object id, String businessFunction, Object dataId, String operate, String authority, String description) {
        this(businessFunction, dataId, operate, authority, description);
        this.id = id;
    }
}
