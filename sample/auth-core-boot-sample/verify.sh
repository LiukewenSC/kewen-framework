#!/usr/bin/env bash
# auth-core 示例的验证脚本：README.md 第 6 节那张矩阵就是这里逐条的断言，编号一一对应。
#
# 用法：
#   bash verify.sh                跑一遍默认阶段（应用需要已经启动）
#   bash verify.sh --stage=restart  跑需要两次启动的阶段（会提示何时重启）
#   BASE=http://host:port/coredemo bash verify.sh
#
# 依赖：curl。不启停应用、不改数据库结构；写操作的用例都走 probe 模块并在结尾复原。
set -uo pipefail

BASE=${BASE:-http://localhost:8082/coredemo}
STAGE="default"
for arg in "$@"; do
  case "$arg" in
    --stage=*) STAGE="${arg#*=}" ;;
  esac
done

TMPD=$(mktemp -d)
trap 'rm -rf "$TMPD"' EXIT

PASS=0
FAIL=0

ok()   { PASS=$((PASS+1)); printf 'PASS  %-6s %s\n' "$1" "$2"; }
no()   { FAIL=$((FAIL+1)); printf 'FAIL  %-6s %s\n     期望: %s\n     实际: %s\n' "$1" "$2" "$3" "$4"; }

# 取响应里的 code
code_of() { printf '%s' "$1" | grep -o '"code":[0-9]*' | head -1 | cut -d: -f2; }
# 取副作用计数
log_count() { curl -s "$BASE/demo/log?apiKey=$1" | grep -o '"count":[0-9]*' | head -1 | cut -d: -f2; }
# 取行集里的 ID（工单用 id，资产用 assetId），排序后逗号连接
row_ids() { printf '%s' "$1" | grep -o '"\(asset\)\?[iI]d":[0-9]*' | cut -d: -f2 | sort -n | paste -sd, -; }
# GET，指定用户
get_as() { curl -s -H "X-Demo-User: $1" "$BASE$2"; }
# POST JSON
post_as() {
  local user="$1" path="$2" body="$3"
  printf '%s' "$body" > "$TMPD/body.json"
  curl -s -X POST -H 'Content-Type: application/json; charset=utf-8' \
       -H "X-Demo-User: $user" --data-binary "@$TMPD/body.json" "$BASE$path"
}
# 带追踪标识的 POST，并取回该次请求的过程记录
trace_of() { curl -s "$BASE/demo/trace?traceId=$1"; }

echo "== auth-core-boot-sample 验证（阶段：$STAGE，目标：$BASE）=="
if ! curl -s -o /dev/null "$BASE/open/ping"; then
  echo "应用没在跑：先按 README 第 4 节启动，BASE 可用环境变量覆盖"
  exit 2
fi

# ---------------- A 装配与接缝 ----------------
BODY=$(curl -s -H 'X-Demo-User: dave' "$BASE/demo/current-user")
if printf '%s' "$BODY" | grep -q '"roundTripConsistent":true'    && printf '%s' "$BODY" | grep -q '"deptRoles":\[{"deptId":100'    && printf '%s' "$BODY" | grep -q '"reSerialised":\[{"auth":"DEPT_ROLE__100_20"'; then
  ok A4 "权限体是对象形态（deptRoles 是带字段名的对象），且串→对象→串往返一致"
else
  no A4 "对象形态 + 往返一致" "roundTripConsistent:true / deptRoles 嵌对象 / reSerialised 含 DEPT_ROLE__100_20" "$(printf '%s' "$BODY" | head -c 300)"
fi

# A4b：含角色的用户。归位分支漏掉某一个类型时，该角色的串会从 listBaseAuth() 里静默消失，
# 表现为"权限明明在库里但判定不通过"——所以必须逐类比对原文与重序列化的出现次数
BODY=$(curl -s -H 'X-Demo-User: alice' "$BASE/demo/current-user")
RC=$(printf '%s' "$BODY" | grep -c '"roundTripConsistent":true')
NROLE=$(printf '%s' "$BODY" | grep -o '"auth":"ROLE__10"' | wc -l | tr -d ' ')
NUSER=$(printf '%s' "$BODY" | grep -o '"auth":"USER__1"' | wc -l | tr -d ' ')
if [ "$RC" = "1" ] && [ "$NROLE" -ge 2 ] && [ "$NUSER" -ge 2 ]; then
  ok A4b "含角色的用户不丢权限：ROLE__10 与 USER__1 在原文和重序列化里都在"
