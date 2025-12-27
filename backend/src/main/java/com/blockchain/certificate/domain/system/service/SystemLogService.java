package com.blockchain.certificate.domain.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blockchain.certificate.shared.common.PageResult;
import com.blockchain.certificate.domain.system.model.SystemLog;
import com.blockchain.certificate.domain.system.repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 系统日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemLogService {
    
    private final SystemLogRepository systemLogRepository;
    
    /**
     * 记录系统日志
     */
    @Transactional(rollbackFor = Exception.class)
    public void log(String userId, String username, String operation, 
                    String module, String content, String ipAddress) {
        SystemLog systemLog = SystemLog.builder()
                .userId(userId)
                .username(username)
                .operation(operation)
                .module(module)
                .content(content)
                .ipAddress(ipAddress)
                .createTime(LocalDateTime.now())
                .build();
        
        systemLogRepository.insert(systemLog);
    }
    
    /**
     * 记录登录日志
     */
    public void logLogin(String userId, String username, String ipAddress, boolean success) {
        String operation = success ? "登录成功" : "登录失败";
        String content = String.format("用户 %s 从 IP %s %s", username, ipAddress, operation);
        log(userId, username, operation, "认证", content, ipAddress);
    }
    
    /**
     * 记录登出日志
     */
    public void logLogout(String userId, String username, String ipAddress) {
        String content = String.format("用户 %s 从 IP %s 登出", username, ipAddress);
        log(userId, username, "登出", "认证", content, ipAddress);
    }
    
    /**
     * 记录申请操作日志
     */
    public void logApplication(String userId, String username, String operation, 
                                String applicationId, String ipAddress) {
        String content = String.format("用户 %s 对申请 %s 执行了 %s 操作", 
                username, applicationId, operation);
        log(userId, username, operation, "申请管理", content, ipAddress);
    }
    
    /**
     * 记录审批操作日志
     */
    public void logApproval(String userId, String username, String operation, 
                            String applicationId, String result, String ipAddress) {
        String content = String.format("用户 %s 对申请 %s 执行了 %s 操作，结果：%s", 
                username, applicationId, operation, result);
        log(userId, username, operation, "审批管理", content, ipAddress);
    }
    
    /**
     * 记录证书操作日志
     */
    public void logCertificate(String userId, String username, String operation, 
                               String certificateNumber, String ipAddress) {
        String content = String.format("用户 %s 对证书 %s 执行了 %s 操作", 
                username, certificateNumber, operation);
        log(userId, username, operation, "证书管理", content, ipAddress);
    }
    
    /**
     * 记录用户管理操作日志
     */
    public void logUserManagement(String userId, String username, String operation, 
                                  String targetUserId, String ipAddress) {
        String content = String.format("用户 %s 对用户 %s 执行了 %s 操作", 
                username, targetUserId, operation);
        log(userId, username, operation, "用户管理", content, ipAddress);
    }
    
    /**
     * 记录配置管理操作日志
     */
    public void logConfigManagement(String userId, String username, String operation, 
                                    String module, String target, String ipAddress) {
        String content = String.format("用户 %s 对 %s 执行了 %s 操作", 
                username, target, operation);
        log(userId, username, operation, module, content, ipAddress);
    }
    
    /**
     * 分页查询系统日志
     */
    public PageResult<SystemLog> getLogList(Integer page, Integer size, 
                                            String keyword, String module, 
                                            String userId, LocalDateTime startTime, 
                                            LocalDateTime endTime) {
        log.info("分页查询系统日志: page={}, size={}, keyword={}, module={}, userId={}", 
                page, size, keyword, module, userId);
        
        Page<SystemLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();
        
        // 关键字搜索（用户名、操作、内容）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SystemLog::getUsername, keyword)
                    .or().like(SystemLog::getOperation, keyword)
                    .or().like(SystemLog::getContent, keyword));
        }
        
        // 模块筛选
        if (StringUtils.hasText(module)) {
            wrapper.eq(SystemLog::getModule, module);
        }
        
        // 用户筛选
        if (StringUtils.hasText(userId)) {
            wrapper.eq(SystemLog::getUserId, userId);
        }
        
        // 时间范围筛选
        if (startTime != null) {
            wrapper.ge(SystemLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SystemLog::getCreateTime, endTime);
        }
        
        wrapper.orderByDesc(SystemLog::getCreateTime);
        
        IPage<SystemLog> logPage = systemLogRepository.selectPage(pageParam, wrapper);
        
        return PageResult.<SystemLog>builder()
                .records(logPage.getRecords())
                .total(logPage.getTotal())
                .current(logPage.getCurrent())
                .size(logPage.getSize())
                .build();
    }
    
    /**
     * 清理过期日志（保留最近N天的日志）
     */
    @Transactional(rollbackFor = Exception.class)
    public int cleanExpiredLogs(int retentionDays) {
        log.info("清理过期日志: retentionDays={}", retentionDays);
        
        LocalDateTime expireTime = LocalDateTime.now().minusDays(retentionDays);
        
        LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(SystemLog::getCreateTime, expireTime);
        
        int count = systemLogRepository.delete(wrapper);
        log.info("清理过期日志完成: count={}", count);
        
        return count;
    }
}
