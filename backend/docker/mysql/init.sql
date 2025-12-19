-- 区块链证书存证系统 - 数据库初始化脚本
-- 创建生产数据库
CREATE DATABASE IF NOT EXISTS certificate_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE certificate_system;

-- 创建用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    role ENUM('STUDENT', 'COLLEGE_ADMIN', 'SCHOOL_ADMIN', 'SYSTEM_ADMIN') NOT NULL COMMENT '角色',
    college_id BIGINT COMMENT '学院ID',
    major_id BIGINT COMMENT '专业ID',
    student_no VARCHAR(20) COMMENT '学号',
    employee_no VARCHAR(20) COMMENT '工号',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    private_key TEXT COMMENT '私钥',
    status ENUM('ACTIVE', 'LOCKED', 'DISABLED') DEFAULT 'ACTIVE' COMMENT '状态',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    failed_login_count INT DEFAULT 0 COMMENT '失败登录次数',
    last_login_time DATETIME COMMENT '最后登录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_college (college_id),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建学院表
CREATE TABLE IF NOT EXISTS t_college (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '学院名称',
    code VARCHAR(20) UNIQUE NOT NULL COMMENT '学院代码',
    approver_id BIGINT COMMENT '审批人ID',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_code (code),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院表';

-- 创建专业表
CREATE TABLE IF NOT EXISTS t_major (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    college_id BIGINT NOT NULL COMMENT '学院ID',
    name VARCHAR(100) NOT NULL COMMENT '专业名称',
    code VARCHAR(20) NOT NULL COMMENT '专业代码',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_college (college_id),
    INDEX idx_deleted (deleted),
    UNIQUE KEY uk_college_code (college_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业表';

-- 创建申请表
CREATE TABLE IF NOT EXISTS t_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    title VARCHAR(200) NOT NULL COMMENT '申请标题',
    certificate_type VARCHAR(50) NOT NULL COMMENT '证书类型',
    status ENUM('PENDING_COLLEGE', 'PENDING_SCHOOL', 'APPROVED', 'REJECTED', 'CANCELLED') NOT NULL COMMENT '状态',
    college_id BIGINT NOT NULL COMMENT '学院ID',
    major_id BIGINT COMMENT '专业ID',
    proof_files JSON COMMENT '证明文件',
    description TEXT COMMENT '申请说明',
    reject_reason TEXT COMMENT '拒绝原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_applicant (applicant_id),
    INDEX idx_status (status),
    INDEX idx_college (college_id),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申请表';

-- 创建审批记录表
CREATE TABLE IF NOT EXISTS t_approval (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_id BIGINT NOT NULL COMMENT '申请ID',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    approval_level ENUM('COLLEGE', 'SCHOOL') NOT NULL COMMENT '审批级别',
    action ENUM('APPROVE', 'REJECT') NOT NULL COMMENT '审批动作',
    comment TEXT COMMENT '审批意见',
    signature_hash VARCHAR(255) COMMENT '签名哈希',
    approval_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_application (application_id),
    INDEX idx_approver (approver_id),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- 创建证书表
CREATE TABLE IF NOT EXISTS t_certificate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    certificate_no VARCHAR(50) UNIQUE NOT NULL COMMENT '证书编号',
    application_id BIGINT NOT NULL COMMENT '申请ID',
    holder_id BIGINT NOT NULL COMMENT '持有人ID',
    title VARCHAR(200) NOT NULL COMMENT '证书标题',
    certificate_type VARCHAR(50) NOT NULL COMMENT '证书类型',
    status ENUM('VALID', 'REVOKED', 'EXPIRED') DEFAULT 'VALID' COMMENT '状态',
    ipfs_cid VARCHAR(100) NOT NULL COMMENT 'IPFS CID',
    file_hash VARCHAR(255) NOT NULL COMMENT '文件哈希',
    blockchain_tx_hash VARCHAR(255) COMMENT '区块链交易哈希',
    block_height BIGINT COMMENT '区块高度',
    issue_date DATE NOT NULL COMMENT '颁发日期',
    expiry_date DATE COMMENT '过期日期',
    revoke_reason TEXT COMMENT '撤销原因',
    revoke_time DATETIME COMMENT '撤销时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_certificate_no (certificate_no),
    INDEX idx_holder (holder_id),
    INDEX idx_status (status),
    INDEX idx_tx_hash (blockchain_tx_hash),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证书表';

-- 创建证书模板表
CREATE TABLE IF NOT EXISTS t_certificate_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    certificate_type VARCHAR(50) NOT NULL COMMENT '证书类型',
    background_image VARCHAR(255) COMMENT '背景图片',
    fields JSON COMMENT '字段配置',
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否默认模板',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_type (certificate_type),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证书模板表';

-- 创建系统日志表
CREATE TABLE IF NOT EXISTS t_system_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation VARCHAR(100) NOT NULL COMMENT '操作',
    method VARCHAR(200) COMMENT '方法',
    params TEXT COMMENT '参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    status VARCHAR(20) COMMENT '状态',
    error_msg TEXT COMMENT '错误信息',
    execution_time BIGINT COMMENT '执行时间(ms)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user (user_id),
    INDEX idx_operation (operation),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';

-- 创建系统配置表
CREATE TABLE IF NOT EXISTS t_system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) UNIQUE NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(255) COMMENT '描述',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_key (config_key),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入初始数据

-- 插入学院数据
INSERT INTO t_college (id, name, code, enabled, deleted) VALUES 
(1, '计算机学院', 'CS', TRUE, FALSE),
(2, '软件学院', 'SE', TRUE, FALSE),
(3, '信息学院', 'IS', TRUE, FALSE);

-- 插入专业数据
INSERT INTO t_major (id, college_id, name, code, enabled, deleted) VALUES 
(1, 1, '计算机科学与技术', 'CS01', TRUE, FALSE),
(2, 1, '人工智能', 'CS02', TRUE, FALSE),
(3, 2, '软件工程', 'SE01', TRUE, FALSE),
(4, 2, '数据科学与大数据技术', 'SE02', TRUE, FALSE),
(5, 3, '信息安全', 'IS01', TRUE, FALSE);

-- 插入管理员用户
-- 密码: admin123 (BCrypt加密后的值)
INSERT INTO t_user (id, username, password, name, role, email, phone, enabled, status, deleted) VALUES 
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '系统管理员', 'SYSTEM_ADMIN', 'admin@example.com', '13800138000', TRUE, 'ACTIVE', FALSE);

-- 插入学院管理员
-- 密码: admin123
INSERT INTO t_user (id, username, password, name, role, college_id, employee_no, email, enabled, status, deleted) VALUES 
(2, 'cs_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '计算机学院管理员', 'COLLEGE_ADMIN', 1, 'T001', 'cs_admin@example.com', TRUE, 'ACTIVE', FALSE),
(3, 'se_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '软件学院管理员', 'COLLEGE_ADMIN', 2, 'T002', 'se_admin@example.com', TRUE, 'ACTIVE', FALSE);

-- 插入学校管理员
-- 密码: admin123
INSERT INTO t_user (id, username, password, name, role, employee_no, email, enabled, status, deleted) VALUES 
(4, 'school_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '学校管理员', 'SCHOOL_ADMIN', 'T003', 'school_admin@example.com', TRUE, 'ACTIVE', FALSE);

-- 插入测试学生
-- 密码: student123
INSERT INTO t_user (id, username, password, name, role, college_id, major_id, student_no, email, enabled, status, deleted) VALUES 
(5, 'student1', '$2a$10$rZ7Qw8YLhc8.vF5K5FqXXuMKp7VvJLxvXxvXxvXxvXxvXxvXxvXxv', '张三', 'STUDENT', 1, 1, '2021001', 'student1@example.com', TRUE, 'ACTIVE', FALSE),
(6, 'student2', '$2a$10$rZ7Qw8YLhc8.vF5K5FqXXuMKp7VvJLxvXxvXxvXxvXxvXxvXxvXxv', '李四', 'STUDENT', 2, 3, '2021002', 'student2@example.com', TRUE, 'ACTIVE', FALSE);

-- 插入系统配置
INSERT INTO t_system_config (config_key, config_value, description, deleted) VALUES 
('system.name', '区块链证书存证系统', '系统名称', FALSE),
('system.version', '1.0.0', '系统版本', FALSE),
('certificate.validity_period', '365', '证书有效期(天)', FALSE),
('blockchain.enabled', 'true', '是否启用区块链', FALSE),
('ipfs.enabled', 'false', '是否启用IPFS', FALSE);

-- 创建默认证书模板
INSERT INTO t_certificate_template (name, certificate_type, is_default, enabled, deleted) VALUES 
('默认荣誉证书模板', 'HONOR', TRUE, TRUE, FALSE),
('默认奖学金证书模板', 'SCHOLARSHIP', TRUE, TRUE, FALSE),
('默认竞赛证书模板', 'COMPETITION', TRUE, TRUE, FALSE);

COMMIT;

-- 显示初始化结果
SELECT '数据库初始化完成！' AS message;
SELECT '默认管理员账号: admin, 密码: admin123' AS admin_info;
SELECT '学院管理员账号: cs_admin/se_admin, 密码: admin123' AS college_admin_info;
SELECT '学校管理员账号: school_admin, 密码: admin123' AS school_admin_info;