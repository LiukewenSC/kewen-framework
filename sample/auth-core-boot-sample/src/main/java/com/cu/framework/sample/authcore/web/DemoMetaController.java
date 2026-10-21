package com.cu.framework.sample.authcore.web;

import com.cu.framework.auth.core.entity.BaseAuth;
import com.cu.framework.sample.authcore.authobject.DemoAuthObject;
import com.cu.framework.sample.authcore.context.DemoAuthContextHolder;
import com.cu.framework.sample.authcore.context.DemoUserContext;
import com.cu.framework.sample.authcore.entity.MenuApiPO;
import com.cu.framework.sample.authcore.mapper.AuthDataViewMapper;
import com.cu.framework.sample.authcore.mapper.DemoMenuApiMapper;
import com.cu.framework.sample.authcore.mapper.ExecutionLogMapper;
import com.cu.framework.sample.authcore.model.DemoRequests.RemoveReq;
import com.cu.framework.sample.authcore.service.DemoAuthAdminService;
import com.cu.framework.sample.authcore.trace.DemoSqlTrace;
import com.cu.framework.basic.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 观察台。
 * <p>
 * 这些端点<b>不带</b>任何管辖声明，因此永远可以直接调用——验证脚本需要一个不受权限影响的入口
 * 来取回"过程证据"：当前装载了哪些权限、本次实际下发了哪些语句、判定发生了没有、表里现在有什么。
 * 规格里"方法体没执行""未声明的语句原文下发""超出上限时不打库"这几条，只能从这里被断言
 *
 * @author kewen
 * @since 2026-10-20
 */
@RestController
@RequestMapping("/demo")
public class DemoMetaController {

    @Autowired
    private DemoAuthContextHolder authContextHolder;
    @Autowired
    private DemoSqlTrace demoSqlTrace;
    @Autowired
    private ExecutionLogMapper executionLogMapper;
    @Autowired
    private AuthDataViewMapper authDataViewMapper;
    @Autowired
    private DemoMenuApiMapper demoMenuApiMapper;
    @Autowired
    private DemoAuthAdminService demoAuthAdminService;

    /**
     * 当前身份的权限集合体，<b>以对象形态返回</b>：{@code users/roles/depts/deptRoles} 四个列表，
     * 组合权限里嵌着 dept 与 role 两个子对象。
     * <p>
     * 同时给出两份串做对照：{@code rawFromStore} 是从存储原样读出的串（未编解码），
     * {@code reSerialised} 是从对象结构重新拼出的串，{@code roundTripConsistent} 是两者是否一致。
     * 不一致就说明 {@code 标记__ID} 与 {@code 标记__名} 这套规则在示例里没走通
     *
     * @return 身份、对象形态的权限体、两份权限串与一致性结论
     */
    @GetMapping("/current-user")
    public Result<Map<String, Object>> currentUser() {
        DemoAuthObject authObject = authContextHolder.loadAuthObject();
        Collection<BaseAuth> raw = authContextHolder.loadRawFromStore();
        Collection<BaseAuth> reSerialised = authObject.listBaseAuth();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", DemoUserContext.username());
        body.put("authObject", authObject);
        body.put("rawFromStore", raw);
        body.put("reSerialised", reSerialised);
        body.put("roundTripConsistent", toStringSet(raw).equals(toStringSet(reSerialised)));
        return Result.success(body);
    }

    /**
     * 把权限集合折成"串集合"，只按权限串本身比对，忽略描述与顺序
     *
     * @param auths 权限集合
     * @return 权限串集合
     */
    private static Set<String> toStringSet(Collection<BaseAuth> auths) {
        Set<String> values = new TreeSet<>();
        for (BaseAuth auth : auths) {
            values.add(auth.getAuth());
        }
        return values;
    }

    /**
     * 取回某个追踪标识下的过程记录：实际下发的语句 + 真实发生的判定
     *
     * @param traceId 追踪标识，与请求头 X-Trace-Id 一致
     * @return 过程记录
     */
    @GetMapping("/trace")
    public Result<List<DemoSqlTrace.Entry>> trace(@RequestParam("traceId") String traceId) {
        return Result.success(demoSqlTrace.get(traceId));
    }

    /**
     * 清空观察台，便于脚本从一个干净起点开始计数
     *
     * @return 是否清空
     */
    @PostMapping("/trace/clear")
    public Result<String> clearTrace() {
        demoSqlTrace.clear();
        return Result.success("cleared");
    }

    /**
     * 读某个槽位上已配置的权限行
     *
     * @param bizModule 业务功能
     * @param action    操作类型
     * @param bizDataId 业务数据ID
     * @return 槽位视图
     */
    @GetMapping("/auth-data")
    public Result<Map<String, Object>> authData(@RequestParam("bizModule") String bizModule,
                               @RequestParam("action") String action,
                               @RequestParam("bizDataId") Long bizDataId) {
        return Result.success(demoAuthAdminService.slotView(bizModule, action, bizDataId));
    }

    /**
     * 按业务功能读全部权限行，用于核对"含引号的载荷没有多写或多删任何行"
     *
     * @param bizModule 业务功能
     * @return 权限行
     */
    @GetMapping("/auth-data/module")
    public Result<Object> authDataByModule(@RequestParam("bizModule") String bizModule) {
        return Result.success(authDataViewMapper.listByModule(bizModule));
    }

    /**
     * 某接口已执行过多少次方法体（副作用证据）
     *
     * @param apiKey 接口标识
     * @return 计数与明细
     */
    @GetMapping("/log")
    public Result<Map<String, Object>> log(@RequestParam("apiKey") String apiKey) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("apiKey", apiKey);
        body.put("count", executionLogMapper.countByApiKey(apiKey));
        body.put("rows", executionLogMapper.listByApiKey(apiKey));
        return Result.success(body);
    }

    /**
     * 各表示例行数，用于断言重复启动不产生重复行
     *
     * @return 表名与行数
     */
    @GetMapping("/counts")
    public Result<Object> counts() {
        return Result.success(authDataViewMapper.tableCounts());
    }

    /**
     * 当前登记的菜单行，用于验证启动注册、父子层级、文案回落与不覆写
     *
     * @return 菜单行
     */
    @GetMapping("/menu-api")
    public Result<List<MenuApiPO>> menuApi() {
        return Result.success(demoMenuApiMapper.listAll());
    }

    /**
     * 人工改写一条已有菜单行，制造"重启前应被保留"的前后状态
     *
     * @param path   菜单行路径
     * @param name   人工设定的名称
     * @param remark 人工设定的备注
     * @return 更新条数
     */
    @PostMapping("/menu-api/touch")
    public Result<Integer> touchMenuApi(@RequestParam("path") String path,
                                        @RequestParam("name") String name,
                                        @RequestParam("remark") String remark) {
        return Result.success(demoMenuApiMapper.touchByPath(path, name, remark));
    }

    /**
     * 只读地把某个槽位的权限清空——与 /authData/remove 同一动作，但这里不加任何管辖声明，
     * 供脚本在断言之间复原现场，不参与规格验证
     *
     * @param req 目标
     * @return 剩余行数
     */
    @PostMapping("/reset-slot")
    public Result<Map<String, Object>> resetSlot(@RequestBody RemoveReq req) {
        return Result.success(demoAuthAdminService.remove(req));
    }
}
