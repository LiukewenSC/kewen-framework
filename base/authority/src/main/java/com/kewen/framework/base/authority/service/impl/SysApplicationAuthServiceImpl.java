package com.kewen.framework.base.authority.service.impl;

import com.kewen.framework.base.authority.entity.SysApplicationAuth;
import com.kewen.framework.base.authority.mapper.SysApplicationAuthMapper;
import com.kewen.framework.base.authority.service.SysApplicationAuthService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
/**
 * <p>
 * 应用权限表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysApplicationAuthServiceImpl extends ServiceImpl<SysApplicationAuthMapper, SysApplicationAuth> implements SysApplicationAuthService {

    @Override
    public boolean hasAuth(Collection<String> auths, String module, String operate, Long businessId) {
        Integer i = baseMapper.hasAuth(auths,module,operate,businessId);
        return i != null && i > 0;
    }

    @Override
    public Page<SysApplicationAuth> pageQuery(Integer pageNo, Integer pageSize, SysApplicationAuth queryModel) {
        Page<SysApplicationAuth> page = new Page<>();
        Wrapper<SysApplicationAuth> wrapper = new LambdaQueryWrapper<SysApplicationAuth>()
                .orderByDesc(SysApplicationAuth::getUpdateTime);
        return this.page(page,wrapper);
    }

    @Override
    public List<SysApplicationAuth> findList(SysApplicationAuth queryModel) {
        Wrapper<SysApplicationAuth> wrapper = new LambdaQueryWrapper<SysApplicationAuth>()
                .orderByDesc(SysApplicationAuth::getUpdateTime);
        return this.list(wrapper);
    }

}
