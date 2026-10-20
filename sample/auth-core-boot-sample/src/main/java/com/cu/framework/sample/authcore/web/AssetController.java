package com.cu.framework.sample.authcore.web;

import com.cu.framework.auth.core.AuthDataOperation;
import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.AuthMenuDoc;
import com.cu.framework.basic.model.Result;
import com.cu.framework.sample.authcore.entity.Asset;
import com.cu.framework.sample.authcore.mapper.AssetMapper;
import com.cu.framework.sample.authcore.mapper.ExecutionLogMapper;
import com.cu.framework.sample.authcore.model.DemoRequests.AssetEditReq;
import com.cu.framework.sample.authcore.model.DemoRequests.AssetEditShortFormReq;
import com.cu.framework.sample.authcore.model.DemoRequests.OrderBatchReq;
import com.cu.framework.sample.authcore.model.DemoRequests.ScatteredReq;
import com.cu.framework.sample.authcore.service.DemoBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 资产：主键列名不是 id（dataIdColumn 场景），并且承载"整型主键的不同形态"与"分散承载"两类判定
 *
 * @author kewen
 * @since 2026-10-20
 */
@RestController
@RequestMapping("/asset")
public class AssetController {

    @Autowired
    private AssetMapper assetMapper;
    @Autowired
    private ExecutionLogMapper executionLogMapper;
    @Autowired
    private DemoBusinessService demoBusinessService;

    /**
     * 按 asset 的 unified 槽位过滤的列表，声明里 dataIdColumn 指向 asset_id
     *
     * @return 可见资产
     */
    @GetMapping("/list")
    public Result<List<Asset>> list() {
        return Result.success(assetMapper.listRange());
    }

    /**
     * 未声明的对照列表
     *
     * @return 所有资产
     */
    @GetMapping("/all")
    public Result<List<Asset>> all() {
        return Result.success(assetMapper.listAll());
    }

    /**
     * 单条编辑：交出 Long 形态的ID
     *
     * @param req 目标资产
     * @return 执行结果
     */
    @AuthMenu
    @AuthMenuDoc(name = "编辑资产")
    @AuthDataOperation(businessFunction = "asset", operate = "edit")
    @PostMapping("/update")
    public Result<Map<String, Object>> update(@RequestBody AssetEditReq req) {
        executionLogMapper.insert("asset.update", String.valueOf(req.getAssetId()), req.getName());
        int rows = assetMapper.updateName(req.getAssetId(), req.getName());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("executed", true);
        body.put("updated", rows);
        return Result.success(body);
    }

    /**
     * 同一条数据、同一个槽位，但ID以 Integer 形态交出：库里是 BIGINT、读回是 Long，
     * 判定按归一化后的值比对，不因形态差异误判
     *
     * @param req 目标资产（短整型形态）
     * @return 执行结果
     */
    @AuthMenu
    @AuthMenuDoc(name = "编辑资产短整型形态")
    @AuthDataOperation(businessFunction = "asset", operate = "edit")
    @PostMapping("/updateIntegerForm")
    public Result<Map<String, Object>> updateIntegerForm(@RequestBody AssetEditShortFormReq req) {
        executionLogMapper.insert("asset.updateIntegerForm", String.valueOf(req.getAssetId()), "整型短形态");
        int rows = assetMapper.updateName(Long.valueOf(req.getAssetId()), "short-form");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("executed", true);
        body.put("submittedForm", req.getAssetId());
        body.put("updated", rows);
        return Result.success(body);
    }

    /**
     * 批量编辑：一个载体交出整批ID
     *
     * @param req 目标资产集合
     * @return 执行结果
     */
    @AuthMenu
    @AuthMenuDoc(name = "批量编辑资产")
    @AuthDataOperation(businessFunction = "asset", operate = "edit")
    @PostMapping("/batchUpdate")
    public Result<Map<String, Object>> batchUpdate(@RequestBody OrderBatchReq req) {
        executionLogMapper.insert("asset.batchUpdate", String.valueOf(req.getIds()), "批量");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("executed", true);
        body.put("ids", req.getIds());
        return Result.success(body);
    }

    /**
     * ID 分散在一个散参、一个集合与一个数组里：交给业务方法上的声明去收集，三类位置都不能漏
     *
     * @param req 三种承载位置
     * @return 业务方法收集到的ID
     */
    @AuthMenu
    @AuthMenuDoc(name = "分散承载的资产编辑")
    @PostMapping("/scattered")
    public Result<Map<String, Object>> scattered(@RequestBody ScatteredReq req) {
        return Result.success(demoBusinessService.scattered(req.getPrimary(), req.getMany(), req.moreArray()));
    }

    /**
     * 完全没有声明的写端点，走业务方法里那条未声明的路径
     *
     * @param req 目标资产
     * @return 执行结果
     */
    @PostMapping("/renameUnchecked")
    public Result<Map<String, Object>> renameUnchecked(@RequestBody AssetEditReq req) {
        int rows = demoBusinessService.renameUnchecked(req.getAssetId(), req.getName());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("executed", true);
        body.put("updated", rows);
        return Result.success(body);
    }
}
