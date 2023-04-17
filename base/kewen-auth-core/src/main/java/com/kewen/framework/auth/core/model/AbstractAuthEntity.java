package com.kewen.framework.auth.core.model;
/**
 * 抽象的权限实体
 * @author kewen
 * @since 2023-04-10
 */
public abstract class AbstractAuthEntity implements AuthEntity {

    protected Long id;
    protected String name;

    @Override
    public String getAuth() {
        return getPrefix() + split + id + split + name;
    }
    protected abstract String getPrefix();
}
