package com.cu.framework.basic.model;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * @author kewen
 * @descrpition
 * @since 2023-04-06
 */
@Data
public class IdReq {
    /** ID 参数，@NotNull 非空校验 */
    @NotNull
    protected Long id;
}
