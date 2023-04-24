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
 * 文件存储
 * </p>
 *
 * @author kewen
 * @since 2023-04-24
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_file_storage")
public class SysFileStorage extends Model<SysFileStorage> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 对象存储名
     */
    @TableField("storage_name")
    private String storageName;
    /**
     * 存储地址
     */
    @TableField("path")
    private String path;
    /**
     * 文件类型
     */
    @TableField("file_type")
    private String fileType;
    /**
     * 存储类型
     */
    @TableField("mime_type")
    private String mimeType;

    /**
     * 文件大小
     */
    @TableField("size")
    private Long size;


    @Override
    public Serializable pkVal() {
        return this.id;
    }

}
