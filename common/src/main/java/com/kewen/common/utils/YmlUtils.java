package com.kewen.common.utils;

import org.springframework.util.ResourceUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-11-30 15:58
 */
public class YmlUtils {

    /**
     * 解析 .yml文件得到对应的属性值
     * @param filePath
     * @param name
     * @param clazz  String.class 或者 Map.class
     * @param <T>
     * @return
     */
    public static <T> T parse(String filePath,String name,Class<T> clazz){
        String[] keys = name.split("\\.");
        InputStream in = null;
        try {
            File file = ResourceUtils.getFile(filePath);
            in = new BufferedInputStream(new FileInputStream(file));
            Yaml props = new Yaml();
            Map map = props.loadAs(in, Map.class);

            return MapUtil.getValue(map,name,clazz);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            if (in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}