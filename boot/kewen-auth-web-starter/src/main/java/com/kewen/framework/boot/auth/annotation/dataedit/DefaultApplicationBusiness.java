package com.kewen.framework.boot.auth.annotation.dataedit;

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
