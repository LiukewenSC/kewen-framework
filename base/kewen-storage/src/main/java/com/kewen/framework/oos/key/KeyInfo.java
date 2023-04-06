package com.kewen.framework.oos.key;

/**
 * @author kewen
 * @descrpition
 * @since 2023-03-30
 */
public interface KeyInfo {

    String accessKey = "qYUhPA6FvZ29dQ4pz1BgLwHaTcV2vt_rD-GoEEFB";
    String secretKey = "eyihg0gLAyHd2j08geDCCWdI0UHOfhxzpjFpKso1";
    String bucketName = "kewen-blog";

    default String getAccessKey(){
        return accessKey;
    }
    default String getSecretKey(){
        return secretKey;
    }
}
