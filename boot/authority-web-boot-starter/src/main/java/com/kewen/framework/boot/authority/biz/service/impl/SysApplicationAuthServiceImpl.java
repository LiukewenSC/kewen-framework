package com.kewen.framework.boot.authority.biz.service.impl;

import com.kewen.framework.boot.authority.biz.entity.SysApplicationAuth;
import com.kewen.framework.boot.authority.biz.mapper.SysApplicationAuthMapper;
import com.kewen.framework.boot.authority.biz.service.SysApplicationAuthService;
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
 * @since 2022-12-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysApplicationAuthServiceImpl extends ServiceImpl<SysApplicationAuthMapper, SysApplicationAuth> implements SysApplicationAuthService {
    @Override
    public boolean hasAuth(Collection<String> auths, String module, String operate, Integer businessId) {
        SysApplicationAuth one = getOne(
                new LambdaQueryWrapper<SysApplicationAuth>()
                        .eq(SysApplicationAuth::getModule, module)
                        .eq(SysApplicationAuth::getBusinessId, businessId)
                        .eq(SysApplicationAuth::getOperate, operate)
                        .in(SysApplicationAuth::getAuthority, auths)
                        .last("limit 1")
                        .select(SysApplicationAuth::getId)
        );
        return one != null;
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
