package com.kewen.framework.common.core.model;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author kewen
 * @descrpition
 * @since 2023-04-06
 */
@Data
public class IdsReq {

    @NotEmpty
    protected List<Long> ids;
}
