package com.cu.framework.sample.authcore.web;

import com.cu.framework.auth.core.data.AuthDataHandler;
import com.cu.framework.auth.core.entity.FlatteningAuthority;
import com.cu.framework.auth.core.exception.AuthEntityException;
import com.cu.framework.auth.core.exception.AuthServiceException;
import com.cu.framework.sample.authcore.authobject.DemoAuthObject;
import com.cu.framework.sample.authcore.authobject.DemoInheritDept;
import com.cu.framework.sample.authcore.authobject.DemoUserAuthInfo;
import com.cu.framework.sample.authcore.context.DemoAuthContextHolder;
import com.cu.framework.sample.authcore.context.DemoMenuAuthHandler;
import com.cu.framework.sample.authcore.context.DemoUserContext;
import com.cu.framework.sample.authcore.entity.MenuApiPO;
import com.cu.framework.sample.authcore.mapper.AuthDataViewMapper;
import com.cu.framework.sample.authcore.mapper.DemoMenuApiMapper;
import com.cu.framework.sample.authcore.mapper.DemoMenuAuthMapper;
import com.cu.framework.sample.authcore.mapper.ExecutionLogMapper;
import com.cu.framework.sample.authcore.model.DemoRequests.PermissionDataCheck;
import com.cu.framework.sample.authcore.model.DemoRequests.PermissionViewReq;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

    /** 预检一次最多接受的菜单路径数 */
    private static final int MAX_MENU_PATHS = 50;
    /** 预检一次最多接受的行级槽位数 */
    private static final int MAX_DATA_CHECKS = 50;
    /** 单个行级槽位一次最多接受的业务数据ID数 */
    private static final int MAX_DATA_IDS = 100;

    @Autowired
    private DemoAuthContextHolder authContextHolder;
    @Autowired
    private DemoMenuAuthHandler demoMenuAuthHandler;
    @Autowired
    private DemoMenuAuthMapper demoMenuAuthMapper;
    @Autowired
    private AuthDataHandler authDataHandler;
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
        Collection<FlatteningAuthority> raw = authContextHolder.loadRawFromStore();
        Collection<FlatteningAuthority> reSerialised = authObject.listBaseAuth();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", DemoUserContext.username());
        body.put("authObject", authObject);
        body.put("rawFromStore", raw);
        body.put("reSerialised", reSerialised);
        body.put("roundTripConsistent", toStringSet(raw).equals(toStringSet(reSerialised)));
        return Result.success(body);
    }

    /**
     * 权限体"可反解 / 不可反解"的不对称。
     * <p>
     * 同一条 {@code DEPTINHERIT__100}：配置侧集合体 {@link DemoAuthObject} 能把它拆回实体、
     * 再由实体拼回完全相同的串；用户侧集合体 {@link DemoUserAuthInfo} 遇到它必须当场失败，
     * 因为串里没有"这是哪个部门的下级"这条信息，反解只能靠猜。
     * <p>
     * 失败在这里被当作数据回显而不是让它冒成一次拒绝：这条用例要观察的是"到底失败了没有"，
     * 不是权限判定结果
     *
     * @return 标记、原始串、配置侧往返结论、用户侧失败原因
     */
    @GetMapping("/inherit-asymmetry")
    public Result<Map<String, Object>> inheritAsymmetry() {
        DemoInheritDept target = new DemoInheritDept(100L, "研发部");
        FlatteningAuthority authority = target.getAuth();

        DemoAuthObject granted = new DemoAuthObject();
        granted.addInheritDepts(target);
        DemoAuthObject parsed = new DemoAuthObject();
        parsed.setProperties(granted.listBaseAuth());
        Collection<FlatteningAuthority> reSerialised = parsed.listBaseAuth();

        String userSideFailure = null;
        boolean userSideRejected = false;
        try {
            new DemoUserAuthInfo(null, Collections.singletonList(target))
                    .setProperties(Collections.singletonList(authority));
        } catch (AuthEntityException e) {
            userSideRejected = true;
            userSideFailure = e.getMessage();
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("flag", DemoInheritDept.FLAG);
        body.put("authority", authority);
        body.put("userSideSerialised", new DemoUserAuthInfo(null, Collections.singletonList(target)).listBaseAuth());
        body.put("configSideParsedInto", parsed.getInheritDepts().size());
        body.put("configSideConsistent", toStringSet(granted.listBaseAuth()).equals(toStringSet(reSerialised)));
        body.put("userSideRejected", userSideRejected);
        body.put("userSideFailure", userSideFailure);
        return Result.success(body);
    }

    /**
     * 把权限集合折成"串集合"，只按权限串本身比对，忽略描述与顺序
     *
     * @param auths 权限集合
     * @return 权限串集合
     */
    private static Set<String> toStringSet(Collection<FlatteningAuthority> auths) {
        Set<String> values = new TreeSet<>();
        for (FlatteningAuthority auth : auths) {
            values.add(auth.getAuth());
        }
        return values;
    }

    /**
     * 权限透视的只读预检：按当前请求的身份回答"菜单能不能进、这些数据能不能操作"。
     * <p>
     * 判定直接调用拦截器与切面使用的同一组处理器，不复制规则；不执行任何业务方法，
     * 因此页面切换身份时可以安全地整表刷新。返回的 required/matched/missing 只是展示依据，
     * 真正的通过与否仍以 ok 字段（处理器返回值）为准
     *
     * @param req 菜单路径与行级槽位
     * @return 当前身份、持有的权限串、菜单与行级判定结果
     */
    @PostMapping("/permission-view")
    public Result<Map<String, Object>> permissionView(@RequestBody PermissionViewReq req) {
        Collection<FlatteningAuthority> auths = authContextHolder.getAuths();
        Set<String> held = toAuthStrings(auths);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", DemoUserContext.username());
        body.put("auths", new ArrayList<>(held));
        body.put("menu", menuView(req.getMenuPaths(), auths, held));
        body.put("data", dataView(req.getDataChecks(), auths, held));
        return Result.success(body);
    }

    /**
     * 逐个菜单路径求判定与依据
     *
     * @param paths 应用内路径
     * @param auths 当前身份的权限集合
     * @param held  当前身份的权限串
     * @return 每个路径的 required/matched/missing/ok
     */
    private List<Map<String, Object>> menuView(List<String> paths, Collection<FlatteningAuthority> auths,
                                               Set<String> held) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (paths == null || paths.isEmpty()) {
            return result;
        }
        if (paths.size() > MAX_MENU_PATHS) {
            throw new AuthServiceException("菜单路径数量超过上限" + MAX_MENU_PATHS);
        }
        for (String path : paths) {
            if (path == null || path.trim().isEmpty()) {
                throw new AuthServiceException("菜单路径不能为空");
            }
            List<String> required = distinct(demoMenuAuthMapper.listAuthsByPath(path));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("path", path);
            item.put("ok", demoMenuAuthHandler.hasMenuAccessAuth(auths, path));
            item.put("required", required);
            item.put("matched", intersect(required, held));
            item.put("missing", difference(required, held));
            result.add(item);
        }
        return result;
    }

    /**
     * 逐个槽位、逐个业务数据ID求判定与依据
     *
     * @param checks 行级槽位检查
     * @param auths  当前身份的权限集合
     * @param held   当前身份的权限串
     * @return 每个槽位、每个ID的 required/matched/missing/ok
     */
    private List<Map<String, Object>> dataView(List<PermissionDataCheck> checks, Collection<FlatteningAuthority> auths,
                                               Set<String> held) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (checks == null || checks.isEmpty()) {
            return result;
        }
        if (checks.size() > MAX_DATA_CHECKS) {
            throw new AuthServiceException("行级判定项数量超过上限" + MAX_DATA_CHECKS);
        }
        for (PermissionDataCheck check : checks) {
            if (check == null) {
                continue;
            }
            String businessFunction = check.getBusinessFunction();
            String operate = check.getOperate();
            if (businessFunction == null || businessFunction.trim().isEmpty()
                    || operate == null || operate.trim().isEmpty()) {
                throw new AuthServiceException("业务功能与操作类型不能为空");
            }
            List<Long> rawIds = check.getIds() == null
                    ? Collections.<Long>emptyList()
                    : new ArrayList<>(new LinkedHashSet<>(check.getIds()));
            if (rawIds.size() > MAX_DATA_IDS) {
                throw new AuthServiceException("业务数据ID数量超过上限" + MAX_DATA_IDS + "，请分批提交");
            }
            List<Map<String, Object>> ids = new ArrayList<>();
            for (Long id : rawIds) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", id);
                item.put("ok", id != null && authDataHandler.hasDataOperateAuths(
                        auths, businessFunction, Collections.<Object>singletonList(id), operate));
                Collection<FlatteningAuthority> configured = id == null
                        ? Collections.<FlatteningAuthority>emptyList()
                        : authDataHandler.getDataAuths(businessFunction, id, operate);
                List<String> required = new ArrayList<>(toAuthStrings(configured));
                item.put("required", required);
                item.put("matched", intersect(required, held));
                item.put("missing", difference(required, held));
                ids.add(item);
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("businessFunction", businessFunction);
            item.put("operate", operate);
            item.put("ids", ids);
            result.add(item);
        }
        return result;
    }

    /**
     * 把权限集合折成去重后的串集合，保持首次出现的顺序
     *
     * @param auths 权限集合，可为 null
     * @return 权限串集合
     */
    private static Set<String> toAuthStrings(Collection<FlatteningAuthority> auths) {
        Set<String> values = new LinkedHashSet<>();
        if (auths != null) {
            for (FlatteningAuthority auth : auths) {
                if (auth != null && auth.getAuth() != null) {
                    values.add(auth.getAuth());
                }
            }
        }
        return values;
    }

    /**
     * 去空、去重并保持顺序
     *
     * @param values 原始串集合
     * @return 清洗后的串列表
     */
    private static List<String> distinct(Collection<String> values) {
        Set<String> result = new LinkedHashSet<>();
        if (values != null) {
            for (String value : values) {
                if (value != null) {
                    result.add(value);
                }
            }
        }
        return new ArrayList<>(result);
    }

    /**
     * 要求的权限串里，当前身份持有的部分
     *
     * @param required 要求的权限串
     * @param held     当前身份持有的权限串
     * @return 交集，保持 required 的顺序
     */
    private static List<String> intersect(Collection<String> required, Set<String> held) {
        List<String> result = new ArrayList<>();
        for (String auth : required) {
            if (auth != null && held.contains(auth)) {
                result.add(auth);
            }
        }
        return result;
    }

    /**
     * 要求的权限串里，当前身份缺少的部分
     *
     * @param required 要求的权限串
     * @param held     当前身份持有的权限串
     * @return 差集，保持 required 的顺序
     */
    private static List<String> difference(Collection<String> required, Set<String> held) {
        List<String> result = new ArrayList<>();
        for (String auth : required) {
            if (auth != null && !held.contains(auth)) {
                result.add(auth);
            }
        }
        return result;
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