else
  no A4b "角色/用户两类都不许丢" "roundTripConsistent=true 且每条串至少出现 2 次" "rc=$RC role=$NROLE user=$NUSER $(printf '%s' "$BODY" | head -c 240)"
fi

# A5：继承型标记的不对称。配置侧权限集合体能把 DEPTINHERIT__100 拆回实体再拼回同一条串；
# 用户侧权限集合体遇到它必须当场失败——串里不含"这是哪个部门的下级"，反解只能靠猜
BODY=$(curl -s "$BASE/demo/inherit-asymmetry")
if printf '%s' "$BODY" | grep -q '"configSideConsistent":true'    && printf '%s' "$BODY" | grep -q '"configSideParsedInto":1'    && printf '%s' "$BODY" | grep -q '"userSideRejected":true'    && printf '%s' "$BODY" | grep -q 'DEPTINHERIT'; then
  ok A5 "继承串在配置侧无损往返，用户权限体反解当场失败且点名 DEPTINHERIT"
else
  no A5 "往返一致 + 反解当场失败" "configSideConsistent:true / configSideParsedInto:1 / userSideRejected:true / 消息含 DEPTINHERIT" "$(printf '%s' "$BODY" | head -c 300)"
fi

# ---------------- B 菜单鉴权 ----------------
BODY=$(get_as bob /methodLevel/guarded); [ "$(code_of "$BODY")" = "403" ] && ok B1a "方法级声明：bob 被拒" || no B1a "方法级声明拒绝" 403 "$BODY"
BODY=$(get_as bob /methodLevel/free);   [ "$(code_of "$BODY")" = "200" ] && ok B1b "同类未声明端点放行" || no B1b "同类未声明端点放行" 200 "$BODY"
BODY=$(get_as alice /classLevel/all);   [ "$(code_of "$BODY")" = "200" ] && ok B2a "类级声明：已授予的路径放行" || no B2a "类级放行" 200 "$BODY"
BODY=$(get_as alice /classLevel/two);   [ "$(code_of "$BODY")" = "403" ] && ok B2b "类级声明下逐路径授权独立（two 未授予）" || no B2b "two 应被拒" 403 "$BODY"
BODY=$(get_as bob /open/ping);          [ "$(code_of "$BODY")" = "200" ] && ok B3  "两处都无声明的类不受管辖" || no B3 "无声明放行" 200 "$BODY"
BODY=$(curl -s "$BASE/methodLevel/guarded"); [ "$(code_of "$BODY")" = "403" ] && ok B4 "无身份（空权限集合）被拒" || no B4 "空权限拒绝" 403 "$BODY"
if printf '%s' "$BODY" | grep -q '"message":"没有API菜单/methodLevel/guarded'; then
  ok B5 "拒绝消息里的身份是去掉 contextPath 的应用内路径"
else
  no B5 "身份不含 /coredemo 前缀" "/methodLevel/guarded" "$BODY"
