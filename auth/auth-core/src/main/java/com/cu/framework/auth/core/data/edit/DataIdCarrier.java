package com.cu.framework.auth.core.data.edit;


import java.util.Collection;

/**
 * @descrpition 交出本次操作所涉及的全部业务数据ID的载体
 * <p>
 * 由 {@link com.cu.framework.auth.core.AuthDataOperation} 的校验切面读取。判定单位是ID而不是调用，
 * 一次调用涉及的每一个ID都必须单独命中权限，因此这里交出的是集合而非单个值：
 * 只表示一条数据时返回单元素集合即可，表示多条数据时把这一条批次的ID全部交出。
 * <p>
 * ID 的类型不受限制，整型、长整型、字符串均可，框架不假设业务主键的类型。
 * 嵌套结构由实现类自行摊平，切面只看这里交出的集合。
 *
 * @param <ID> 业务主键类型
 * @author kewen
 * @since 2022-11-23
 */
public interface DataIdCarrier<ID> {
    /**
     * 获取本次调用所涉及的全部业务数据ID
     * @return 业务主键集合，单值场景返回单元素集合
     */
    Collection<ID> getDataIds();
}
