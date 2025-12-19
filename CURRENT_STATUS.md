# 当前系统状态

## ✅ 已运行的服务

### 1. IPFS 节点 (运行中)
```
容器名称: ipfs-node
镜像: ipfs/kubo:v0.24.0
状态: Up About an hour (healthy)
端口映射:
  - 5001:5001 (API)
  - 8081:8080 (Gateway)
  - 4001:4001 (P2P)
```

**验证结果**: ✅ 正常
```bash
curl -X POST http://localhost:5001/api/v0/id
# 返回节点ID: 12D3KooWNUUFnwjLPJCWwrNcYWW6WDUHo1Vgx18LfPfvd4zywShJ
```

**部署位置**: 
- Docker Compose 配置: `docker/ipfs/docker-compose.yml`
- 数据卷: `ipfs_ipfs_data`

**启动命令**:
```bash
cd docker/ipfs
docker-compose up -d
```

---

### 2. FISCO BCOS 节点 (运行中)
```
容器名称: fisco-node
镜像: fiscoorg/fiscobcos:v2.9.1
状态: Up 43 minutes (healthy)
端口映射:
  - 20200:20200 (Channel - SDK连接)
  - 8545:8545 (JSON-RPC)
  - 30300:30300 (P2P)
```

**验证结果**: ✅ 正常
```bash
curl -X POST --data '{"jsonrpc":"2.0","method":"getBlockNumber","params":[1],"id":1}' \
  -H "Content-Type: application/json" http://localhost:8545
# 返回: {"id":1,"jsonrpc":"2.0","result":"0x0"}
# 当前区块高度: 0 (新节点)
```

**部署位置**:
- Docker Compose 配置: `docker/fisco/docker-compose.yml`
- 节点配置: `docker/fisco/nodes/127.0.0.1/node0/`
- SDK 证书: `docker/fisco/nodes/127.0.0.1/sdk/`

**启动命令**:
```bash
cd docker/fisco
docker-compose up -d
```

---

## 📋 后端配置状态

### 已完成的配置

1. **区块链功能**: ✅ 已启用
   ```yaml
   # application.yml
   blockchain:
     enabled: true
   ```

2. **IPFS 功能**: ✅ 已启用
   ```yaml
   # application.yml
   ipfs:
     enabled: true
     host: localhost
     port: 5001
     gateway-port: 8081
   ```

3. **FISCO BCOS 配置**: ✅ 已配置
   ```yaml
   # application.yml
   fisco:
     nodes:
       - 127.0.0.1:20200
     group-id: 1
   ```

4. **SDK 证书**: ✅ 已复制
   - 位置: `backend/src/main/resources/fisco/`
   - 文件: ca.crt, sdk.crt, sdk.key

5. **SDK 配置文件**: ✅ 已创建
   - 文件: `backend/src/main/resources/fisco-config.yaml`

6. **后端编译**: ✅ 成功
   ```bash
   cd backend && mvn compile -q
   # Exit code: 0
   ```

---

## ⏳ 待完成任务

### 1. 部署智能合约 (必须)

智能合约已编写但尚未部署到区块链。

**合约位置**: `backend/src/main/resources/contracts/CertificateRegistry.sol`

**部署方法**:

#### 方法 1: 使用 Java 部署工具 (推荐)
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=deploy
```

部署成功后会显示合约地址，需要配置到 `application.yml`:
```yaml
fisco:
  contract:
    address: 0x... # 填入部署后的合约地址
```

#### 方法 2: 使用 FISCO BCOS 控制台
```bash
cd docker/fisco

# 下载控制台
curl -#LO https://github.com/FISCO-BCOS/console/releases/download/v2.9.2/download_console.sh
bash download_console.sh

# 配置控制台
cd console
cp ../nodes/127.0.0.1/sdk/* conf/

# 启动控制台
bash start.sh

# 在控制台中部署
[group:1]> deploy CertificateRegistry
```

---

### 2. 启动后端服务

合约部署完成后，启动后端：
```bash
cd backend
mvn spring-boot:run
```

---

### 3. 测试完整流程

1. 启动前端
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

2. 访问 http://localhost:5173

3. 测试流程:
   - 用户注册/登录
   - 提交证书申请
   - 审批流程
   - 证书生成（自动上链到 FISCO BCOS）
   - 证书文件上传到 IPFS
   - 证书验证

---

## 📊 服务访问地址

| 服务 | 地址 | 状态 |
|------|------|------|
| IPFS API | http://localhost:5001 | ✅ 运行中 |
| IPFS Gateway | http://localhost:8081 | ✅ 运行中 |
| FISCO BCOS JSON-RPC | http://localhost:8545 | ✅ 运行中 |
| FISCO BCOS Channel | 127.0.0.1:20200 | ✅ 运行中 |
| 后端 API | http://localhost:8080 | ⏳ 待启动 |
| 前端 | http://localhost:5173 | ⏳ 待启动 |

---

## 🔍 快速检查命令

### 检查 Docker 容器状态
```bash
docker ps --filter "name=ipfs" --filter "name=fisco"
```

### 测试 IPFS
```bash
# 查看节点信息
curl -X POST http://localhost:5001/api/v0/id

# 测试文件上传
echo "Hello IPFS" | curl -X POST -F "file=@-" http://localhost:5001/api/v0/add
```

### 测试 FISCO BCOS
```bash
# 查询区块高度
curl -X POST --data '{"jsonrpc":"2.0","method":"getBlockNumber","params":[1],"id":1}' \
  -H "Content-Type: application/json" http://localhost:8545

# 查看节点信息
curl -X POST --data '{"jsonrpc":"2.0","method":"getNodeVersion","params":[],"id":1}' \
  -H "Content-Type: application/json" http://localhost:8545
```

### 查看日志
```bash
# IPFS 日志
docker logs ipfs-node -f

# FISCO BCOS 日志
docker logs fisco-node -f

# 后端日志（启动后）
tail -f backend/logs/certificate-system.log
```

---

## 📚 相关文档

- **完整部署指南**: [docker/README.md](docker/README.md)
- **项目总结**: [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)
- **数据库初始化**: [初始化数据库.sql](初始化数据库.sql)

---

## 🎯 下一步操作

1. **立即执行**: 部署智能合约
   ```bash
   cd backend
   mvn spring-boot:run -Dspring-boot.run.profiles=deploy
   ```

2. **配置合约地址**: 将部署后的地址填入 `application.yml`

3. **启动服务**: 启动后端和前端进行测试

---

**更新时间**: 2025-12-17 22:24  
**系统状态**: 基础环境已就绪，等待合约部署 🚀