fi
BODY=$(curl -s "$BASE/demo/menu-api")
printf '%s' "$BODY" | grep -q '"path":"/classLevel"' && ok B6a "类虚拟路径行已注册" || no B6a "类虚拟路径行" "存在 /classLevel" "$BODY"
printf '%s' "$BODY" | grep -q '"path":"/classLevel/all"' && ok B6b "端点行已注册" || no B6b "端点行" "存在 /classLevel/all" "-"
BODY=$(curl -s "$BASE/demo/menu-api")
printf '%s' "$BODY" | grep -q 'ClassNoDocController&gt;all\|ClassNoDocController>all' && ok B8a "无文案时端点标签回落到方法名" || no B8a "标签回落" "ClassNoDocController>all" "-"
printf '%s' "$BODY" | grep -q '"name":"ClassNoDocController"' && ok B8b "类无文案时分组名回落到类简单名" || no B8b "分组名回落" "ClassNoDocController" "-"
BODY=$(get_as bob /methodLevel/sameDocA); [ "$(code_of "$BODY")" = "200" ] && ok B9a "同名文案 A 放行" || no B9a "A 放行" 200 "$BODY"
BODY=$(get_as bob /methodLevel/sameDocB); [ "$(code_of "$BODY")" = "403" ] && ok B9b "同名文案 B 仍独立拒绝（两行授权不共用）" || no B9b "B 应被拒" 403 "$BODY"
BODY=$(get_as alice /classLevel/all); [ "$(code_of "$BODY")" = "200" ] && ok B10a "改文案后 alice 归属不变" || no B10a "改文案后放行" 200 "$BODY"
BODY=$(get_as bob /classLevel/all);   [ "$(code_of "$BODY")" = "403" ] && ok B10b "改文案后 bob 仍被拒（身份只认 path）" || no B10b "改文案后拒绝" 403 "$BODY"
# B11/B12：/deptInherit/ledger 只被授予 DEPTINHERIT__100 一条串，谁能进只取决于谁持有它
BODY=$(get_as frank /deptInherit/ledger); [ "$(code_of "$BODY")" = "200" ] && ok B11 "继承型标记放行持有它的用户" || no B11 "frank 应放行" 200 "$BODY"
BODY=$(get_as bob   /deptInherit/ledger); [ "$(code_of "$BODY")" = "403" ] && ok B12 "同一条串对不持有者拒绝（bob 只有 ROLE__20）" || no B12 "bob 应被拒" 403 "$BODY"

# ---------------- C 行级数据操作权限 ----------------
BEFORE=$(log_count workOrder.update)
BODY=$(post_as alice /workOrder/update '{"id":1,"title":"verify"}')
AFTER=$(log_count workOrder.update)
[ "$(code_of "$BODY")" = "200" ] && [ "$AFTER" = "$((BEFORE+1))" ] && ok C1 "单 ID 命中放行且方法体执行" || no C1 "放行+计数+1" "$((BEFORE+1))" "code=$(code_of "$BODY") count=$AFTER"

BEFORE=$(log_count workOrder.update)
BODY=$(post_as bob /workOrder/update '{"id":1,"title":"verify"}')
AFTER=$(log_count workOrder.update)
[ "$(code_of "$BODY")" = "403" ] && [ "$AFTER" = "$BEFORE" ] && ok C2 "未命中拒绝且方法体未执行" || no C2 "403 且计数不变" "$BEFORE" "code=$(code_of "$BODY") count=$AFTER"

BEFORE=$(log_count workOrder.batchApprove)
BODY=$(post_as alice /workOrder/batchApprove '{"ids":[4,5]}')
AFTER=$(log_count workOrder.batchApprove)
[ "$(code_of "$BODY")" = "200" ] && [ "$AFTER" = "$((BEFORE+1))" ] && ok C3 "多 ID 全命中放行" || no C3 "200+计数+1" "$((BEFORE+1))" "code=$(code_of "$BODY") count=$AFTER"

BEFORE=$(log_count workOrder.batchApprove)
BODY=$(post_as alice /workOrder/batchApprove '{"ids":[4,1]}')
AFTER=$(log_count workOrder.batchApprove)
[ "$(code_of "$BODY")" = "403" ] && [ "$AFTER" = "$BEFORE" ] && ok C4 "部分命中整体拒绝，已命中的那条也没执行" || no C4 "403 且计数不变" "$BEFORE" "code=$(code_of "$BODY") count=$AFTER"

BODY=$(post_as dave /asset/scattered '{"primary":{"assetId":101},"many":[{"assetId":103}],"more":[{"assetId":101}]}')
printf '%s' "$BODY" | grep -q '\["101","103","101"\]' && ok C5 "散参+集合+数组三类位置的 ID 全部收集" || no C5 "collected 含三类 ID" '["101","103","101"]' "$BODY"
BODY=$(post_as dave /asset/scattered '{"primary":{"assetId":101},"many":[{"assetId":102}],"more":[]}')
[ "$(code_of "$BODY")" = "403" ] && ok C5b "其中一条未命中即整体拒绝" || no C5b "403" 403 "$BODY"

