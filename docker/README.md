# 区块链证书存证系统 - 本地开发环境部署指南

## 概述

本文档介绍如何在本地搭建完整的开发环境，包括 IPFS 和 FISCO BCOS 区块链节点。

## 环境架构

```
┌─────────────────────────────────────────────────────────────┐
│                    本地开发环境                              │
├─────────────────┬─────────────────┬─────────────────────────┤
│   IPFS 节点     │  FISCO BCOS     │    Spring Boot 后端      │
│   Port: 5001    │  节点           │    Port: 8080           │
│   Port: 8081    │  Port: 20200    │                         │
│                 │  Port: 8545     │                         │
└─────────────────┴─────────────────┴─────────────────────────┘
```

## 一、IPFS 环境部署

### 1.1 启动 IPFS 节点

```bash
cd docker/ipfs
docker-compose up -d
```

### 1.2 验证 IPFS 服务

```bash
# 检查节点信息
curl -X POST http://localhost:5001/api/v0/id

# 测试文件上传
echo "Hello IPFS" | curl -X POST -F "file=@-" http://localhost:5001/api/v0/add

# 测试文件下载（使用上一步返回的 Hash）
curl -X POST "http://localhost:5001/api/v0/cat?arg=<HASH>"
```

### 1.3 IPFS 配置说明

- **API 端口**: 5001 - 用于程序调用
- **Gateway 端口**: 8081 - HTTP 网关访问（避免与后端 8080 冲突）
- **数据持久化**: 使用 Docker volume `ipfs_ipfs_data`

## 二、FISCO BCOS 环境部署

### 2.1 生成节点配置（首次部署）

节点配置已预先生成在 `docker/fisco/nodes/` 目录下。如需重新生成：

```bash
cd docker/fisco
# 删除现有配置
rm -rf nodes

# 重新生成（使用 Docker）
docker run --rm -v "$(pwd)/nodes:/output" ubuntu:20.04 /bin/bash -c \
  "apt-get update -qq && apt-get install -y -qq curl openssl && \
   curl -#LO https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/v2.9.1/build_chain.sh && \
   chmod +x build_chain.sh && \
   bash build_chain.sh -l 127.0.0.1:1 -p 30300,20200,8545 -o nodes && \
   cp -r nodes/* /output/"
```

### 2.2 启动 FISCO BCOS 节点

```bash
cd docker/fisco
docker-compose up -d
```

### 2.3 验证区块链服务

```bash
# 检查容器状态
docker ps | grep fisco

# 查看节点日志
docker logs fisco-node

# 测试 JSON-RPC 接口
curl -X POST --data '{"jsonrpc":"2.0","method":"getBlockNumber","params":[1],"id":1}' \
  -H "Content-Type: application/json" http://localhost:8545
```

### 2.4 FISCO BCOS 配置说明

- **Channel 端口**: 20200 - SDK 连接端口
- **JSON-RPC 端口**: 8545 - JSON-RPC 接口
- **P2P 端口**: 30300 - 节点间通信
- **证书目录**: `nodes/127.0.0.1/sdk/` - 包含 ca.crt, sdk.crt, sdk.key

## 三、后端服务配置

### 3.1 IPFS 配置

在 `backend/src/main/resources/application.yml` 中：

```yaml
# IPFS 配置
ipfs:
  enabled: true          # 启用真实 IPFS 连接
  host: localhost        # 本地 Docker IPFS 节点
  port: 5001
  timeout: 30000
  gateway-port: 8081
```

### 3.2 区块链配置

```yaml
# 区块链开关
blockchain:
  enabled: true          # 启用区块链功能

# FISCO BCOS 配置
fisco:
  nodes:
    - 127.0.0.1:20200   # 本地节点
  group-id: 1
  account:
    private-key: ${FISCO_PRIVATE_KEY:}  # 可选：指定私钥
  contract:
    address: ${CONTRACT_ADDRESS:}        # 部署后的合约地址
```

### 3.3 证书文件

