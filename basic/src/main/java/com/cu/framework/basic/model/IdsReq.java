package com.cu.framework.basic.model;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author kewen
 * @descrpition
 * @since 2023-04-06
 */
@Data
public class IdsReq {

    /** ID 列表参数，@NotEmpty 非空校验 */
    @NotEmpty
    protected List<Long> ids;
}
