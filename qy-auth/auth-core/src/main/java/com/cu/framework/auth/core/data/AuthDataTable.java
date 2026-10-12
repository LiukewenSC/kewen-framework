package com.cu.framework.auth.core.data;

import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

/**
 * 数据范围数据库字段定义
 * @author kewen
 * @since 2023-12-04
 */
@Setter
@Accessors(chain = true)
public class AuthDataTable {
    /**
     * 业务关联表名
     */
    private String tableName;
    /**
     * id字段名
     * 删除使用，插入、查询不使用此字段
     */
    private String idColumn;
    /**
     * 业务功能字段名
     */
    private String businessFunctionColumn;
    /**
     * 业务ID存储字段名
     */
    private String dataIdColumn;
    /**
     * 操作字段名
     */
    private String operateColumn;


    /**
     * 权限存储字段名
     */
    private String authorityColumn;
    /**
     * 权限描述字段名
     */
    private String descriptionColumn;

    public AuthDataTable() {
    }

    public AuthDataTable(String tableName, String idColumn, String businessFunctionColumn, String dataIdColumn, String operateColumn, String authorityColumn, String descriptionColumn) {
        this.tableName = tableName;
        this.idColumn = idColumn;
        this.businessFunctionColumn = businessFunctionColumn;
        this.dataIdColumn = dataIdColumn;
        this.operateColumn = operateColumn;
        this.authorityColumn = authorityColumn;
        this.descriptionColumn = descriptionColumn;
    }

    /**
     * 以业务关联表名构造 JSqlParser 表对象
     * @return 权限数据所在的表
     */
    public Table getTable(){
        return new Table(tableName);
    }
    /**
     * 以 id 字段名构造 JSqlParser 列对象
     * @return 主键列
     */
    public Column getIdColumn(){
        return new Column(idColumn);
    }
    /**
     * 以业务功能字段名构造 JSqlParser 列对象
     * @return 业务功能列
     */
    public Column getBusinessFunctionColumn(){
        return new Column(businessFunctionColumn);
    }
    /**
     * 以业务ID字段名构造 JSqlParser 列对象
     * @return 业务ID列
     */
    public Column getDataIdColumn(){
        return new Column(dataIdColumn);
    }
    /**
     * 以操作字段名构造 JSqlParser 列对象
     * @return 操作列
     */
    public Column getOperateColumn(){
        return new Column(operateColumn);
    }
    /**
     * 以权限存储字段名构造 JSqlParser 列对象
     * @return 权限列
     */
    public Column getAuthorityColumn(){
        return new Column(authorityColumn);
    }
    /**
     * 以权限描述字段名构造 JSqlParser 列对象
     * @return 权限描述列
     */
    public Column getDescriptionColumn(){
        return new Column(descriptionColumn);
    }


}
