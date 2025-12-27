package com.blockchain.certificate.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统日志实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_system_log")
public class SystemLog {
    
    /**
     * 日志ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 操作类型
     */
    private String operation;
    
    /**
     * 操作模块
     */
    private String module;
    
    /**
     * 操作内容
     */
    private String content;
    
    /**
     * 方法名
     */
    private String method;
    
    /**
     * 请求参数
     */
    private String params;
    
    /**
     * IP地址
     */
    private String ip;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 自定义Builder，支持ipAddress别名
     */
    public static SystemLogBuilder builder() {
        return new SystemLogBuilder();
    }
    
    public static class SystemLogBuilder {
        private String id;
        private String userId;
        private String username;
        private String operation;
        private String module;
        private String content;
        private String method;
        private String params;
        private String ip;
        private LocalDateTime createTime;
        
        SystemLogBuilder() {
        }
        
        public SystemLogBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public SystemLogBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public SystemLogBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public SystemLogBuilder operation(String operation) {
            this.operation = operation;
            return this;
        }
        
        public SystemLogBuilder module(String module) {
            this.module = module;
            return this;
        }
        
        public SystemLogBuilder content(String content) {
            this.content = content;
            return this;
        }
        
        public SystemLogBuilder method(String method) {
            this.method = method;
            return this;
        }
        
        public SystemLogBuilder params(String params) {
            this.params = params;
            return this;
        }
        
        public SystemLogBuilder ip(String ip) {
            this.ip = ip;
            return this;
        }
        
        public SystemLogBuilder ipAddress(String ipAddress) {
            this.ip = ipAddress;
            return this;
        }
        
        public SystemLogBuilder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }
        
        public SystemLog build() {
            SystemLog log = new SystemLog();
            log.id = this.id;
            log.userId = this.userId;
            log.username = this.username;
            log.operation = this.operation;
            log.module = this.module;
            log.content = this.content;
            log.method = this.method;
            log.params = this.params;
            log.ip = this.ip;
            log.createTime = this.createTime;
            return log;
        }
    }
}
