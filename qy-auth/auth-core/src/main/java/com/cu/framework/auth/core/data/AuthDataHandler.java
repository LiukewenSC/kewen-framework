package com.cu.framework.auth.core.data;

import com.cu.framework.auth.core.entity.BaseAuth;
import com.cu.framework.auth.core.entity.IAuthObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 *  权限处理器，对于注解涉及到的权限需要的实现都从这里完成
 * <E> 权限对象泛型
 * @author kewen
 * @since 2023-04-10
 */
public interface AuthDataHandler {

    Logger logger = LoggerFactory.getLogger(AuthDataHandler.class);


    /**
     * 是否有这些数据的操作权限
     *  对应行级操作权限 @AuthDataOperation
     * <p>
     * 判定单位是数据ID：传入的每一个ID都必须在 businessFunction + operate 这个槽位下命中用户的权限，
     * 只要有一个未命中即返回 false，不做跨操作类型的回退，也不因权限集合为空而放行。
     * ID 的比较按字符串归一化进行，整型与长整型等同一主键的不同形态视为同一条数据。
     * @param auths 用户权限
     * @param businessFunction 模块
     * @param dataIds 业务id集合，如 [1L, 1011L] 等业务主键ID
     * @param operate 操作
     * @return 是否全部数据都有权限
     */
    boolean hasDataOperateAuths(Collection<BaseAuth> auths, String businessFunction, Collection<Object> dataIds, String operate);
    /**
     * 基于权限对象判断是否有这些数据的操作权限
     * @param authObject 权限集合体，为 null 时直接返回 false
     * @param businessFunction 模块
     * @param dataIds 业务id集合
     * @param operate 操作
     * @return 是否全部数据都有权限
     */
    default boolean hasDataOperateAuths(IAuthObject authObject, String businessFunction, Collection<Object> dataIds, String operate){
        if (authObject==null){
            logger.info("authObject is null");
            return false;
        }
        return hasDataOperateAuths(authObject.listBaseAuth(), businessFunction, dataIds, operate);
    }


    /**
     * 编辑某条 数据权限
     * 但是这里要注意了，不应该编辑此接口本身的权限，否则就会出现自己把自己编辑没，或者把不应该有的人加入（其实就是属于越权了，本应该是上级做的事）
     * @param dataId 数据ID
     * @param businessFunction 模块
     * @param operate 操作
     * @param auths 权限结构
     */
    void editDataAuths(String businessFunction,Object dataId,  String operate, Collection<BaseAuth> auths);
    /**
     * 基于权限对象编辑某条数据的权限
     * @param businessFunction 模块
     * @param dataId 数据ID
     * @param operate 操作
     * @param authObject 权限集合体，为 null 时不做任何处理
     */
    default void editDataAuths(String businessFunction,Object dataId,  String operate, IAuthObject authObject){
        if (authObject==null){
            logger.info("authObject is null");
            return;
        }
        editDataAuths(businessFunction, dataId, operate, authObject.listBaseAuth());
    }

    /**
     * 获取某条数据在指定操作下配置的权限
     * @param businessFunction 模块
     * @param dataId 数据ID
     * @param operate 操作
     * @return 该数据该操作下已配置的权限集合
     */
    Collection<BaseAuth> getDataAuths(String businessFunction,Object dataId,  String operate);

    /**
     * 填充数据，把某条数据已配置的权限写入权限对象
     * @param businessFunction 模块
     * @param dataId 数据ID
     * @param operate 操作
     * @param authObject 待填充的权限对象
     */
    default void fillDataAuths(String businessFunction,Object dataId,  String operate,IAuthObject authObject){
        authObject.setProperties(getDataAuths(businessFunction, dataId, operate));
    }

    /**
     * 根据业务功能数据ID删除权限
     * 其实就是清空dataID对应的数据，在业务中删除了此条数据需要调用
     */
    void deleteBusinessFunctionAuthByDataId(String businessFunction,Object dataId);

}
