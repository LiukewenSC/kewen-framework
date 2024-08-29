package com.kewen.framework.storage.web.mp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author kewen
 * @since 2023-06-20
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_storage_module")
public class SysStorageModule extends Model<SysStorageModule> {

    private static final long serialVersionUID = 1L;

    /**
     * 模块名
     */
    @TableId(value = "module_name", type = IdType.AUTO)
    private String moduleName;

    /**
     * 相对存储路径/存储文件夹
     */
    @TableField("relative_directory")
    private String relativeDirectory;


    @Override
    public Serializable pkVal() {
        return this.moduleName;
    }

}
