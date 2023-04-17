package com.kewen.framework.boot.jdbc.config;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户上下文
 *
 * @author liukewen
 * @since 2022/9/1
 */

@ConfigurationProperties()
public class DbTenantProperties {

    public Boolean open;
    String tenantIdColumn = "tenant_id";
    String ignorePrefixTables ;

    public Boolean getOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public String getTenantIdColumn() {
        return tenantIdColumn;
    }

    public void setTenantIdColumn(String tenantIdColumn) {
        this.tenantIdColumn = tenantIdColumn;
    }

    public void setIgnorePrefixTables(String ignorePrefixTables) {
        this.ignorePrefixTables = ignorePrefixTables;
    }

    public List<String> getIgnorePrefixTables() {
        if (ignorePrefixTables==null){
            return Collections.emptyList();
        }
        String[] strings = StringUtils.tokenizeToStringArray(ignorePrefixTables, ",;");
        return Arrays.stream(strings).collect(Collectors.toList());
    }
}