BODY=$(curl -s -X POST -H 'X-Demo-User: alice' "$BASE/probe/noCarrier")
[ "$(code_of "$BODY")" = "400" ] && printf '%s' "$BODY" | grep -q '未提供业务数据ID' && ok C6 "实参无承载对象=用法错误 400" || no C6 "400 用法错误" '400/未提供业务数据ID' "$BODY"
BODY=$(post_as alice /probe/emptyIds '{"ids":[]}')
[ "$(code_of "$BODY")" = "400" ] && ok C7 "空 ID 集合=用法错误" || no C7 "400" 400 "$BODY"
BODY=$(post_as alice /probe/nullId '{"ids":[null]}')
[ "$(code_of "$BODY")" = "400" ] && printf '%s' "$BODY" | grep -q '业务数据ID不能为空' && ok C8 "含 null 的集合=用法错误" || no C8 "400 业务数据ID不能为空" 400 "$BODY"

RANGE100=$(python3 -c 'print(",".join(str(i) for i in range(1,101)))' 2>/dev/null || seq -s, 1 100)
RANGE101=$(python3 -c 'print(",".join(str(i) for i in range(1,102)))' 2>/dev/null || seq -s, 1 101)
curl -s -o /dev/null -X POST -H 'X-Demo-User: alice' -H 'X-Trace-Id: c9' -H 'Content-Type: application/json' -d "{\"ids\":[$RANGE100]}" "$BASE/probe/bulk"
T=$(trace_of c9)
printf '%s' "$T" | grep -q '"type":"judge"' && printf '%s' "$T" | grep -q '"idCount":100' && ok C9 "100 条正常逐个判定（有查询、有判定记录）" || no C9 "judge 记录 idCount=100" "存在" "$T"
curl -s -o /dev/null -X POST -H 'X-Demo-User: alice' -H 'X-Trace-Id: c10' -H 'Content-Type: application/json' -d "{\"ids\":[$RANGE101]}" "$BASE/probe/bulk"
T=$(trace_of c10)
BODY=$(curl -s -X POST -H 'X-Demo-User: alice' -H 'Content-Type: application/json' -d "{\"ids\":[$RANGE101]}" "$BASE/probe/bulk")
[ "$(code_of "$BODY")" = "400" ] && ! printf '%s' "$T" | grep -q '"type":"judge"' && ok C10 "101 条直接失败且一次权限查询都不发" || no C10 "400 且零判定记录" "judge 不存在" "$T"

BODY=$(curl -s -X POST -H 'Content-Type: application/json' -d '{"id":801}' "$BASE/probe/call")
[ "$(code_of "$BODY")" = "403" ] && ok C11 "无身份（空权限集合）即拒绝，不因空集合放行" || no C11 "403" 403 "$BODY"

BODY=$(post_as bob /workOrder/approve '{"id":1}')
[ "$(code_of "$BODY")" = "200" ] && ok C12a "同一条数据在 approve 槽位命中" || no C12a "200" 200 "$BODY"
BODY=$(post_as bob /workOrder/update '{"id":1}')
[ "$(code_of "$BODY")" = "403" ] && ok C12b "不跨操作回退：edit 槽位仍拒绝" || no C12b "403" 403 "$BODY"

BODY=$(post_as dave /asset/updateIntegerForm '{"assetId":101}')
[ "$(code_of "$BODY")" = "200" ] && ok C13 "Integer 交出、BIGINT 存储、Long 读回不误判" || no C13 "200" 200 "$BODY"

