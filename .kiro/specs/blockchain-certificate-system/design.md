# 设计文档

## 概述

区块链证书存证系统是一个企业级的证书管理平台，采用前后端分离架构。系统通过 FISCO BCOS 区块链提供不可篡改的存证服务，通过 IPFS 实现去中心化文件存储，确保证书数据的真实性、完整性和可追溯性。

### 技术栈

**前端**:
- Vue 3 (Composition API)
- TypeScript
- Pinia (状态管理)
- Element Plus (UI 组件库)
- Axios (HTTP 客户端)
- Vue Router (路由管理)

**后端**:
- Spring Boot 2.7+
- Spring Security (安全框架)
- JWT (身份认证)
- MyBatis Plus (ORM)
- Redis (缓存)
- MySQL 8.0 (关系数据库)

**区块链**:
- FISCO BCOS 2.x
- Solidity 0.4.25
- Web3SDK (Java)

**存储**:
- IPFS (去中心化文件存储)
- MySQL (业务数据)
- Redis (缓存和会话)

## 架构设计

### 系统架构

系统采用分层架构，从上到下分为：

1. **表现层 (Presentation Layer)**: Vue 3 前端应用
2. **API 网关层 (API Gateway)**: Spring Boot REST API
3. **业务逻辑层 (Business Logic Layer)**: 服务层实现业务逻辑
4. **数据访问层 (Data Access Layer)**: Repository 模式访问数据
5. **基础设施层 (Infrastructure Layer)**: 区块链、IPFS、数据库

```
┌─────────────────────────────────────────────────────────┐
│                    前端应用 (Vue 3)                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐ │
│  │ 学生端   │  │ 老师端   │  │ 管理员端 │  │ 公共页面│ │
│  └──────────┘  └──────────┘  └──────────┘  └─────────┘ │
└─────────────────────────────────────────────────────────┘
                          │ HTTPS/REST API
┌─────────────────────────────────────────────────────────┐
│              后端应用 (Spring Boot)                      │
│  ┌──────────────────────────────────────────────────┐  │
│  │          API 层 (Controllers)                     │  │
│  └──────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │    业务逻辑层 (Services)                          │  │
│  │  ┌────────┐ ┌────────┐ ┌────────┐ ┌──────────┐  │  │
│  │  │认证服务│ │申请服务│ │审批服务│ │存证服务  │  │  │
│  │  └────────┘ └────────┘ └────────┘ └──────────┘  │  │
│  └──────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │    数据访问层 (Repositories)                      │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
           │              │              │
    ┌──────┴──────┐  ┌───┴────┐  ┌─────┴──────┐
    │   MySQL     │  │  IPFS  │  │FISCO BCOS  │
    │  (业务数据) │  │ (文件) │  │ (存证)     │
    └─────────────┘  └────────┘  └────────────┘
```

### 安全架构

1. **认证机制**: JWT Token 认证
2. **授权机制**: 基于角色的访问控制 (RBAC)
3. **数据加密**: 
   - 传输层: HTTPS/TLS
   - 存储层: 密码 BCrypt 加密，私钥 AES 加密
4. **数字签名**: 审批操作使用私钥签名
5. **防护措施**: 
   - 图形验证码防暴力破解
   - 账户锁定机制
   - 请求频率限制

## 组件和接口

### 前端组件结构

```
src/
├── views/                    # 页面组件
│   ├── auth/
│   │   ├── Login.vue        # 登录页面
│   │   └── Verify.vue       # 证书核验页面
│   ├── common/
│   │   └── Profile.vue      # 个人中心
│   ├── student/
│   │   ├── Application.vue  # 证书申请
│   │   └── MyCertificates.vue # 我的证书
│   ├── teacher/
│   │   ├── Approval.vue     # 证书审批
│   │   └── MyApprovals.vue  # 我的审批
│   └── admin/
│       ├── UserManagement.vue      # 用户管理
│       ├── CollegeConfig.vue       # 学院配置
│       ├── ApprovalFlowConfig.vue  # 审批流配置
│       ├── TemplateConfig.vue      # 模板配置
│       ├── BlockchainConfig.vue    # 区块链配置
│       ├── SystemLog.vue           # 系统日志
│       └── Dashboard.vue           # 可视化大屏
├── components/               # 可复用组件
│   ├── ApplicationForm.vue  # 申请表单
│   ├── ApprovalModal.vue    # 审批模态框
│   ├── CertificateDetail.vue # 证书详情
│   └── FileUpload.vue       # 文件上传
├── stores/                   # Pinia 状态管理
│   ├── auth.ts              # 认证状态
│   ├── application.ts       # 申请状态
│   └── user.ts              # 用户状态
├── api/                      # API 接口
│   ├── auth.ts
│   ├── application.ts
│   ├── approval.ts
│   ├── certificate.ts
│   └── admin.ts
├── router/                   # 路由配置
│   └── index.ts
└── utils/                    # 工具函数
    ├── request.ts           # Axios 封装
    ├── crypto.ts            # 加密工具
    └── validation.ts        # 表单验证
```

### 后端模块结构

