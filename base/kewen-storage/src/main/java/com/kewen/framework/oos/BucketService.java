package com.kewen.framework.oos;

import com.qiniu.common.QiniuException;

/**
 * @author kewen
 * @descrpition
 * @since 2023-03-30
 */
public interface BucketService {

    void createBucket(String bucket) throws QiniuException;

}
