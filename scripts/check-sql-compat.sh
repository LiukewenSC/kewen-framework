#!/usr/bin/env bash
# 用 H2 的 MySQL 兼容模式实测 docs/sql 与各 sample/sql 下的脚本。
# 校验三件事：每条语句 H2 都接受、脚本可重复执行（spring.sql.init 每次启动都会重跑）、表真的建出来了。
# MySQL 侧不由这里验证：脚本只写两边都接受的语法，改完仍需在 MySQL 上过一遍。
#
# 可用环境变量：H2_JAR（h2 jar 路径）、JAVA_BIN（java 可执行文件）
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CHECKER="$ROOT/scripts/CheckCompat.java"
# 只要驱动包，sources/javadoc 里没有 org.h2.Driver
H2_JAR=${H2_JAR:-$(ls -t "$HOME"/.m2/repository/com/h2database/h2/*/h2-[0-9]*.jar 2>/dev/null | grep -vE -- '-(sources|javadoc)\.jar$' | head -1)}
JAVA=${JAVA_BIN:-java}

[ -f "$CHECKER" ] || { echo "找不到 $CHECKER"; exit 1; }
[ -f "$H2_JAR" ] || { echo "找不到 h2 jar，先用 mvn dependency:get 拉一个，或用 H2_JAR 指定"; exit 1; }

mapfile -t FILES < <(ls "$ROOT"/docs/sql/*.sql "$ROOT"/sample/*/sql/*.sql 2>/dev/null | sort)
[ "${#FILES[@]}" -gt 0 ] || { echo "没有发现任何 .sql"; exit 1; }
# 统一成 C:/ 形式的绝对路径，Java 与 H2 都认
if command -v cygpath >/dev/null 2>&1; then
  for i in "${!FILES[@]}"; do FILES[$i]="$(cygpath -m "${FILES[$i]}")"; done
fi

echo "检查 ${#FILES[@]} 个脚本："
for f in "${FILES[@]}"; do echo "  $f"; done
echo

"$JAVA" -Dfile.encoding=UTF-8 -cp "$H2_JAR" "$CHECKER" "${FILES[@]}"
rc=$?
[ $rc -eq 0 ] || echo "结论：存在 H2(MySQL 模式) 不接受的写法，或对象没建成"
exit $rc
