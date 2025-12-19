package com.blockchain.certificate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审批响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalResponse {
    
    /**
     * 审批记录ID
     */
    private String id;
    
    /**
     * 申请ID
     */
    private String applicationId;
    
    /**
     * 审批人ID
     */
    private String approverId;
    
    /**
     * 审批人姓名
     */
    private String approverName;
    
    /**
     * 审批级别：COLLEGE, SCHOOL
     */
    private String approvalLevel;
    
    /**
     * 审批级别描述
     */
    private String approvalLevelDesc;
    
    /**
     * 审批动作：APPROVE, REJECT
     */
    private String action;
    
    /**
     * 审批动作描述
     */
    private String actionDesc;
    
    /**
     * 审批意见
     */
    private String comment;
    
    /**
     * 数字签名哈希
     */
    private String signatureHash;
    
    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;
    
    /**
     * 申请新状态
     */
    private String newStatus;
    
    /**
     * 申请新状态描述
     */
    private String newStatusDesc;
}