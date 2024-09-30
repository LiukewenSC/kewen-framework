package com.kewen.framework.storage.web.mp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 文件存储
 * </p>
 *
 * @author kewen
 * @since 2023-06-20
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_storage_file")
public class SysStorageFile extends Model<SysStorageFile> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 存储的key值
     */
    @TableField("file_key")
    private String fileKey;

    /**
     * 文件类型
     */
    @TableField("mime_type")
    private String mimeType;

    /**
     * 文件大小
     */
    @TableField("size")
    private Long size;


    /**
     * 0-等待上传 1-上传中 2-完成 3-失败
     */
    @TableField("status")
    private Integer status;


    @Override
    public Serializable pkVal() {
        return this.id;
    }

}
