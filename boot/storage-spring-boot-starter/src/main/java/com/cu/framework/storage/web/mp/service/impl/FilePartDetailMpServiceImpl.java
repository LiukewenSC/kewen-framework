package com.cu.framework.storage.web.mp.service.impl;

import com.cu.framework.storage.web.mp.entity.FilePartDetail;
import com.cu.framework.storage.web.mp.mapper.FilePartDetailMpMapper;
import com.cu.framework.storage.web.mp.service.FilePartDetailMpService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文件分片信息表，仅在手动分片上传时使用 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2024-09-30
 */
@Service
public class FilePartDetailMpServiceImpl extends ServiceImpl<FilePartDetailMpMapper, FilePartDetail> implements FilePartDetailMpService {

}
