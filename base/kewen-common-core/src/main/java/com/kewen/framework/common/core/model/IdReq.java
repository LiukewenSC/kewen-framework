package com.kewen.framework.common.core.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author kewen
 * @descrpition
 * @since 2023-04-06
 */
@Data
public class IdReq {
    @NotNull
    protected Long id;
}
