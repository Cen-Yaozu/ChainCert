#!/bin/bash

# FISCO BCOS 节点配置生成脚本
# 用于生成单节点开发环境

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
NODES_DIR="${SCRIPT_DIR}/nodes"

echo "=========================================="
echo "FISCO BCOS 节点配置生成脚本"
echo "=========================================="

# 检查是否已存在节点配置
if [ -d "${NODES_DIR}/127.0.0.1" ]; then
    echo "节点配置已存在，跳过生成"
    echo "如需重新生成，请先删除 ${NODES_DIR} 目录"
    exit 0
fi

# 下载 build_chain.sh 脚本
echo "下载 FISCO BCOS 构建脚本..."
curl -#LO https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/v2.9.1/build_chain.sh
chmod +x build_chain.sh

# 生成单节点配置
echo "生成单节点配置..."
bash build_chain.sh -l 127.0.0.1:1 -p 30300,20200,8545 -o "${NODES_DIR}"

# 清理下载的脚本
rm -f build_chain.sh

echo "=========================================="
echo "节点配置生成完成！"
echo "节点目录: ${NODES_DIR}/127.0.0.1/node0"
echo "=========================================="

# 显示生成的文件
echo ""
echo "生成的文件列表:"
ls -la "${NODES_DIR}/127.0.0.1/node0/"

echo ""
echo "SDK 证书目录:"
ls -la "${NODES_DIR}/127.0.0.1/sdk/"