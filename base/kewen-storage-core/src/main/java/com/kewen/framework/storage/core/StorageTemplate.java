package com.kewen.framework.storage.core;

import com.kewen.framework.storage.core.model.PreUploadTokenBO;
import com.kewen.framework.storage.core.model.UploadBO;

import java.io.InputStream;

/**
 *
 * 文件传输调用接口
 * @author kewen
 * @since 2023-04-23
 */
public interface StorageTemplate {
    UploadBO upload(InputStream stream,String relativeDirectory, String fileName, String mediumType);
    String downloadUrl(String key);

    /**
     * 客户端上传
     * @return
     */
    PreUploadTokenBO createPreUploadToken(String relativeDirectory, String fileName);
}
