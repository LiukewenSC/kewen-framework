package com.kewen.framework.datasource.plug;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.kewen.framework.datasource.context.DbCurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * 全局填充插件
 *
 * @author liukewen
 * @since 2022/9/1
 */
@Slf4j
public class GlobalFillConfig implements MetaObjectHandler {

    private final DbCurrentUser dbCurrentUser;

    public GlobalFillConfig(DbCurrentUser dbCurrentUser) {
        this.dbCurrentUser = dbCurrentUser;
    }

    @Override
    public void insertFill(MetaObject metaObject) {

        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createUserId", Long.class, dbCurrentUser.getUserId());
        this.strictInsertFill(metaObject, "createUserName", String.class, dbCurrentUser.getUserName());

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}