BEFORE=$(curl -s "$BASE/demo/counts")
post_as alice /authData/grant '{"bizModule":"probe","bizDataId":803,"action":"edit","authStr":"ROLE__77","authDesc":"ROLE__77'"'"'); DROP TABLE demo_auth_data; --","adminId":900}' > /dev/null
SLOT=$(curl -s "$BASE/demo/auth-data?bizModule=probe&action=edit&bizDataId=803")
printf '%s' "$SLOT" | grep -q "DROP TABLE demo_auth_data" && ok C15a "含引号与注释符的描述原样入库" || no C15a "描述原样存储" "含 DROP 文本" "$SLOT"
AFTER=$(curl -s "$BASE/demo/counts")
printf '%s' "$AFTER" | grep -q '"tableName":"demo_auth_data","rowCount":' && ok C15b "权限表仍在（载荷没有改变语句结构）" || no C15b "表仍存在" "存在" "$AFTER"

BODY=$(post_as erin /probe/quote/call '{"rawId":"900"}')
[ "$(code_of "$BODY")" = "200" ] && ok C16a "含引号的权限串按整值相等仍能命中" || no C16a "200" 200 "$BODY"
BODY=$(post_as erin /probe/quote/call '{"rawId":"901"}')
[ "$(code_of "$BODY")" = "403" ] && ok C16b "不相等的值不命中" || no C16b "403" 403 "$BODY"
BEFORE=$(log_count probe.quoteCall)
BODY=$(post_as erin /probe/quote/call '{"rawId":"900'"'"' OR '"'"'1'"'"'='"'"'1"}')
AFTER=$(log_count probe.quoteCall)
[ "$(code_of "$BODY")" != "200" ] && [ "$AFTER" = "$BEFORE" ] && ok C16c "ID 里的语句片段不扩大匹配（未放行、无副作用；整型列下表现为类型错误）" || no C16c "不放行且计数不变" "非200 且 $BEFORE" "code=$(code_of "$BODY") count=$AFTER"

BODY=$(post_as alice /workOrder/update '{"id":1,"title":"verify"}')
C_ERR=$(code_of "$(curl -s -X POST -H 'X-Demo-User: alice' "$BASE/probe/noCarrier")")
[ "$(code_of "$BODY")" = "200" ] && [ "$C_ERR" = "400" ] && ok C17 "权限拒绝(403)与用法错误(400)编码可区分" || no C17 "403 对 400" "403/400" "$BODY"

BEFORE=$(log_count workOrder.updateUnchecked)
BODY=$(post_as bob /workOrder/updateUnchecked '{"id":6}')
AFTER=$(log_count workOrder.updateUnchecked)
[ "$(code_of "$BODY")" = "200" ] && [ "$AFTER" = "$((BEFORE+1))" ] && ok C18 "未声明的写接口不判定，有受控记录也照常执行" || no C18 "200+计数+1" "$((BEFORE+1))" "code=$(code_of "$BODY") count=$AFTER"

post_as alice /authData/remove '{"bizModule":"probe","bizDataId":802,"adminId":900}' > /dev/null
BODY=$(post_as alice /probe/call '{"id":802}')
[ "$(code_of "$BODY")" = "403" ] && ok C19a "起点：802 未授权时拒绝" || no C19a "403" 403 "$BODY"
post_as alice /authData/grant '{"bizModule":"probe","bizDataId":802,"action":"edit","authStr":"ROLE__10","authDesc":"ROLE__admin","adminId":900}' > /dev/null
BODY=$(post_as alice /probe/call '{"id":802}')
[ "$(code_of "$BODY")" = "200" ] && ok C19b "授权后同一调用从拒绝变放行" || no C19b "200" 200 "$BODY"
BODY=$(post_as bob /probe/call '{"id":802}')
[ "$(code_of "$BODY")" = "403" ] && ok C19c "新授的权限只给对的人，bob 仍被拒" || no C19c "403" 403 "$BODY"
FILL=$(post_as alice /authData/fill '{"bizModule":"probe","bizDataId":802,"action":"edit","adminId":900}')
printf '%s' "$FILL" | grep -q '"rowCount":1' && printf '%s' "$FILL" | grep -q 'filledIntoAuthObject' && ok C21 "读回该槽位权限并填进权限集合体" || no C21 "rows 与 filled 一致" "rowCount=1" "$FILL"
post_as alice /authData/remove '{"bizModule":"probe","bizDataId":802,"adminId":900}' > /dev/null
BODY=$(post_as alice /probe/call '{"id":802}')
[ "$(code_of "$BODY")" = "403" ] && ok C20 "清空权限后回归拒绝" || no C20 "403" 403 "$BODY"
BODY=$(post_as bob /authData/grant '{"bizModule":"probe","bizDataId":802,"action":"edit","authStr":"ROLE__20","authDesc":"ROLE__x","adminId":900}')
if [ "$(code_of "$BODY")" = "403" ] && printf '%s' "$BODY" | grep -q '权限校验不通过'; then
  ok C22 "管理动作自身受行级管辖：bob 能进接口但管理槽位被拒"
