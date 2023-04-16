package com.kewen.framework.base.authority.annotation;

import com.kewen.framework.base.auth.annotation.dataedit.ApplicationBusiness;
import lombok.Data;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-11-25 10:36
 */
@Data
public class DefaultApplicationBusiness implements ApplicationBusiness {

    private Long businessId;
    @Override
    public Long getBusinessId() {
        return businessId;
    }
}
