package com.cu.framework.sample.authcore.authobject;

import com.cu.framework.auth.core.entity.AbstractAuthObject;
import com.cu.framework.auth.core.entity.BaseAuth;
import com.cu.framework.auth.core.entity.IFlagAuthEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 示例的权限集合体。
 * <p>
 * 继承 {@link AbstractAuthObject} 就自动获得"权限串按 {@code __} 拆标记、按标记找回实体类、
 * 再按 {@code _} 逐位置回填"的解析规则，子类只需交出解析结果并实现回写。
 * <p>
 * 这里故意让 {@link #listBaseAuth()} 从解析出的实体<b>重新序列化</b>得到权限串，而不是原样退回入参：
 * 往返必须一致，否则判定用的串和库里存的串会对不上，解析规则的错误会立刻暴露
 *
 * @author kewen
 * @since 2026-10-20
 */
public class DemoAuthObject extends AbstractAuthObject {

    private static final long serialVersionUID = 1L;

    /** 解析出来的权限实体 */
    private final List<IFlagAuthEntity> entities = new ArrayList<>();

    /**
     * 由解析后的实体重新拼出权限串
     */
    @Override
    public Collection<BaseAuth> listBaseAuth() {
        List<BaseAuth> result = new ArrayList<>(entities.size());
        for (IFlagAuthEntity entity : entities) {
            result.add(entity.getAuth());
        }
        return result;
    }

    /**
     * {@link AbstractAuthObject} 解析完成后的回写钩子
     */
    @Override
    protected void setBaseAuth(List<IFlagAuthEntity> abstractAuthEntities) {
        entities.clear();
        entities.addAll(abstractAuthEntities);
    }

    /**
     * @return 解析出的权限实体，供 /demo/current-user 回显往返结果
     */
    public List<IFlagAuthEntity> getEntities() {
        return entities;
    }
}
