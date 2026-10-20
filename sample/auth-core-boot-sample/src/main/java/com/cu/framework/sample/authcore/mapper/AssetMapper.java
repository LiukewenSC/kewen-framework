package com.cu.framework.sample.authcore.mapper;

import com.cu.framework.auth.core.AuthDataRange;
import com.cu.framework.sample.authcore.entity.Asset;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资产业务查询，主键列名是 asset_id，用于验证 dataIdColumn 声明
 *
 * @author kewen
 * @since 2026-10-20
 */
public interface AssetMapper {

    /**
     * 按 unified 槽位过滤的资产列表，业务主键列显式声明为 asset_id
     *
     * @return 当前用户可见的资产
     */
    @AuthDataRange(businessFunction = "asset", operate = "unified", dataIdColumn = "asset_id")
    List<Asset> listRange();

    /**
     * 未声明的对照列表
     *
     * @return 所有资产
     */
    List<Asset> listAll();

    /**
     * 写语句，未声明
     *
     * @param assetId 资产ID
     * @param name    新名称
     * @return 更新条数
     */
    int updateName(@Param("assetId") Long assetId, @Param("name") String name);
}
