package com.kewen.framework.storage.model;

import com.kewen.framework.storage.boot.FileResponseBodyAdvance;

/**
 *
 * 文件填充，实现了此接口的在Result返回的时候会补齐文件信息
 * {@link  FileResponseBodyAdvance}
 * @author kewen
 * @since 2023-04-28
 */
public interface FileFillSupport {

    Long getFileId();

    void setFileInfo(FileInfo fileInfo);

}
