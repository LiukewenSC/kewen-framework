package com.cu.framework.sample.authcore.entity;

import lombok.Data;

/**
 * 示例业务表 demo_asset 的行。主键列名故意不是 id，用于验证 dataIdColumn 声明
 *
 * @author kewen
 * @since 2026-10-20
 */
@Data
public class Asset {

    /** 主键，列名 asset_id */
    private Long assetId;
    /** 资产名称 */
    private String name;
    /** 所属部门 */
    private String dept;
}