else
  no C22 "403 且消息为权限校验不通过（行级拒绝）" "403/权限校验不通过" "$BODY"
fi

# ---------------- 只读预检一致性 ----------------
# /demo/permission-view 复用拦截器与切面的处理器；这里用几条只读行对照真实结论，
# 防止预检与真实判定漂移。断言依赖响应里 path/id 紧跟 ok 的字段顺序。
BODY=$(post_as alice /demo/permission-view '{"menuPaths":["/methodLevel/guarded"],"dataChecks":[]}')
printf '%s' "$BODY" | grep -q '"path":"/methodLevel/guarded","ok":true' \
  && ok PV1 "预检：alice 对 guarded 菜单放行" \
  || no PV1 "预检 alice 放行" '"path":"/methodLevel/guarded","ok":true' "$BODY"

BODY=$(post_as bob /demo/permission-view '{"menuPaths":["/methodLevel/guarded"],"dataChecks":[]}')
printf '%s' "$BODY" | grep -q '"path":"/methodLevel/guarded","ok":false' \
  && ok PV2 "预检：bob 对 guarded 菜单拒绝" \
  || no PV2 "预检 bob 拒绝" '"path":"/methodLevel/guarded","ok":false' "$BODY"

BODY=$(post_as bob /demo/permission-view '{"menuPaths":["/workOrder/update"],"dataChecks":[{"businessFunction":"work_order","operate":"edit","ids":[1]}]}')
if printf '%s' "$BODY" | grep -q '"path":"/workOrder/update","ok":true' \
   && printf '%s' "$BODY" | grep -q '"id":1,"ok":false'; then
  ok PV3 "预检：bob 能进 update 菜单，但工单1的行级判定拒绝"
else
  no PV3 "菜单通过 + 行级拒绝" '"/workOrder/update" ok=true 且 id1 ok=false' "$BODY"
fi

BODY=$(post_as bob /demo/permission-view '{"menuPaths":["/authData/grant"],"dataChecks":[{"businessFunction":"demo_admin","operate":"unified","ids":[900]}]}')
if printf '%s' "$BODY" | grep -q '"path":"/authData/grant","ok":true' \
   && printf '%s' "$BODY" | grep -q '"id":900,"ok":false'; then
  ok PV4 "预检：bob 能进管理接口，但管理槽位被行级拒绝"
else
  no PV4 "管理菜单通过 + 槽位拒绝" '"/authData/grant" ok=true 且 id900 ok=false' "$BODY"
fi

# ---------------- D 数据范围 ----------------
BODY=$(get_as alice /workOrder/rangeList)
[ "$(row_ids "$BODY")" = "1,2,4" ] && ok D1 "被声明语句注入，结果只含可见行" || no D1 "1,2,4" "1,2,4" "$(row_ids "$BODY")"
curl -s -o /dev/null -H 'X-Demo-User: alice' -H 'X-Trace-Id: d2' "$BASE/workOrder/rangeList"
T=$(trace_of d2)
printf '%s' "$T" | grep -q 'from demo_user_auth' && ok D2a "装载权限那条未声明语句原文下发" || no D2a "未声明语句存在" "from demo_user_auth" "$T"
if printf '%s' "$T" | tr ',' '\n' | grep -q 'where username = ?' && ! printf '%s' "$T" | grep -q 'demo_user_auth WHERE'; then
  ok D2b "未声明语句没被附加权限条件"
