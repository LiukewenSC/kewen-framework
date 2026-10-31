#!/usr/bin/env bash
# 框架版本号的字面量散落在 5 类位置，升版本时漏掉任何一处都会构建失败或静默取到旧版本：
#   1) 根 pom.xml 的 <project><version>            —— cu-parent 自身版本
#   2) 根 pom.xml 里 cu-dependencies 的 import 版本 —— 框架内部取版本表的入口
#   3) dependencies/pom.xml 的 <project><version>   —— BOM 自身版本（外部 import 时写的就是它）
#   4) dependencies/pom.xml 的 <framework-version>  —— BOM 内全部 com.cu.framework 坐标的版本
#   5) 每个子 POM 的 <parent><version>              —— Maven 要求字面量，不能用属性
# 这里逐个比对，全部必须等于 1)。
#
# 版本单源改造（${revision} + flatten-maven-plugin）是另一件事；在那之前靠这个脚本兜。
# 可用环境变量：VERSION_SYNC_ROOT（被检查的仓库根，默认取脚本所在目录的上一级；自检夹具用）
# 扫描排除 target/ 与 logs/：夹具或构建产物里的 pom 曾被当成子模块报“读不到 parent”。
set -uo pipefail

ROOT="${VERSION_SYNC_ROOT:-$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)}"
cd "$ROOT"

# 取紧跟在指定 artifactId 之后的那个 <version> 值；参数1=文件 参数2=artifactId 参数3=允许的行偏移
grab_after() {
  local file="$1" aid="$2"
  grep -A1 "<artifactId>${aid}</artifactId>" "$file" 2>/dev/null \
    | sed -n 's|.*<version>\([^<]*\)</version>.*|\1|p' | head -1
}

EXPECTED=$(grab_after pom.xml cu-parent)
[ -n "$EXPECTED" ] || { echo "读不到根 pom.xml 里 cu-parent 的 <version>"; exit 1; }

FAIL=0
report() {
  # $1=文件 $2=实际值 $3=说明；按实际值定位行号，偏离的版本号一般在文件里唯一
  local line
  line=$(grep -n -F -- "$2" "$1" | head -1 | cut -d: -f1)
  printf '  %-46s %-11s (期望 %s) %s\n' "$1" "$2" "$EXPECTED" "[$3:${line:-?}]"
  FAIL=$((FAIL + 1))
}

# 值读不到要单独说清楚，否则表格里只剩一个空白格，看不出是缺失还是不匹配
cmp_val() {
  # $1=文件 $2=实际值 $3=说明
  if [ -z "$2" ]; then
    printf '  %-46s %-11s (期望 %s) %s\n' "$1" "读不到" "$EXPECTED" "[$3:?]"
    FAIL=$((FAIL + 1))
  elif [ "$2" != "$EXPECTED" ]; then
    report "$1" "$2" "$3"
  fi
}

echo "根 POM 的版本基准：$EXPECTED"

# 1) 根自身的 project/version 就是基准，不必再比一次
# 2) 根里 import cu-dependencies 的版本
cmp_val pom.xml "$(grab_after pom.xml cu-dependencies)" "bom-import"
# 3) 4) BOM 自身版本与 framework-version 属性
cmp_val dependencies/pom.xml "$(grab_after dependencies/pom.xml cu-dependencies)" "bom-version"
cmp_val dependencies/pom.xml "$(sed -n 's|.*<framework-version>\([^<]*\)</framework-version>.*|\1|p' dependencies/pom.xml | head -1)" "framework-version"

# 5) 所有子 POM 的 <parent><version>；dependencies/pom.xml 按设计没有 parent，跳过
while IFS= read -r pom; do
  [ "$pom" = "./pom.xml" ] && continue
  [ "$pom" = "./dependencies/pom.xml" ] && continue
  V=$(awk '/<parent>/{p=1} p && /<version>/{gsub(/.*<version>|<\/version>.*/,""); print; exit} /<\/parent>/{p=0}' "$pom")
  cmp_val "$pom" "$V" "parent/version"
done < <(find . -name pom.xml -not -path "./.git/*" -not -path "*/target/*" -not -path "./logs/*" | sort)

echo
if [ "$FAIL" -gt 0 ]; then
  echo "版本不一致：$FAIL 处。升版本时 5 类位置必须一起改。"
  exit 1
fi
echo "版本一致：全部对齐 $EXPECTED"
