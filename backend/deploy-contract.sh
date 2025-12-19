#!/bin/bash

# 证书存证合约部署脚本
# 用于在本地 FISCO BCOS 节点上部署智能合约

echo "=========================================="
echo "证书存证系统 - 智能合约部署"
echo "=========================================="
echo ""

# 检查 FISCO BCOS 节点是否运行
echo "1. 检查 FISCO BCOS 节点状态..."
BLOCK_NUMBER=$(curl -s -X POST --data '{"jsonrpc":"2.0","method":"getBlockNumber","params":[1],"id":1}' \
  -H "Content-Type: application/json" http://localhost:8545 | grep -o '"result":"[^"]*"' | cut -d'"' -f4)

if [ -z "$BLOCK_NUMBER" ]; then
    echo "❌ 错误: 无法连接到 FISCO BCOS 节点 (localhost:8545)"
    echo "   请确保节点已启动: cd docker/fisco && docker-compose up -d"
    exit 1
fi

echo "✅ FISCO BCOS 节点运行正常，当前区块高度: $BLOCK_NUMBER"
echo ""

# 编译项目
echo "2. 编译 Spring Boot 项目..."
cd "$(dirname "$0")"
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ 项目编译失败"
    exit 1
fi

echo "✅ 项目编译成功"
echo ""

# 部署合约
echo "3. 部署智能合约..."
echo "   正在连接区块链节点并部署合约..."
echo ""

# 使用 Spring Boot 的方式运行部署任务
mvn spring-boot:run -Dspring-boot.run.arguments="--deploy-contract" -q &
DEPLOY_PID=$!

# 等待部署完成（最多等待30秒）
COUNTER=0
while [ $COUNTER -lt 30 ]; do
    # 检查日志文件中是否有合约地址
    if [ -f "logs/certificate-system.log" ]; then
        CONTRACT_ADDRESS=$(grep -o "合约地址: 0x[a-fA-F0-9]*" logs/certificate-system.log | tail -1 | cut -d' ' -f2)
        if [ ! -z "$CONTRACT_ADDRESS" ]; then
            echo "✅ 合约部署成功！"
            echo ""
            echo "=========================================="
            echo "合约地址: $CONTRACT_ADDRESS"
            echo "=========================================="
            echo ""
            echo "请将此地址配置到 application.yml 中："
            echo ""
            echo "fisco:"
            echo "  contract:"
            echo "    address: $CONTRACT_ADDRESS"
            echo ""
            
            # 停止 Spring Boot 进程
            kill $DEPLOY_PID 2>/dev/null
            exit 0
        fi
    fi
    
    sleep 1
    COUNTER=$((COUNTER + 1))
    echo -n "."
done

echo ""
echo "❌ 部署超时或失败"
echo "   请查看日志: logs/certificate-system.log"
kill $DEPLOY_PID 2>/dev/null
exit 1