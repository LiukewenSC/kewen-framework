#!/usr/bin/env bash
# 校验 docs/application.yml 覆盖所有 @ConfigurationProperties 与 additional metadata 中的配置键。
# 前置：先在仓库根执行 mvn -q -DskipTests compile，生成各模块的 spring-configuration-metadata.json。
# List 元素字段（clients[] / registrations[]）不在 metadata 中，本脚本不覆盖，需人工核对。
#
# 可用环境变量：SNAKEYAML_JAR（snakeyaml jar 路径）、JAVA_BIN（java 可执行文件）
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CHECKER="$ROOT/scripts/CheckConfigDocs.java"
DOC="$ROOT/docs/application.yml"
SNAKEYAML_JAR=${SNAKEYAML_JAR:-$(ls -t "$HOME"/.m2/repository/org/yaml/snakeyaml/*/snakeyaml-[0-9]*.jar 2>/dev/null | grep -vE -- '-(sources|javadoc)\.jar$' | head -1)}
JAVA=${JAVA_BIN:-java}

[ -f "$CHECKER" ] || { echo "找不到 $CHECKER"; exit 1; }
[ -f "$DOC" ] || { echo "找不到 $DOC"; exit 1; }
[ -f "$SNAKEYAML_JAR" ] || { echo "找不到 snakeyaml jar，先执行 mvn compile，或用 SNAKEYAML_JAR 指定"; exit 1; }

mapfile -t METADATA < <(find "$ROOT" -path '*/target/classes/META-INF/spring-configuration-metadata.json' | sort)
[ "${#METADATA[@]}" -gt 0 ] || { echo "没有发现任何 spring-configuration-metadata.json，先执行 mvn compile"; exit 1; }

# 统一成 C:/ 形式的绝对路径，Java 与 SnakeYAML 都认
if command -v cygpath >/dev/null 2>&1; then
  DOC="$(cygpath -m "$DOC")"
  SNAKEYAML_JAR="$(cygpath -m "$SNAKEYAML_JAR")"
  for i in "${!METADATA[@]}"; do METADATA[$i]="$(cygpath -m "${METADATA[$i]}")"; done
fi

"$JAVA" -Dfile.encoding=UTF-8 -cp "$SNAKEYAML_JAR" "$CHECKER" "$DOC" "${METADATA[@]}"
