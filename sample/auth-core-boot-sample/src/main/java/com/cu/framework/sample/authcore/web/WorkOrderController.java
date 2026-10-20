package com.cu.framework.sample.authcore.web;

import com.cu.framework.auth.core.AuthDataOperation;
import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.AuthMenuDoc;
import com.cu.framework.basic.model.Result;
import com.cu.framework.sample.authcore.entity.WorkOrder;
import com.cu.framework.sample.authcore.mapper.ExecutionLogMapper;
import com.cu.framework.sample.authcore.mapper.WorkOrderMapper;
import com.cu.framework.sample.authcore.model.DemoRequests.OrderBatchReq;
import com.cu.framework.sample.authcore.model.DemoRequests.OrderEditReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工单：数据范围过滤的现场，同时也是行级判定的主战场。
 * <p>
 * 列表类端点<b>不带</b>菜单管辖声明——它们要演的是"注入到哪条语句、返回哪些行"，
 * 与能不能进这个门无关；写端点则同时带上管辖声明与行级声明，
 * 且两者的授权都在种子数据里给全，好让观察到的拒绝只可能来自行级判定
 *
 * @author kewen
 * @since 2026-10-20
 */
@RestController
@RequestMapping("/workOrder")
public class WorkOrderController {

    @Autowired
    private WorkOrderMapper workOrderMapper;
    @Autowired
    private ExecutionLogMapper executionLogMapper;

    /**
     * 声明了 unified 槽位、IN 匹配的列表
     *
     * @return 可见行
     */
    @GetMapping("/rangeList")
    public Result<List<WorkOrder>> rangeList() {
        return Result.success(workOrderMapper.listRangeIn());
    }

    /**
     * 同一槽位、EXISTS 匹配的列表，行集应与 {@link #rangeList()} 完全一致
     *
     * @return 可见行
     */
    @GetMapping("/rangeListExists")
    public Result<List<WorkOrder>> rangeListExists() {
        return Result.success(workOrderMapper.listRangeExists());
    }

    /**
     * 同一槽位的计数，注入同样生效
     *
     * @return 可见行数
     */
    @GetMapping("/countRange")
    public Result<Integer> countRange() {
        return Result.success(workOrderMapper.countRange());
    }

    /**
     * 同表的另一个槽位：可编辑列表
     *
     * @return 命中编辑授权的行
     */
    @GetMapping("/editable")
    public Result<List<WorkOrder>> editable() {
        return Result.success(workOrderMapper.listEditable());
    }

    /**
     * 同表的又一个槽位：可审批列表
     *
     * @return 命中审批授权的行
     */
    @GetMapping("/approvable")
    public Result<List<WorkOrder>> approvable() {
        return Result.success(workOrderMapper.listApprovable());
    }

