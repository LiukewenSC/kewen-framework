package com.kewen.framework.auth.sys.model.req;

import com.kewen.framework.common.core.model.PageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 
 * @author kewen
 * @since 2023-04-19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserPageReq extends PageReq {
    private String name;

}