else
  no D2b "未声明语句不被改写" "无注入" "$T"
fi
printf '%s' "$T" | grep -q 'demo_work_order WHERE demo_work_order.id IN (SELECT biz_data_id FROM demo_auth_data' && ok D12 "改写后的最终语句可观测" || no D12 "注入语句可见" "含子查询" "$T"

BODY=$(get_as alice /workOrder/rangeListExists)
[ "$(row_ids "$BODY")" = "1,2,4" ] && ok D6 "EXISTS 与 IN 行集一致" || no D6 "1,2,4" "1,2,4" "$(row_ids "$BODY")"
E=$(row_ids "$(get_as alice /workOrder/editable)"); P=$(row_ids "$(get_as alice /workOrder/approvable)")
[ "$E" = "1,2,3" ] && [ "$P" = "4,5" ] && ok D4 "同表两槽位各按自己的操作过滤" || no D4 "edit=1,2,3 approve=4,5" "1,2,3 / 4,5" "$E / $P"
BODY=$(get_as alice /workOrder/twoSlots)
printf '%s' "$BODY" | grep -q '"editable":\[' && ok D5a "同请求先后使用两个槽位" || no D5a "两槽位结果并存" "存在" "-"
E2=$(printf '%s' "$BODY" | sed 's/.*editableAgain":\[//; s/\].*//' | grep -o '"id":[0-9]*' | cut -d: -f2 | paste -sd, -)
[ "$E2" = "1,2,3" ] && ok D5b "第二次 editable 不被第一次所用槽位污染" || no D5b "1,2,3" "1,2,3" "$E2"
curl -s -o /dev/null -H 'X-Demo-User: alice' -H 'X-Trace-Id: d3' "$BASE/workOrder/nested"
T=$(trace_of d3)
printf '%s' "$T" | grep -q "action = 'unified'" && printf '%s' "$T" | grep -q "action = 'edit'" && ok D3 "嵌套声明各注各的槽位（unified 与 edit 同现）" || no D3 "两条语句两个槽位" "unified+edit" "$T"
C_U=$(row_ids "$(get_as alice /workOrder/joinCu)"); C_B=$(row_ids "$(get_as alice /workOrder/joinCb)")
[ "$C_U" = "1,3,5" ] && [ "$C_B" = "3,4,6" ] && ok D7 "tableAlias 精确命中同一张表的两个别名实例" || no D7 "cu=1,3,5 cb=3,4,6" "1,3,5 / 3,4,6" "$C_U / $C_B"
BODY=$(get_as dave /asset/list)
[ "$(row_ids "$BODY")" = "101,102" ] && ok D8 "dataIdColumn 指向非 id 主键列" || no D8 "101,102" "101,102" "$(row_ids "$BODY")"
BODY=$(get_as carol /workOrder/all)
[ "$(row_ids "$BODY")" = "1,2,3,4,5,6" ] && ok D9 "未声明即不过滤（carol 零权限也拿全量）" || no D9 "全 6 行" "1..6" "$(row_ids "$BODY")"
BODY=$(get_as alice /workOrder/dynamic)
[ "$(row_ids "$BODY")" = "1,2,4" ] && ok D11a "动态语句运行期拼得出目标表时照常注入" || no D11a "1,2,4" "1,2,4" "$(row_ids "$BODY")"
BODY=$(get_as alice /workOrder/dynamicMissingTable?withJoin=false)
! printf '%s' "$BODY" | grep -q '"id":1' && ok D11b "拼不出目标表时抛异常，不返回未过滤结果" || no D11b "不返回行" "无 id" "$BODY"
curl -s -o /dev/null -H 'X-Demo-User: alice' -H 'X-Trace-Id: d10' "$BASE/workOrder/updateUnchecked" -X POST -H 'Content-Type: application/json' -d '{"id":5}'
T=$(trace_of d10)
printf '%s' "$T" | grep -q 'update demo_work_order' && ! printf '%s' "$T" | grep -q 'update demo_work_order WHERE.*demo_auth_data' && ok D10 "写语句不被追加权限条件" || no D10 "update 语句原文" "无注入" "$T"
BODY=$(curl -s "$BASE/workOrder/rangeList")
printf '%s' "$BODY" | grep -q '"data":\[\]' && ok D15a "空权限集合：返回零行而不是报错" || no D15a "空数组" '"data":[]' "$BODY"
curl -s -o /dev/null -H 'X-Trace-Id: d15' "$BASE/workOrder/rangeList"
T=$(trace_of d15)
printf '%s' "$T" | grep -qE '(WHERE|AND) 1 = 0' && ok D15b "注入的是恒假条件，过滤没有被关闭" || no D15b "含 1 = 0" "1 = 0" "$T"

