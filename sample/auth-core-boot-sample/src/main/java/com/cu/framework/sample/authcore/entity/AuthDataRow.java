package com.cu.framework.sample.authcore.entity;

import lombok.Data;

/**
 * 权限槽位表 demo_auth_data 的读回行，只用于观察，不参与框架读写
 *
 * @author kewen
 * @since 2026-10-20
 */
@Data
public class AuthDataRow {

    /** 主键 */
    private Long pk;
    /** 业务功能 */
    private String bizModule;
    /** 业务数据ID */
    private Long bizDataId;
    /** 操作类型 */
    private String action;
    /** 权限字符串 */
    private String authStr;
    /** 权限描述 */
    private String authDesc;
}