    /**
     * 同一次请求里先后用两个槽位，第二次的结果不受第一次影响
     *
     * @return 两个槽位各自的行集
     */
    @GetMapping("/twoSlots")
    public Result<Map<String, Object>> twoSlots() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("editable", workOrderMapper.listEditable());
        body.put("approvable", workOrderMapper.listApprovable());
        body.put("editableAgain", workOrderMapper.listEditable());
        return Result.success(body);
    }

    /**
     * 没有任何声明的对照列表：永远返回全部业务行
     *
     * @return 所有行
     */
    @GetMapping("/all")
    public Result<List<WorkOrder>> all() {
        return Result.success(workOrderMapper.listAll());
    }

    /**
     * 外层声明语句内部触发另一条声明语句：两条各按自己的槽位注入
     *
     * @return 带同客户子列表的行集
     */
    @GetMapping("/nested")
    public Result<List<WorkOrder>> nested() {
        return Result.success(workOrderMapper.listNested());
    }

    /**
     * 多表语句，权限条件挂到别名 cu
     *
     * @return 可见行
     */
    @GetMapping("/joinCu")
    public Result<List<WorkOrder>> joinCu() {
        return Result.success(workOrderMapper.listJoinCu());
    }

    /**
     * 同一条多表语句形态，权限条件挂到别名 cb
     *
     * @return 可见行
     */
    @GetMapping("/joinCb")
    public Result<List<WorkOrder>> joinCb() {
        return Result.success(workOrderMapper.listJoinCb());
    }

    /**
     * 动态语句：SQL 运行期拼得出目标表时照常注入
     *
     * @param status 状态条件，可为空
     * @return 可见行
     */
    @GetMapping("/dynamic")
    public Result<List<WorkOrder>> dynamic(@RequestParam(value = "status", required = false) String status) {
        return Result.success(workOrderMapper.listDynamic(status));
    }

    /**
     * 动态语句的失败面：运行期拼不出声明的目标表时抛异常，而不是返回未经过滤的结果
     *
     * @param withJoin 是否拼出关联表
     * @return 只有 withJoin=true 且权限命中时才有结果
     */
    @GetMapping("/dynamicMissingTable")
    public Result<List<WorkOrder>> dynamicMissingTable(@RequestParam("withJoin") boolean withJoin) {
        return Result.success(workOrderMapper.listDynamicMissingTable(withJoin));
    }

    /**
     * 写工单：受菜单管辖，同时按 work_order/edit 槽位逐条判定
     *
     * @param req 目标工单
     * @return 执行结果
     */
    @AuthMenu
    @AuthMenuDoc(name = "编辑工单")
    @AuthDataOperation(businessFunction = "work_order", operate = "edit")
    @PostMapping("/update")
    public Result<Map<String, Object>> update(@RequestBody OrderEditReq req) {
        executionLogMapper.insert("workOrder.update", String.valueOf(req.getId()), req.getTitle());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("executed", true);
        body.put("id", req.getId());
        return Result.success(body);
    }

    /**
     * 审批工单：与编辑同表不同槽位，互不回退
     *
     * @param req 目标工单
     * @return 执行结果
     */
    @AuthMenu
    @AuthMenuDoc(name = "审批工单")
    @AuthDataOperation(businessFunction = "work_order", operate = "approve")
    @PostMapping("/approve")
    public Result<Map<String, Object>> approve(@RequestBody OrderEditReq req) {
        executionLogMapper.insert("workOrder.approve", String.valueOf(req.getId()), "审批");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("executed", true);
        body.put("id", req.getId());
        return Result.success(body);
    }

    /**
     * 批量审批：一个载体交出整批ID，全部命中才放行
     *
     * @param req 目标工单集合
     * @return 执行结果
     */
    @AuthMenu
    @AuthMenuDoc(name = "批量审批工单")
    @AuthDataOperation(businessFunction = "work_order", operate = "approve")
    @PostMapping("/batchApprove")
    public Result<Map<String, Object>> batchApprove(@RequestBody OrderBatchReq req) {
        executionLogMapper.insert("workOrder.batchApprove", join(req.getIds()), "批量审批");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("executed", true);
        body.put("ids", req.getIds());
        return Result.success(body);
    }

    /**
     * 没有任何声明的写端点：数据在权限表里明明有受控记录，也照样按业务原样执行，不发生判定
     *
     * @param req 目标工单
     * @return 执行结果
     */
    @PostMapping("/updateUnchecked")
    public Result<Map<String, Object>> updateUnchecked(@RequestBody OrderEditReq req) {
        executionLogMapper.insert("workOrder.updateUnchecked", String.valueOf(req.getId()), "未声明判定");
        int rows = workOrderMapper.updateStatus(req.getId(), "unchecked");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("executed", true);
        body.put("updated", rows);
        return Result.success(body);
    }

    /**
     * 把ID集合拼成一行日志文本
     *
     * @param ids 业务数据ID
     * @return 逗号分隔的ID串
     */
    private static String join(List<Long> ids) {
        StringBuilder builder = new StringBuilder();
        if (ids != null) {
            for (Long id : ids) {
                if (builder.length() > 0) {
                    builder.append(',');
                }
                builder.append(id);
            }
        }
        return builder.toString();
    }
}
