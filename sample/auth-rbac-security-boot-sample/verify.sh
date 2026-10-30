#!/usr/bin/env bash
# auth-rbac-security-boot-sample 的端到端验证脚本。
#
# 用法：
#   bash verify.sh
#   BASE=http://localhost:8083 bash verify.sh
#
# 依赖：curl。不启动应用、不改业务数据。
# 用例编号与 README 的身份 × 端点真值表一一对应：
#   1 /currentUser          2 /test/checkMenu        3 /test/checkMenuNone
#   4 /test/dataRange       5 /test/dataEdit         6 /deptInherit/rooms
set -uo pipefail

BASE=${BASE:-http://localhost:8083}
TMPD=$(mktemp -d)
trap 'rm -rf "$TMPD"' EXIT

PASS=0
FAIL=0

ok() { PASS=$((PASS + 1)); printf 'PASS  %-8s %s\n' "$1" "$2"; }
no() {
    FAIL=$((FAIL + 1))
    printf 'FAIL  %-8s %s\n      期望: %s\n      实际: %s\n' "$1" "$2" "$3" "$4"
}

code_of() { printf '%s' "$1" | grep -o '"code":[0-9]*' | head -1 | cut -d: -f2; }
success_of() { printf '%s' "$1" | grep -q '"success":true' && echo true || echo false; }
row_count() { printf '%s' "$1" | grep -o '"id":[0-9]*' | wc -l | tr -d ' '; }
has_authority() { printf '%s' "$1" | grep -q "\"authority\":\"$2\"" && echo true || echo false; }

login() {
    local user=$1
    local body
    body=$(curl -s -c "$TMPD/$user.jar" -X POST \
        -H 'Content-Type: application/json' \
        -d "{\"username\":\"$user\",\"password\":\"123456\"}" \
        "$BASE/login")
    if [ "$(success_of "$body")" = "true" ]; then
        ok "login" "账号 $user 登录成功"
    else
        no "login" "账号 $user 登录成功" "success=true" "$body"
    fi
}

get_as() {
    curl -s -b "$TMPD/$1.jar" "$BASE$2"
}

post_as() {
    local user=$1 path=$2 body=$3
    curl -s -b "$TMPD/$user.jar" -X POST \
        -H 'Content-Type: application/json' \
        -d "$body" "$BASE$path"
}

expect_code() {
    local id=$1 user=$2 method=$3 path=$4 expected=$5 body=${6:-}
    local response actual
    if [ "$method" = "POST" ]; then
        response=$(post_as "$user" "$path" "$body")
    else
        response=$(get_as "$user" "$path")
    fi
    actual=$(code_of "$response")
    if [ "$actual" = "$expected" ]; then
        ok "$id" "$user $method $path -> code=$actual"
    else
        no "$id" "$user $method $path" "code=$expected" "code=$actual $response"
    fi
}

expect_total() {
    local id=$1 user=$2 expected=$3
    local response actual
    response=$(get_as "$user" "/test/dataRange")
    actual=$(row_count "$response")
    if [ "$(code_of "$response")" = "200" ] && [ "$actual" = "$expected" ]; then
        ok "$id" "$user GET /test/dataRange -> 200 / $actual 行"
    else
        no "$id" "$user GET /test/dataRange" "200 / $expected 行" "$response"
    fi
}

echo "== auth-rbac-security-boot-sample 验证（目标：$BASE）=="
if [ "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/api/login/options")" != "200" ]; then
    echo "应用没在跑：先按 README 启动，BASE 可用环境变量覆盖"
    exit 2
fi

# ---------------- 逐身份验证 ----------------
for user in admin admin2 subordinate outsider; do
    echo "-- $user"
    login "$user"

    # 1 登录态
    RESP=$(get_as "$user" "/currentUser")
    if [ "$(code_of "$RESP")" = "200" ] \
        && printf '%s' "$RESP" | grep -q "\"username\":\"$user\""; then
        ok 1 "$user GET /currentUser -> $user"
    else
        no 1 "$user GET /currentUser" "code=200 且 username=$user" "$RESP"
    fi

    if [ "$user" = "admin" ]; then
        if [ "$(has_authority "$RESP" "ROLE__1")" = "true" ]; then
            ok 1b "admin 的权限集合含 ROLE__1"
        else
            no 1b "admin 的权限集合含 ROLE__1" '"authority":"ROLE__1"' "$RESP"
        fi
    else
        if [ "$(has_authority "$RESP" "ROLE__1")" = "false" ]; then
            ok 1b "$user 的权限集合不含 ROLE__1"
        else
            no 1b "$user 的权限集合不含 ROLE__1" '不含 "authority":"ROLE__1"' "$RESP"
        fi
    fi

    # 2 菜单：/test 行授权 ROLE__1，checkMenu 继承父级
    if [ "$user" = "admin" ]; then
        expect_code 2 "$user" GET /test/checkMenu 200
    else
        expect_code 2 "$user" GET /test/checkMenu 403
    fi

    # 3 菜单反例：没有任何授权行
    expect_code 3 "$user" GET /test/checkMenuNone 403

    # 4 数据范围：只把 data_id=1 授权给 ROLE__1
    if [ "$user" = "admin" ]; then
        expect_total 4 "$user" 1
    else
        expect_total 4 "$user" 0
    fi

    # 5 行级数据操作：testedit 槽位
    if [ "$user" = "admin" ]; then
        expect_code 5 "$user" POST /test/dataEdit 200 '{"id":1,"name":"verify"}'
    else
        expect_code 5 "$user" POST /test/dataEdit 403 '{"id":1,"name":"verify"}'
    fi

    # 6 部门继承：只有总部的严格子孙（subordinate 在研发部）放行
    if [ "$user" = "subordinate" ]; then
        expect_code 6 "$user" GET /deptInherit/rooms 200
    else
        expect_code 6 "$user" GET /deptInherit/rooms 403
    fi

    curl -s -b "$TMPD/$user.jar" -X POST "$BASE/logout" >/dev/null
done

echo
echo "结果：通过 $PASS，失败 $FAIL"
[ "$FAIL" -eq 0 ]