```
src/main/java/com/blockchain/certificate/
├── controller/              # 控制器层
│   ├── AuthController.java
│   ├── ApplicationController.java
│   ├── ApprovalController.java
│   ├── CertificateController.java
│   ├── VerificationController.java
│   └── AdminController.java
├── service/                 # 服务层
│   ├── AuthService.java
│   ├── ApplicationService.java
│   ├── ApprovalService.java
│   ├── CertificateService.java
│   ├── BlockchainService.java
│   ├── IpfsService.java
│   └── VerificationService.java
├── repository/              # 数据访问层
│   ├── UserRepository.java
│   ├── ApplicationRepository.java
│   ├── ApprovalRepository.java
│   ├── CertificateRepository.java
│   └── LogRepository.java
├── model/                   # 数据模型
│   ├── entity/             # 实体类
│   │   ├── User.java
│   │   ├── Application.java
│   │   ├── Approval.java
│   │   ├── Certificate.java
│   │   └── SystemLog.java
│   ├── dto/                # 数据传输对象
│   │   ├── LoginRequest.java
│   │   ├── ApplicationRequest.java
│   │   └── ApprovalRequest.java
│   └── vo/                 # 视图对象
│       ├── CertificateVO.java
│       └── VerificationResultVO.java
├── security/                # 安全配置
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── SecurityConfig.java
├── blockchain/              # 区块链集成
│   ├── FiscoBcosClient.java
│   ├── CertificateContract.java
│   └── BlockchainConfig.java
├── ipfs/                    # IPFS 集成
│   ├── IpfsClient.java
│   └── IpfsConfig.java
└── util/                    # 工具类
    ├── CryptoUtil.java
    ├── PdfGenerator.java
    └── SignatureUtil.java
```

### 核心接口定义

#### 认证接口

```java
POST /api/auth/login
Request: {
  "username": "string",
  "password": "string",
  "captcha": "string",
  "captchaKey": "string",
  "rememberMe": boolean
}
Response: {
  "token": "string",
  "refreshToken": "string",
  "userInfo": {
    "id": "string",
    "username": "string",
    "name": "string",
    "role": "string"
  }
}

GET /api/auth/captcha
Response: {
  "key": "string",
  "image": "base64 string"
}

POST /api/auth/refresh
Request: {
  "refreshToken": "string"
}
Response: {
  "token": "string"
}
```

#### 申请接口

```java
POST /api/applications
Request: {
  "title": "string",
  "certificateType": "string",
  "files": ["file1", "file2"]
}
Response: {
  "id": "string",
  "status": "string",
  "createTime": "timestamp"
}

GET /api/applications
Query: {
  "status": "string",
  "page": number,
  "size": number
}
Response: {
  "total": number,
  "items": [Application]
}

DELETE /api/applications/{id}
Response: {
  "success": boolean
}
```

#### 审批接口

```java
POST /api/approvals/{applicationId}
Request: {
  "action": "APPROVE | REJECT",
  "comment": "string",
  "signature": "string"
}
Response: {
  "success": boolean,
  "nextStatus": "string"
}

GET /api/approvals/pending
Query: {
  "page": number,
  "size": number
}
Response: {
  "total": number,
  "items": [Application]
}
```

#### 证书接口

```java
GET /api/certificates
Query: {
  "certificateNo": "string",
  "type": "string",
  "startDate": "date",
  "endDate": "date",
  "page": number,
  "size": number
}
Response: {
  "total": number,
  "items": [Certificate]
}

GET /api/certificates/{id}
Response: {
  "certificateNo": "string",
  "title": "string",
  "type": "string",
  "status": "string",
  "ipfsCid": "string",
  "blockchainTxHash": "string",
  "blockHeight": number,
  "createTime": "timestamp"
}

GET /api/certificates/{id}/download
Response: PDF file stream
```

#### 核验接口

```java
POST /api/verification/verify
Request: {
  "certificateNo": "string"
}
Response: {
  "valid": boolean,
  "certificate": {
    "certificateNo": "string",
    "title": "string",
    "holderName": "string",
    "issueDate": "date"
  },
  "verification": {
    "databaseCheck": boolean,
    "blockchainCheck": boolean,
    "ipfsCheck": boolean,
    "txHash": "string",
    "blockHeight": number
  },
  "downloadUrl": "string"
}
```

## 数据模型

### 数据库设计

#### 用户表 (t_user)

```sql
CREATE TABLE t_user (
  id VARCHAR(32) PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  name VARCHAR(50) NOT NULL,
  role ENUM('STUDENT', 'COLLEGE_TEACHER', 'SCHOOL_TEACHER', 'ADMIN') NOT NULL,
  college_id VARCHAR(32),
  student_no VARCHAR(20),
  employee_no VARCHAR(20),
  email VARCHAR(100),
  phone VARCHAR(20),
  private_key TEXT,  -- AES 加密存储
  status ENUM('ACTIVE', 'LOCKED', 'DISABLED') DEFAULT 'ACTIVE',
  failed_login_count INT DEFAULT 0,
  last_login_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_username (username),
  INDEX idx_role (role),
  INDEX idx_college (college_id)
);
```

#### 申请表 (t_application)

