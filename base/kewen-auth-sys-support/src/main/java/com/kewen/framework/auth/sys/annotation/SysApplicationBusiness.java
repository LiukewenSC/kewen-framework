package com.kewen.framework.auth.sys.annotation;

import com.kewen.framework.auth.core.annotation.dataedit.ApplicationBusiness;
import lombok.Data;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-11-25 10:36
 */
@Data
public class SysApplicationBusiness implements ApplicationBusiness {

    private Long businessId;
    @Override
    public Long getBusinessId() {
        return businessId;
    }
}
