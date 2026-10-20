package com.cu.framework.sample.authcore.store;

import cn.hutool.core.util.IdUtil;
import com.cu.framework.auth.core.menu.MenuApiEntity;
import com.cu.framework.auth.core.menu.MenuApiService;
import com.cu.framework.sample.authcore.entity.MenuApiPO;
import com.cu.framework.sample.authcore.mapper.DemoMenuApiMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * auth-core 的可选扩展点：菜单行的存储。
 * <p>
 * 框架的生成过程只调用这四个方法——读全量、批量插、拿根节点ID、生成主键，
 * 至于菜单行存在哪、主键怎么来，全由实现决定。本实现用 demo_menu_api 加 hutool 雪花ID。
 * <p>
 * 不提供这个 bean 时应用照样启动，只是启动日志会记录"菜单注册被跳过"
 *
 * @author kewen
 * @since 2026-10-20
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DemoMenuApiStore implements MenuApiService<Long> {

    /** 菜单行存储 */
    private final DemoMenuApiMapper demoMenuApiMapper;

    public DemoMenuApiStore(DemoMenuApiMapper demoMenuApiMapper) {
        this.demoMenuApiMapper = demoMenuApiMapper;
    }

    /**
     * 读回已登记的菜单行。框架用它做两件事：过滤掉已存在的路径、按 parentPath 找回父节点ID，
     * 因此 path 与 id 必须准确
     */
    @Override
    public List<MenuApiEntity<Long>> list() {
        List<MenuApiEntity<Long>> result = new ArrayList<>();
        for (MenuApiPO po : demoMenuApiMapper.listAll()) {
            MenuApiEntity entity = MenuApiEntity.of(po.getPath(), po.getName());
            entity.setId(po.getId());
            entity.setParentId(po.getParentId());
            result.add(entity);
        }
        return result;
    }

    /**
     * 批量登记新菜单行
     */
    @Override
    public void saveBatch(List<MenuApiEntity<Long>> apiEntities) {
        if (apiEntities == null || apiEntities.isEmpty()) {
            return;
        }
        List<MenuApiPO> rows = new ArrayList<>(apiEntities.size());
        for (MenuApiEntity<Long> entity : apiEntities) {
            MenuApiPO po = new MenuApiPO();
            po.setId(entity.getId());
            po.setPath(entity.getPath());
            po.setName(entity.getName());
            po.setParentId(entity.getParentId() == null ? getRootParentId() : entity.getParentId());
            rows.add(po);
        }
        demoMenuApiMapper.insertBatch(rows);
    }

    /**
     * 根节点的父ID，本示例用 0
     */
    @Override
    public Long getRootParentId() {
        return 0L;
    }

    /**
     * 菜单行主键由实现生成，框架不假设它是自增还是别的形态
     */
    @Override
    public Long generateId() {
        return IdUtil.getSnowflakeNextId();
    }
}