```sql
CREATE TABLE t_application (
  id VARCHAR(32) PRIMARY KEY,
  applicant_id VARCHAR(32) NOT NULL,
  title VARCHAR(200) NOT NULL,
  certificate_type VARCHAR(50) NOT NULL,
  status ENUM('PENDING_COLLEGE', 'PENDING_SCHOOL', 'APPROVED', 'REJECTED', 'CANCELLED') NOT NULL,
  college_id VARCHAR(32) NOT NULL,
  proof_files JSON,  -- [{"name": "file1.pdf", "cid": "Qm..."}]
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_applicant (applicant_id),
  INDEX idx_status (status),
  INDEX idx_college (college_id),
  FOREIGN KEY (applicant_id) REFERENCES t_user(id)
);
```

#### 审批记录表 (t_approval)

```sql
CREATE TABLE t_approval (
  id VARCHAR(32) PRIMARY KEY,
  application_id VARCHAR(32) NOT NULL,
  approver_id VARCHAR(32) NOT NULL,
  approval_level ENUM('COLLEGE', 'SCHOOL') NOT NULL,
  action ENUM('APPROVE', 'REJECT') NOT NULL,
  comment TEXT,
  signature_hash VARCHAR(255),  -- 数字签名哈希
  approval_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_application (application_id),
  INDEX idx_approver (approver_id),
  FOREIGN KEY (application_id) REFERENCES t_application(id),
  FOREIGN KEY (approver_id) REFERENCES t_user(id)
);
```

#### 证书表 (t_certificate)

```sql
CREATE TABLE t_certificate (
  id VARCHAR(32) PRIMARY KEY,
  certificate_no VARCHAR(50) UNIQUE NOT NULL,
  application_id VARCHAR(32) NOT NULL,
  holder_id VARCHAR(32) NOT NULL,
  title VARCHAR(200) NOT NULL,
  certificate_type VARCHAR(50) NOT NULL,
  status ENUM('VALID', 'REVOKED', 'EXPIRED') DEFAULT 'VALID',
  ipfs_cid VARCHAR(100) NOT NULL,
  file_hash VARCHAR(255) NOT NULL,
  blockchain_tx_hash VARCHAR(255),
  block_height BIGINT,
  issue_date DATE NOT NULL,
  expiry_date DATE,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_certificate_no (certificate_no),
  INDEX idx_holder (holder_id),
  INDEX idx_status (status),
  INDEX idx_tx_hash (blockchain_tx_hash),
  FOREIGN KEY (application_id) REFERENCES t_application(id),
  FOREIGN KEY (holder_id) REFERENCES t_user(id)
);
```

#### 学院表 (t_college)

```sql
CREATE TABLE t_college (
  id VARCHAR(32) PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  code VARCHAR(20) UNIQUE NOT NULL,
  approver_id VARCHAR(32),  -- 学院审批老师
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_code (code),
  FOREIGN KEY (approver_id) REFERENCES t_user(id)
);
```

#### 专业表 (t_major)

```sql
CREATE TABLE t_major (
  id VARCHAR(32) PRIMARY KEY,
  college_id VARCHAR(32) NOT NULL,
  name VARCHAR(100) NOT NULL,
  code VARCHAR(20) NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_college (college_id),
  FOREIGN KEY (college_id) REFERENCES t_college(id)
);
```

#### 证书模板表 (t_certificate_template)

