package com.kewen.framework.storage.web.mp.mapper;

import com.kewen.framework.storage.web.mp.entity.FilePartDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 文件分片信息表，仅在手动分片上传时使用 Mapper 接口
 * </p>
 *
 * @author kewen
 * @since 2024-09-30
 */
@Mapper
public interface FilePartDetailMpMapper extends BaseMapper<FilePartDetail> {

}
