package com.cu.framework.sample.authcore.web;

import com.cu.framework.auth.core.AuthDataOperation;
import com.cu.framework.basic.model.Result;
import com.cu.framework.sample.authcore.mapper.ExecutionLogMapper;
import com.cu.framework.sample.authcore.model.DemoRequests.BulkIdsReq;
import com.cu.framework.sample.authcore.model.DemoRequests.EmptyIdsReq;
import com.cu.framework.sample.authcore.model.DemoRequests.NullIdReq;
import com.cu.framework.sample.authcore.model.DemoRequests.OrderEditReq;
import com.cu.framework.sample.authcore.model.DemoRequests.RawIdReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 判定边界的探针端点。
 * <p>
 * 这些端点<b>不带</b>菜单管辖声明：它们要演的是"能不能判定"，
 * 与"能不能进这个门"无关，混在一起会让观察结果失去唯一变量。
 * 用独立的业务功能 bulk_probe / probe / quote_probe，避免污染工单与资产的正常场景
 *
 * @author kewen
 * @since 2026-10-20
 */
@RestController
@RequestMapping("/probe")
public class ProbeController {

    @Autowired
    private ExecutionLogMapper executionLogMapper;

    /**
     * 方法受行级管辖，但实参里没有任何承载对象——属于用法错误，绝不能因为找不到判定对象而放行
     *
     * @param memo 与业务数据无关的参数
     * @return 只有实现有缺陷时才会走到这里
     */
    @AuthDataOperation(businessFunction = "bulk_probe", operate = "unified")
    @PostMapping("/noCarrier")
    public Result<String> noCarrier(@RequestParam(value = "memo", required = false) String memo) {
        return Result.success("不该被执行");
    }

    /**
     * 承载对象交出的集合为空：同样是用法错误
     *
     * @param req 空集合
     * @return 不该被执行
     */
    @AuthDataOperation(businessFunction = "bulk_probe", operate = "unified")
    @PostMapping("/emptyIds")
    public Result<String> emptyIds(@RequestBody EmptyIdsReq req) {
        return Result.success("不该被执行");
    }

    /**
     * 交出的集合里含 null：同样是用法错误，不能当成"这条没有ID"跳过
     *
     * @param req 含空值的集合
     * @return 不该被执行
     */
    @AuthDataOperation(businessFunction = "bulk_probe", operate = "unified")
    @PostMapping("/nullId")
    public Result<String> nullId(@RequestBody NullIdReq req) {
        return Result.success("不该被执行");
    }

    /**
     * 上限：去重后 100 个正常判定（会打权限表，绝大多数未命中所以拒绝）；
     * 101 个直接以用法错误失败，且一次权限查询都不发
     *
     * @param req 本批ID
     * @return 执行结果
     */
    @AuthDataOperation(businessFunction = "bulk_probe", operate = "unified")
    @PostMapping("/bulk")
    public Result<Map<String, Object>> bulk(@RequestBody BulkIdsReq req) {
        executionLogMapper.insert("probe.bulk", String.valueOf(req.getIds().size()), "上限探针");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("executed", true);
        body.put("count", req.getIds().size());
        return Result.success(body);
    }

    /**
     * 探针槽位的正常判定：801 已授予 ROLE__10，802/803 没有任何授权行
     *
     * @param req 目标数据
     * @return 执行结果
     */
    @AuthDataOperation(businessFunction = "probe", operate = "edit")
    @PostMapping("/call")
    public Result<Map<String, Object>> call(@RequestBody OrderEditReq req) {
        executionLogMapper.insert("probe.call", String.valueOf(req.getId()), "探针判定");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("executed", true);
        body.put("id", req.getId());
        return Result.success(body);
    }

    /**
     * 权限串含引号载荷的槽位：只有整值相等才算命中，载荷不会被拆成语句片段去匹配别的行
     *
     * @param req 原始文本形态的ID
     * @return 执行结果
     */
    @AuthDataOperation(businessFunction = "quote_probe", operate = "unified")
    @PostMapping("/quote/call")
    public Result<Map<String, Object>> quoteCall(@RequestBody RawIdReq req) {
        executionLogMapper.insert("probe.quoteCall", req.getRawId(), "引号载荷");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("executed", true);
        body.put("rawId", req.getRawId());
        return Result.success(body);
    }
}
