package com.kewen.framework.boot.authority.advance;

/**
 * @descrpition 关联的信息
 * @author kewen
 * @since 2022-11-23 10:55
 */
public interface ApplicationBusiness {
    /**
     * 获取到关联的ID
     * @return 业务关联ID，业务具体事项的主键
     */
    Long getBusinessId();
}
