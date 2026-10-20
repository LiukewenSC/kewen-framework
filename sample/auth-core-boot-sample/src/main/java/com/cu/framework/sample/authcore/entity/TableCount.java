package com.cu.framework.sample.authcore.entity;

import lombok.Data;

/**
 * 一条"某张表现在有多少行"的观察结果，用于验证种子脚本可重复执行不产生重复行
 *
 * @author kewen
 * @since 2026-10-20
 */
@Data
public class TableCount {

    /** 表名 */
    private String tableName;
    /** 行数 */
    private Integer rowCount;
}
