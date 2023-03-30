package com.kewen.framework.oos;

import com.qiniu.common.QiniuException;

/**
 * @author kewen
 * @descrpition
 * @since 2023-03-30
 */
public interface FileService {
    String upload(String filePath) throws QiniuException;

    String getFilePath(String key) throws QiniuException;
}