SDK 证书已复制到 `backend/src/main/resources/fisco/` 目录：
- `ca.crt` - CA 证书
- `sdk.crt` - SDK 证书
- `sdk.key` - SDK 私钥

## 四、环境切换

### 4.1 本地开发环境（默认）

使用 `application.yml` 中的配置：
- IPFS: `localhost:5001`
- FISCO BCOS: `127.0.0.1:20200`

### 4.2 切换到远程环境

创建 `application-prod.yml` 或使用环境变量：

```yaml
# application-prod.yml
ipfs:
  enabled: true
  host: 139.196.72.119   # 远程 IPFS 服务器
  port: 5001

fisco:
  nodes:
    - 139.196.72.119:20200  # 远程区块链节点
```

启动时指定 profile：
```bash
java -jar certificate-system.jar --spring.profiles.active=prod
```

或使用环境变量：
```bash
export IPFS_HOST=139.196.72.119
export FISCO_NODES=139.196.72.119:20200
```

### 4.3 禁用区块链功能

如果只想测试基本功能，可以禁用区块链：

```yaml
blockchain:
  enabled: false

ipfs:
  enabled: false  # 使用 Mock 实现
```

## 五、常用命令

### 5.1 启动所有服务

```bash
# 启动 IPFS
cd docker/ipfs && docker-compose up -d

# 启动 FISCO BCOS
cd docker/fisco && docker-compose up -d

# 启动后端（在项目根目录）
cd backend && mvn spring-boot:run
```

### 5.2 停止所有服务

```bash
# 停止 IPFS
cd docker/ipfs && docker-compose down

# 停止 FISCO BCOS
cd docker/fisco && docker-compose down
```

### 5.3 查看日志

```bash
# IPFS 日志
docker logs ipfs-node -f

# FISCO BCOS 日志
docker logs fisco-node -f

# 后端日志
tail -f backend/logs/certificate-system.log
```

### 5.4 清理数据

```bash
# 清理 IPFS 数据
docker-compose -f docker/ipfs/docker-compose.yml down -v

# 清理 FISCO BCOS 数据
docker-compose -f docker/fisco/docker-compose.yml down -v
```

## 六、故障排查

### 6.1 IPFS 连接失败

1. 检查容器状态：`docker ps | grep ipfs`
2. 查看日志：`docker logs ipfs-node`
3. 测试连接：`curl http://localhost:5001/api/v0/id`

### 6.2 FISCO BCOS 连接失败

1. 检查容器状态：`docker ps | grep fisco`
2. 查看日志：`docker logs fisco-node`
3. 测试 JSON-RPC：`curl -X POST --data '{"jsonrpc":"2.0","method":"getBlockNumber","params":[1],"id":1}' http://localhost:8545`
4. 检查证书文件是否存在：`ls backend/src/main/resources/fisco/`

### 6.3 后端启动失败

1. 检查 `application.yml` 配置
2. 确认 IPFS 和 FISCO BCOS 服务已启动
3. 查看后端日志：`backend/logs/certificate-system.log`

## 七、端口占用说明

| 服务 | 端口 | 说明 |
|------|------|------|
| IPFS API | 5001 | IPFS HTTP API |
| IPFS Gateway | 8081 | IPFS 网关（避免与后端冲突） |
| FISCO Channel | 20200 | SDK 连接端口 |
| FISCO JSON-RPC | 8545 | JSON-RPC 接口 |
| FISCO P2P | 30300 | 节点间通信 |
| Spring Boot | 8080 | 后端 API 服务 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |

## 八、下一步

1. **部署智能合约**：使用 WeBase 或控制台部署 `CertificateRegistry.sol`
2. **配置合约地址**：将部署后的合约地址配置到 `application.yml`
3. **测试完整流程**：申请 → 审批 → 存证 → 验证

## 参考资料

- [FISCO BCOS 官方文档](https://fisco-bcos-documentation.readthedocs.io/)
- [IPFS 官方文档](https://docs.ipfs.tech/)
- [项目技术规格文档](../docs/SPEC.md)