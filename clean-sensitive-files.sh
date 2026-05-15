#!/bin/bash

# 清理 Git 历史中的敏感文件
# 这个脚本会从 Git 历史中完全删除敏感文件

echo "========================================="
echo "开始清理 Git 历史中的敏感文件"
echo "========================================="

# 设置严格模式
set -e

# 清理所有 .jks 文件的历史
echo "正在清理 .jks 文件的历史..."
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch **/*.jks' \
  --prune-empty --tag-name-filter cat -- --all

# 清理 logs 目录的历史
echo "正在清理 logs/ 目录的历史..."
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch -r **/logs/' \
  --prune-empty --tag-name-filter cat -- --all

# 清理 target 目录的历史
echo "正在清理 target/ 目录的历史..."
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch -r **/target/' \
  --prune-empty --tag-name-filter cat -- --all

echo "========================================="
echo "清理完成！"
echo "========================================="
echo ""
echo "接下来请执行以下步骤："
echo "1. 检查清理结果: git log --all --oneline"
echo "2. 强制推送到远程仓库: git push origin --force --all"
echo "3. 强制推送标签: git push origin --force --tags"
echo "4. 清理本地引用: git for-each-ref --format='delete %(refname)' refs/original/ | git update-ref --stdin"
echo "5. 清理垃圾: git reflog expire --expire=now --all && git gc --prune=now --aggressive"
echo ""
echo "⚠️  警告：这些操作会修改 Git 历史，请确保其他开发者已知悉！"