```sql
CREATE TABLE t_certificate_template (
  id VARCHAR(32) PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  background_image VARCHAR(255),
  fields JSON,  -- 字段定义
  is_default BOOLEAN DEFAULT FALSE,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 系统日志表 (t_system_log)

```sql
CREATE TABLE t_system_log (
  id VARCHAR(32) PRIMARY KEY,
  user_id VARCHAR(32),
  username VARCHAR(50),
  operation VARCHAR(100) NOT NULL,
  method VARCHAR(200),
  params TEXT,
  ip VARCHAR(50),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (user_id),
  INDEX idx_operation (operation),
  INDEX idx_create_time (create_time)
);
```

#### 系统配置表 (t_system_config)

```sql
CREATE TABLE t_system_config (
  id VARCHAR(32) PRIMARY KEY,
  config_key VARCHAR(100) UNIQUE NOT NULL,
  config_value TEXT,
  description VARCHAR(255),
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 智能合约设计

```solidity
pragma solidity ^0.4.25;

contract CertificateRegistry {
    
    struct CertificateRecord {
        string certificateNo;
        string fileHash;
        address issuer;
        uint256 timestamp;
        bool exists;
    }
    
    mapping(string => CertificateRecord) private certificates;
    
    event CertificateStored(
        string indexed certificateNo,
        string fileHash,
        address indexed issuer,
        uint256 timestamp
    );
    
    function storeCertificate(
        string memory certificateNo,
        string memory fileHash
    ) public returns (bool) {
        require(!certificates[certificateNo].exists, "Certificate already exists");
        
        certificates[certificateNo] = CertificateRecord({
            certificateNo: certificateNo,
            fileHash: fileHash,
            issuer: msg.sender,
            timestamp: block.timestamp,
            exists: true
        });
        
        emit CertificateStored(certificateNo, fileHash, msg.sender, block.timestamp);
        return true;
    }
    
    function verifyCertificate(
        string memory certificateNo,
        string memory fileHash
    ) public view returns (bool, uint256) {
        CertificateRecord memory record = certificates[certificateNo];
        
        if (!record.exists) {
            return (false, 0);
        }
        
        bool isValid = keccak256(abi.encodePacked(record.fileHash)) == 
                       keccak256(abi.encodePacked(fileHash));
        
        return (isValid, record.timestamp);
    }
    
    function getCertificate(
        string memory certificateNo
    ) public view returns (
        string memory,
        string memory,
        address,
        uint256,
        bool
    ) {
        CertificateRecord memory record = certificates[certificateNo];
        return (
            record.certificateNo,
            record.fileHash,
            record.issuer,
            record.timestamp,
            record.exists
        );
    }
}
```


## 正确性属性

*属性是指在系统所有有效执行中都应该成立的特征或行为——本质上是关于系统应该做什么的形式化陈述。属性是人类可读规范和机器可验证正确性保证之间的桥梁。*

### 认证与授权属性

**属性 1: 有效凭据登录成功**
*对于任何*有效的用户凭据（账号、密码、验证码），系统应该验证通过并返回有效的 JWT 令牌
**验证需求: 1.1**

**属性 2: 无效凭据登录失败**
*对于任何*无效的用户凭据，系统应该拒绝登录并返回错误信息
**验证需求: 1.2**

**属性 3: 记住密码功能**
*对于任何*选择"记住密码"的登录操作，本地存储应该包含加密的登录凭据
**验证需求: 1.3**

**属性 4: 基于角色的访问控制**
*对于任何*用户和功能访问请求，系统应该根据用户角色正确允许或拒绝访问
**验证需求: 1.4**

**属性 5: 令牌过期处理**
*对于任何*过期的 JWT 令牌，系统应该要求刷新令牌或重新登录
**验证需求: 1.5**

**属性 6: 验证码刷新**
*对于任何*验证码刷新请求，系统应该生成新的验证码并返回不同的图片
**验证需求: 1.7**

### 申请流程属性

**属性 7: 申请创建和文件存储**
*对于任何*有效的申请数据和 1-3 个证明文件，系统应该创建申请记录并将所有文件上传到 IPFS 获取 CID
**验证需求: 2.1**

**属性 8: 文件格式验证**
*对于任何*非 PDF、JPG、PNG 格式的文件，系统应该拒绝上传
**验证需求: 2.2**

**属性 9: 自动填充学生信息**
*对于任何*学生提交的申请，申请记录应该包含该学生的完整个人信息
**验证需求: 2.3**

**属性 10: 申请列表完整性**
*对于任何*学生，查询申请列表应该返回该学生的所有申请记录
**验证需求: 2.4**

**属性 11: 取消申请数据清理**
*对于任何*未审批的申请，取消后数据库和 IPFS 都不应该包含该申请的数据
**验证需求: 2.5**

**属性 12: 申请搜索准确性**
*对于任何*搜索条件（证书类型或状态），搜索结果应该只包含符合条件的申请
**验证需求: 2.6**

### 审批流程属性

**属性 13: 审批数字签名**
*对于任何*审批操作，审批记录应该包含使用审批人私钥生成的有效数字签名
**验证需求: 3.1**

**属性 14: 初审通过状态转换**
*对于任何*学院老师通过的初审，申请状态应该更新为待学校审批
**验证需求: 3.2**

**属性 15: 驳回状态转换**
*对于任何*被驳回的申请，申请状态应该更新为已驳回
**验证需求: 3.3**

**属性 16: 终审触发存证**
*对于任何*学校老师通过的终审，系统应该生成证书并完成区块链存证
**验证需求: 3.4**

**属性 17: 待审列表权限过滤**
*对于任何*审批老师，待审列表应该只包含分配给该老师学院的申请
**验证需求: 3.5**

**属性 18: 审批意见记录**
*对于任何*包含审批意见的审批操作，审批记录应该包含该意见
**验证需求: 3.6**

**属性 19: 申请详情完整性**
*对于任何*申请详情查询，返回结果应该包含学生信息、申请内容、证明材料和审批历史
**验证需求: 3.7**

### 证书生成属性

**属性 20: 证书编号唯一性**
*对于任何*生成的证书，证书编号应该是全局唯一的
**验证需求: 4.1**

**属性 21: 证书 PDF 生成**
*对于任何*证书，系统应该使用模板生成有效的 PDF 文件
**验证需求: 4.2**

**属性 22: 证书 IPFS 存储往返**
*对于任何*生成的证书 PDF，上传到 IPFS 后应该能通过 CID 获取相同的文件
**验证需求: 4.3**

**属性 23: 证书元数据存储**
*对于任何*生成的证书，数据库应该包含证书编号、CID 和哈希值
**验证需求: 4.4**

**属性 24: 证书撤销状态更新**
*对于任何*被撤销的证书，证书状态应该更新为已撤销
**验证需求: 4.5**

**属性 25: 撤销证书状态查询**
*对于任何*已撤销的证书，查询结果应该显示已撤销状态
**验证需求: 4.6**


### 区块链存证属性

**属性 26: 智能合约调用**
*对于任何*生成的证书，系统应该调用智能合约将证书哈希值上链
**验证需求: 5.1**

**属性 27: 交易回执获取**
*对于任何*成功的智能合约调用，系统应该获取交易哈希和区块高度
**验证需求: 5.2**

**属性 28: 交易信息记录**
*对于任何*获取的交易回执，数据库应该包含交易哈希、区块高度和时间戳
**验证需求: 5.3**

**属性 29: 区块链错误重试**
*对于任何*区块链节点不可用的情况，系统应该记录错误日志并重试
**验证需求: 5.4**

### 证书核验属性

**属性 30: 数据库查询**
*对于任何*证书编号，系统应该能在数据库中查询到对应的证书记录（如果存在）
**验证需求: 6.1**

**属性 31: 区块链验证**
*对于任何*数据库中存在的证书，区块链上应该有对应的哈希值记录
**验证需求: 6.2**

**属性 32: IPFS 完整性验证**
*对于任何*通过区块链验证的证书，从 IPFS 获取的文件哈希应该与记录的哈希一致
**验证需求: 6.3**

**属性 33: 三级验证成功**
*对于任何*通过三级验证的证书，系统应该返回验证成功结果和完整的证书详情
**验证需求: 6.4**

**属性 34: 验证失败处理**
*对于任何*验证失败的情况，系统应该返回具体的失败原因
**验证需求: 6.5**

**属性 35: 下载链接提供**
*对于任何*验证成功的证书，系统应该提供有效的 PDF 下载链接
**验证需求: 6.6**

**属性 36: 核验页面公开访问**
*对于任何*未登录用户，系统应该允许访问核验页面
**验证需求: 6.7**

### 文件存储属性

**属性 37: IPFS 上传往返**
*对于任何*上传到 IPFS 的文件，应该能通过返回的 CID 获取相同的文件内容
**验证需求: 7.1**

**属性 38: IPFS 文件检索**
*对于任何*有效的 CID，系统应该能从 IPFS 检索到对应的文件
**验证需求: 7.2**

**属性 39: 证书下载权限验证**
*对于任何*证书下载请求，系统应该验证用户权限后才提供文件
**验证需求: 7.3**

**属性 40: IPFS 错误处理**
*对于任何*IPFS 节点不可用的情况，系统应该记录错误并提示用户
**验证需求: 7.4**

**属性 41: 申请删除文件清理**
*对于任何*删除的申请，IPFS 上的关联文件也应该被删除
**验证需求: 7.5**

### 用户管理属性

**属性 42: 用户创建验证**
*对于任何*有效的用户信息，系统应该创建用户记录并分配指定角色
**验证需求: 8.1**

**属性 43: 用户信息更新日志**
*对于任何*用户信息更新操作，系统应该更新数据并记录操作日志
**验证需求: 8.2**

**属性 44: 密码重置**
*对于任何*密码重置操作，系统应该生成临时密码
**验证需求: 8.3**

**属性 45: 账号禁用登录阻止**
*对于任何*被禁用的账号，系统应该拒绝该用户的登录请求
**验证需求: 8.4**

**属性 46: 用户软删除**
*对于任何*删除的用户，系统应该软删除记录并保留历史数据
**验证需求: 8.5**

**属性 47: 用户搜索准确性**
*对于任何*搜索条件（用户名、姓名、角色），搜索结果应该只包含符合条件的用户
**验证需求: 8.6**

**属性 48: 用户列表分页**
*对于任何*用户列表查询，系统应该支持分页并返回正确的页数据
**验证需求: 8.7**

### 配置管理属性

**属性 49: 学院创建**
*对于任何*有效的学院信息，系统应该创建学院记录并支持添加专业
**验证需求: 9.1**

**属性 50: 审批人分配**
*对于任何*学院和审批老师，系统应该正确关联审批人到学院
**验证需求: 9.2**

**属性 51: 学院信息更新**
*对于任何*学院信息更新操作，系统应该更新学院数据
**验证需求: 9.3**

**属性 52: 学院删除检查**
*对于任何*学院删除操作，系统应该检查关联数据并提示确认
**验证需求: 9.4**

**属性 53: 审批流配置**
*对于任何*审批流配置操作，系统应该允许启用或禁用学校审批环节
**验证需求: 9.5**

**属性 54: 审批流配置应用**
*对于任何*保存的审批流配置，系统应该将新配置应用到后续申请
**验证需求: 9.6**

**属性 55: 模板上传验证**
*对于任何*有效格式的模板背景图，系统应该存储模板
**验证需求: 10.1**

**属性 56: 模板字段配置**
*对于任何*模板字段添加操作，系统应该保存字段位置和样式
**验证需求: 10.2**

**属性 57: 默认模板设置**
*对于任何*设置为默认的模板，系统应该在证书生成时使用该模板
**验证需求: 10.3**

**属性 58: 模板预览**
*对于任何*模板，系统应该能使用示例数据渲染预览效果
**验证需求: 10.4**

**属性 59: 模板删除检查**
*对于任何*模板删除操作，系统应该检查是否为默认模板
**验证需求: 10.5**


### 日志与统计属性

**属性 60: 操作日志记录**
*对于任何*关键操作，系统应该记录操作时间、操作人、操作类型和操作内容
**验证需求: 11.1**

**属性 61: 日志列表分页**
*对于任何*日志列表查询，系统应该支持分页并返回正确的页数据
**验证需求: 11.2**

**属性 62: 日志时间筛选**
*对于任何*时间范围筛选，系统应该只返回该时间范围内的日志
**验证需求: 11.3**

**属性 63: 日志用户筛选**
*对于任何*操作人筛选，系统应该只返回该用户的操作记录
**验证需求: 11.4**

**属性 64: 日志详情完整性**
*对于任何*日志详情查询，系统应该返回完整的操作信息
**验证需求: 11.5**

**属性 65: 日志导出**
*对于任何*日志导出请求，系统应该生成有效的 Excel 文件
**验证需求: 11.6**

**属性 66: 证书统计准确性**
*对于任何*时间段，系统应该准确统计证书颁发数量
**验证需求: 12.1**

**属性 67: 审批统计准确性**
*对于任何*时间段，系统应该准确统计待审批、已通过、已驳回的数量
**验证需求: 12.2**

**属性 68: 区块链监控数据**
*对于任何*区块链监控查询，系统应该返回节点状态、交易统计和区块同步进度
**验证需求: 12.3**

**属性 69: 报表导出**
*对于任何*报表导出请求，系统应该支持 Excel 或 PDF 格式
**验证需求: 12.4**

### 个人中心属性

**属性 70: 表单实时验证**
*对于任何*表单输入，系统应该提供实时验证反馈
**验证需求: 13.5**

**属性 71: 个人信息展示**
*对于任何*用户访问个人中心，系统应该显示该用户的基本信息
**验证需求: 14.1**

**属性 72: 密码修改验证**
*对于任何*密码修改操作，系统应该验证原密码正确后才更新新密码
**验证需求: 14.2**

**属性 73: 个人信息更新**
*对于任何*个人信息更新操作，系统应该保存修改
**验证需求: 14.3**

### 证书查询属性

**属性 74: 我的证书列表**
*对于任何*学生，我的证书页面应该显示该学生的所有有效证书
**验证需求: 15.1**

**属性 75: 证书搜索准确性**
*对于任何*搜索条件（证书编号或类型），搜索结果应该只包含符合条件的证书
**验证需求: 15.2**

**属性 76: 证书时间筛选**
*对于任何*时间范围筛选，系统应该只返回该时间范围内的证书
**验证需求: 15.3**

**属性 77: 证书详情完整性**
*对于任何*证书详情查询，系统应该返回完整的证书信息、区块链存证信息和 IPFS 信息
**验证需求: 15.4**

**属性 78: 证书下载**
*对于任何*证书下载请求，系统应该从 IPFS 获取 PDF 并提供下载
**验证需求: 15.5**

### 审批统计属性

**属性 79: 审批统计展示**
*对于任何*审批老师，审批页面应该显示待我审批、我已通过、我已驳回和总审批量的统计
**验证需求: 16.1**

**属性 80: 我的审批列表**
*对于任何*审批老师，我的审批页面应该显示该老师的所有审批记录
**验证需求: 16.2**

**属性 81: 审批记录搜索**
*对于任何*搜索条件（申请人、证书类型、时间），搜索结果应该只包含符合条件的审批记录
**验证需求: 16.3**

### 区块链配置属性

**属性 82: 区块链状态展示**
*对于任何*区块链配置页面访问，系统应该显示当前区块链网络状态
**验证需求: 17.1**

**属性 83: 配置信息展示**
*对于任何*配置信息查询，系统应该显示合约地址和节点连接信息
**验证需求: 17.2**

**属性 84: 数据同步**
*对于任何*数据同步请求，系统应该从区块链同步最新的存证记录
**验证需求: 17.3**

**属性 85: 区块链错误展示**
*对于任何*区块链节点连接失败的情况，系统应该显示错误状态和错误信息
**验证需求: 17.4**

## 错误处理

### 错误分类

1. **客户端错误 (4xx)**
   - 400 Bad Request: 请求参数错误
   - 401 Unauthorized: 未认证或令牌无效
   - 403 Forbidden: 无权限访问
   - 404 Not Found: 资源不存在
   - 409 Conflict: 资源冲突（如证书编号重复）

2. **服务器错误 (5xx)**
   - 500 Internal Server Error: 服务器内部错误
   - 502 Bad Gateway: 区块链或 IPFS 服务不可用
   - 503 Service Unavailable: 服务暂时不可用

### 错误响应格式

```json
{
  "success": false,
  "code": "ERROR_CODE",
  "message": "用户友好的错误信息",
  "details": "详细的错误描述（开发环境）",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### 关键错误处理策略

1. **区块链交互错误**
   - 节点不可用: 重试 3 次，间隔 5 秒
   - 交易失败: 记录日志，通知管理员
   - Gas 不足: 自动调整 Gas 限制

2. **IPFS 交互错误**
   - 上传失败: 重试 3 次
   - 文件不存在: 返回 404 错误
   - 节点不可用: 使用备用节点

3. **数据库错误**
   - 连接失败: 使用连接池重试
   - 唯一约束冲突: 返回 409 错误
   - 事务失败: 回滚并记录日志

4. **文件处理错误**
   - 文件过大: 限制 10MB
   - 格式不支持: 返回 400 错误
   - PDF 生成失败: 记录日志并通知管理员


## 测试策略

### 双重测试方法

系统采用单元测试和基于属性的测试相结合的方法，以确保全面的代码覆盖和正确性验证：

- **单元测试**: 验证特定示例、边界情况和错误条件
- **基于属性的测试**: 验证应该在所有输入中成立的通用属性
- 两者互补：单元测试捕获具体的错误，基于属性的测试验证一般正确性

### 单元测试

单元测试覆盖：
- 特定示例，展示正确行为
- 组件之间的集成点
- 边界条件（空输入、最大值、最小值）
- 错误条件和异常处理

**前端单元测试框架**: Vitest + Vue Test Utils

示例测试：
```typescript
describe('Login Component', () => {
  it('should display error when credentials are invalid', async () => {
    const wrapper = mount(Login)
    await wrapper.find('input[name="username"]').setValue('invalid')
    await wrapper.find('input[name="password"]').setValue('wrong')
    await wrapper.find('button[type="submit"]').trigger('click')
    
    expect(wrapper.find('.error-message').text()).toBe('用户名或密码错误')
  })
  
  it('should lock account after 5 failed attempts', async () => {
    // 测试账户锁定边界条件
  })
})
```

**后端单元测试框架**: JUnit 5 + Mockito

示例测试：
```java
@Test
void shouldRejectInvalidFileFormat() {
    MockMultipartFile file = new MockMultipartFile(
        "file", "test.txt", "text/plain", "content".getBytes()
    );
    
    assertThrows(InvalidFileFormatException.class, () -> {
        applicationService.createApplication(request, List.of(file));
    });
}

@Test
void shouldGenerateUniqueCertificateNumber() {
    String cert1 = certificateService.generateCertificateNumber();
    String cert2 = certificateService.generateCertificateNumber();
    
    assertNotEquals(cert1, cert2);
}
```

### 基于属性的测试

**测试库选择**:
- 前端: fast-check (TypeScript)
- 后端: jqwik (Java)

**配置要求**:
- 每个基于属性的测试必须运行至少 100 次迭代
- 每个测试必须使用注释明确引用设计文档中的正确性属性
- 注释格式: `// Feature: blockchain-certificate-system, Property X: [property text]`

**示例基于属性的测试**:

前端示例：
```typescript
import fc from 'fast-check'

describe('Property-Based Tests', () => {
  // Feature: blockchain-certificate-system, Property 1: 有效凭据登录成功
  it('should issue valid JWT token for any valid credentials', () => {
    fc.assert(
      fc.asyncProperty(
        fc.record({
          username: fc.string({ minLength: 3, maxLength: 50 }),
          password: fc.string({ minLength: 6, maxLength: 20 }),
          captcha: fc.string({ minLength: 4, maxLength: 4 })
        }),
        async (credentials) => {
          // 创建有效用户
          await createUser(credentials)
          
          // 登录
          const response = await login(credentials)
          
          // 验证 JWT 令牌有效
          expect(response.token).toBeDefined()
          expect(isValidJWT(response.token)).toBe(true)
        }
      ),
      { numRuns: 100 }
    )
  })
  
  // Feature: blockchain-certificate-system, Property 8: 文件格式验证
  it('should reject any non-PDF/JPG/PNG file format', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('txt', 'doc', 'exe', 'zip', 'mp4'),
        fc.uint8Array({ minLength: 100, maxLength: 1000 }),
        (extension, content) => {
          const file = new File([content], `test.${extension}`)
          
          expect(() => validateFileFormat(file)).toThrow()
        }
      ),
      { numRuns: 100 }
    )
  })
})
```

后端示例：
```java
@Property
// Feature: blockchain-certificate-system, Property 20: 证书编号唯一性
void certificateNumbersShouldBeUnique(@ForAll("certificates") List<Certificate> certificates) {
    Set<String> certificateNumbers = certificates.stream()
        .map(Certificate::getCertificateNo)
        .collect(Collectors.toSet());
    
    assertEquals(certificates.size(), certificateNumbers.size());
}

@Property
// Feature: blockchain-certificate-system, Property 22: 证书 IPFS 存储往返
void ipfsUploadDownloadShouldBeRoundTrip(
    @ForAll @Size(min = 1000, max = 10000) byte[] pdfContent
) {
    // 上传到 IPFS
    String cid = ipfsService.upload(pdfContent);
    
    // 从 IPFS 下载
    byte[] downloaded = ipfsService.download(cid);
    
    // 验证内容一致
    assertArrayEquals(pdfContent, downloaded);
}

@Property
// Feature: blockchain-certificate-system, Property 26: 智能合约调用
void blockchainStorageShouldSucceedForAnyCertificate(
    @ForAll("validCertificate") Certificate certificate
) {
    // 调用智能合约
    TransactionReceipt receipt = blockchainService.storeCertificate(
        certificate.getCertificateNo(),
        certificate.getFileHash()
    );
    
    // 验证交易成功
    assertTrue(receipt.isStatusOK());
    assertNotNull(receipt.getTransactionHash());
    assertTrue(receipt.getBlockNumber().longValue() > 0);
}

@Provide
Arbitrary<Certificate> validCertificate() {
    return Combinators.combine(
        Arbitraries.strings().alpha().ofLength(20),
        Arbitraries.strings().hexadecimal().ofLength(64),
        Arbitraries.strings().alpha().ofMinLength(10).ofMaxLength(200)
    ).as((no, hash, title) -> 
        Certificate.builder()
            .certificateNo(no)
            .fileHash(hash)
            .title(title)
            .build()
    );
}
```

### 集成测试

集成测试覆盖：
- API 端到端流程
- 数据库事务
- 区块链交互
- IPFS 文件操作

**测试环境**:
- 使用 Testcontainers 启动 MySQL、Redis
- 使用 FISCO BCOS 测试网络
- 使用本地 IPFS 节点

### 测试覆盖率目标

- 单元测试代码覆盖率: ≥ 80%
- 关键业务逻辑覆盖率: ≥ 90%
- 所有正确性属性必须有对应的基于属性的测试

### 测试数据生成策略

1. **智能生成器**: 为基于属性的测试编写智能生成器，约束到有效的输入空间
2. **边界值**: 包含最小值、最大值、空值等边界情况
3. **随机数据**: 使用随机数据测试系统的鲁棒性
4. **真实数据**: 使用脱敏的真实数据进行测试

## 性能考虑

### 性能目标

- API 响应时间: < 200ms (P95)
- 证书生成时间: < 5s
- 区块链存证时间: < 10s
- 文件上传时间: < 3s (1MB 文件)
- 并发用户数: 1000+

### 优化策略

1. **数据库优化**
   - 为常用查询字段添加索引
   - 使用连接池管理数据库连接
   - 实施查询缓存

2. **缓存策略**
   - Redis 缓存用户会话
   - 缓存证书模板
   - 缓存学院和专业信息
   - 缓存区块链配置

3. **异步处理**
   - 证书生成异步处理
   - 区块链存证异步处理
   - 文件上传异步处理
   - 使用消息队列解耦

4. **前端优化**
   - 组件懒加载
   - 图片懒加载
   - 虚拟滚动处理长列表
   - 使用 CDN 加速静态资源

5. **区块链优化**
   - 批量存证减少交易次数
   - 使用事件监听而非轮询
   - 优化 Gas 使用

## 安全考虑

### 安全措施

1. **认证安全**
   - JWT 令牌有效期: 2 小时
   - 刷新令牌有效期: 7 天
   - 密码 BCrypt 加密，强度 10
   - 私钥 AES-256 加密存储

2. **授权安全**
   - 基于角色的访问控制
   - 方法级权限注解
   - 数据级权限过滤

3. **传输安全**
   - 强制 HTTPS
   - TLS 1.2+
   - 证书验证

4. **输入验证**
   - 前端表单验证
   - 后端参数验证
   - SQL 注入防护
   - XSS 防护

5. **审计日志**
   - 记录所有关键操作
   - 记录登录失败
   - 记录权限拒绝
   - 日志不可篡改

6. **防护措施**
   - 图形验证码
   - 账户锁定机制
   - 请求频率限制
   - CSRF 防护

## 部署架构

### 部署环境

- **前端**: Nginx 静态文件服务
- **后端**: Spring Boot 应用，使用 Tomcat 容器
- **数据库**: MySQL 8.0 主从复制
- **缓存**: Redis 哨兵模式
- **区块链**: FISCO BCOS 多节点部署
- **存储**: IPFS 集群

### 容器化部署

使用 Docker Compose 编排服务：

```yaml
version: '3.8'
services:
  frontend:
    image: nginx:alpine
    volumes:
      - ./dist:/usr/share/nginx/html
    ports:
      - "80:80"
      - "443:443"
  
  backend:
    image: certificate-backend:latest
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
  
  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=${DB_PASSWORD}
    volumes:
      - mysql-data:/var/lib/mysql
  
  redis:
    image: redis:alpine
    volumes:
      - redis-data:/data
  
  ipfs:
    image: ipfs/go-ipfs:latest
    ports:
      - "5001:5001"
    volumes:
      - ipfs-data:/data/ipfs

volumes:
  mysql-data:
  redis-data:
  ipfs-data:
```

### 监控和日志

- **应用监控**: Spring Boot Actuator + Prometheus
- **日志收集**: ELK Stack (Elasticsearch + Logstash + Kibana)
- **链路追踪**: Zipkin
- **告警**: Grafana + AlertManager

## 技术债务和未来改进

1. **短期改进**
   - 实现证书批量导入功能
   - 添加证书模板在线编辑器
   - 优化大文件上传体验

2. **中期改进**
   - 实现微服务架构拆分
   - 添加消息队列支持
   - 实现分布式事务

3. **长期改进**
   - 支持多链存证
   - 实现跨链验证
   - 添加 AI 辅助审批