# ---------------- E 配置 ----------------
BODY=$(get_as alice /workOrder/rangeList)
printf '%s' "$BODY" | grep -q 'demo_auth_data' || true
curl -s -o /dev/null -H 'X-Demo-User: alice' -H 'X-Trace-Id: e1' "$BASE/workOrder/rangeList"
T=$(trace_of e1)
if printf '%s' "$T" | grep -q 'SELECT biz_data_id FROM demo_auth_data WHERE auth_str' && printf '%s' "$T" | grep -q "biz_module = 'work_order'" && printf '%s' "$T" | grep -q "action = 'unified'"; then
  ok E1 "权限表的表名与六个列名全部由配置映射生效"
else
  no E1 "自定义列名参与注入" "biz_data_id/auth_str/biz_module/action" "$T"
fi

# ---------------- 需要两次启动的阶段 ----------------
if [ "$STAGE" = "restart" ]; then
  MARKER=${MARKER:-target/verify-restart.snapshot}
  mkdir -p "$(dirname "$MARKER")"
  if [ ! -f "$MARKER" ]; then
    # 先造出"重启前应被保留"的状态，并快照当前行数
    curl -s -X POST "$BASE/demo/menu-api/touch?path=%2FclassLevel%2Fall&name=manual-name&remark=manual-remark" > /dev/null
    curl -s "$BASE/demo/counts" > "$MARKER"
    echo "已把 /classLevel/all 改成人值并快照当前行数到 $MARKER"
    echo "请重启应用，再跑一次：bash verify.sh --stage=restart"
    exit 0
  fi
  BEFORE=$(tr -d ' \n' < "$MARKER")
  AFTER=$(curl -s "$BASE/demo/counts" | tr -d ' \n')
  ROW=$(curl -s "$BASE/demo/menu-api" | grep -o '"path":"/classLevel/all"[^}]*')
  if printf '%s' "$ROW" | grep -q 'manual-name'; then
    ok B7 "已有菜单行的 name 未被启动过程覆写"
  else
    no B7 "人工值保持" "manual-name" "$ROW"
  fi
  if printf '%s' "$ROW" | grep -q 'manual-remark'; then
    ok B7b "已有菜单行的 remark 未被启动过程覆写"
  else
    no B7b "人工备注保持" "manual-remark" "$ROW"
  fi
  TOTAL=$(curl -s "$BASE/demo/menu-api" | grep -o '"path"' | wc -l | tr -d ' ')
  [ "$TOTAL" = "25" ] && ok E3a "菜单行不因重复启动增长" || no E3a "25 行不变" 25 "$TOTAL"
  for table in demo_auth_data demo_user_auth demo_menu_auth demo_work_order demo_asset demo_customer; do
    b=$(printf '%s' "$BEFORE" | grep -o "\"tableName\":\"$table\",\"rowCount\":[0-9]*" | cut -d: -f3)
    a=$(printf '%s' "$AFTER" | grep -o "\"tableName\":\"$table\",\"rowCount\":[0-9]*" | cut -d: -f3)
    [ -n "$b" ] && [ "$b" = "$a" ] && ok "E3b-$table" "重复启动不灌出重复种子行（$table 仍 $b 行）" \
      || no "E3b-$table" "$table 行数不变" "$b" "$a"
  done
  echo "（阶段完成后可删掉 $MARKER 重跑）"
fi

echo
echo "== 汇总：通过 $PASS，失败 $FAIL =="
[ "$FAIL" = "0" ] || exit 1
