package com.kewen.framework.storage.web.mp.service.impl;

import com.kewen.framework.storage.web.mp.entity.SysFileStorage;
import com.kewen.framework.storage.web.mp.mapper.SysFileStorageMpMapper;
import com.kewen.framework.storage.web.mp.service.SysFileStorageMpService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * <p>
 * 文件存储 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2023-04-24
 */
@Service
@Primary
public class SysFileStorageMpServiceImpl extends ServiceImpl<SysFileStorageMpMapper, SysFileStorage> implements SysFileStorageMpService {

}
