package com.cu.framework.sample.authcore.service;

import com.cu.framework.auth.core.AuthDataOperation;
import com.cu.framework.sample.authcore.mapper.AssetMapper;
import com.cu.framework.sample.authcore.mapper.ExecutionLogMapper;
import com.cu.framework.sample.authcore.mapper.WorkOrderMapper;
import com.cu.framework.sample.authcore.model.DemoRequests.AssetEditReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务方法层的行级判定载体。
 * <p>
 * {@code @AuthDataOperation} 标在业务方法上而不是接口上也能生效（切面走 Spring AOP），
 * 这里用它来演"实参里同时有散参、集合、数组三类承载位置"的场景——
 * HTTP 方法签名只能有一个请求体，所以把三个实参摊开的位置放在业务方法上
 *
 * @author kewen
 * @since 2026-10-20
 */
@Service
public class DemoBusinessService {

    @Autowired
    private WorkOrderMapper workOrderMapper;
    @Autowired
    private AssetMapper assetMapper;
    @Autowired
    private ExecutionLogMapper executionLogMapper;

    /**
     * ID 分散在一个散参、一个集合与一个数组里：三类位置的ID都要纳入判定，无一被忽略
     *
     * @param one  单个载体
     * @param many 集合，元素各自承载ID
     * @param more 数组，元素各自承载ID
     * @return 本次真正涉及的ID与执行结果
     */
    @AuthDataOperation(businessFunction = "asset", operate = "edit")
    public Map<String, Object> scattered(AssetEditReq one, List<AssetEditReq> many, AssetEditReq[] more) {
        List<String> collected = new ArrayList<>();
        if (one != null) {
            collected.add(String.valueOf(one.getAssetId()));
        }
        if (many != null) {
            for (AssetEditReq item : many) {
                collected.add(String.valueOf(item.getAssetId()));
            }
        }
        if (more != null) {
            for (AssetEditReq item : more) {
                collected.add(String.valueOf(item.getAssetId()));
            }
        }
        // 走到这里说明判定通过且方法体确实执行了
        executionLogMapper.insert("service.scattered", String.join(",", collected), "散参+集合+数组");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("collected", collected);
        result.put("executed", true);
        return result;
    }

    /**
     * 与 {@link #scattered} 相同的数据，但完全没有声明行级权限：按业务原样执行，不发生任何判定
     *
     * @param assetId 资产ID
     * @param name    新名称
     * @return 更新条数
     */
    public int renameUnchecked(Long assetId, String name) {
        executionLogMapper.insert("service.renameUnchecked", String.valueOf(assetId), "未声明判定");
        return assetMapper.updateName(assetId, name);
    }

    /**
     * 未声明的写语句本身：数据范围只作用于查询，写语句原文下发
     *
     * @param id     工单ID
     * @param status 新状态
     * @return 更新条数
     */
    public int updateOrderStatus(Long id, String status) {
        return workOrderMapper.updateStatus(id, status);
    }
}
