package com.cu.framework.basic.utils;



import com.cu.framework.basic.exception.BizException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

/**
 * @author LiuKewen
 * @description
 * @date 2022/2/8 19:12
 */
public class FileUtils {

    private FileUtils(){}

    /**
     * 递归创建文件夹，并设置777
     *
     * @param file
     * @throws IOException
     */
    public void createDirForLoop(File file) throws BizException, IOException {
        createDirForLoop(file, chmod777());
    }

    /**
     * 递归创建 file 自身及其所有缺失的父级文件夹，并为每层目录设置指定的 POSIX 权限
     * @param file 待创建的文件夹
     * @param permissions 需要设置的 POSIX 文件权限集合
     * @throws BizException 文件夹创建失败（mkdir 返回 false）时抛出
     * @throws IOException 设置 POSIX 权限失败时抛出
     */
    public void createDirForLoop(File file, Set<PosixFilePermission> permissions) throws BizException, IOException {
        createParentDirWithPermission(file, permissions);
    }

    /**
     * 构建 777 权限集合，即所有者/组/其他用户均具备读、写、执行权限
     * @return 包含全部九项 POSIX 权限的集合
     */
    private Set<PosixFilePermission> chmod777() {
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_WRITE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_WRITE);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);
        return perms;
    }

    /**
     * 创建文件夹，若创建失败则报错
     *
     * @param file
     * @param perms
     * @throws IOException
     */
    private void createParentDirWithPermission(File file, Set<PosixFilePermission> perms) throws BizException, IOException {
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {

            //递归创建父级文件夹并设置权限
            createParentDirWithPermission(parentFile, perms);
        }
        if (!file.exists() && !file.mkdir()) {
            throw new BizException(String.format(
                    "创建父级文件夹失败path:%s，mkdir()==false", parentFile.getAbsolutePath()));
        }
        Path parentPath = Paths.get(file.getAbsolutePath());
        Files.setPosixFilePermissions(parentPath, perms);

    }

}
