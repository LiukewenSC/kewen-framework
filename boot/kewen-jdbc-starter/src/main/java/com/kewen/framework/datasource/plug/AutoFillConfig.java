package com.kewen.framework.datasource.plug;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.kewen.framework.boot.common.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * 全局自动填充插件
 *      自动填充插件必须在entity上配置对应的 填充申明，如
 *      @TableField(value = "create_time",fill = FieldFill.INSERT)
 *      private LocalDateTime createTime;
 * @author liukewen
 * @since 2022/9/1
 */
@Slf4j
public class AutoFillConfig implements MetaObjectHandler {

    /**
     * 是否支持填充用户
     */
    private final boolean isSupportFillUser;

    public AutoFillConfig(boolean isSupportFillUser) {
        this.isSupportFillUser = isSupportFillUser;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        if (isSupportFillUser){
            this.strictInsertFill(metaObject, "createUserId", Long.class, UserContext.get().getUserId());
            this.strictInsertFill(metaObject, "createUserName", String.class, UserContext.get().getUserName());
        }

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}