package com.blockchain.certificate.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审批历史视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalHistoryVO {
    
    /**
     * 审批记录ID
     */
    private String id;
    
    /**
     * 申请ID
     */
    private String applicationId;
    
    /**
     * 申请标题
     */
    private String applicationTitle;
    
    /**
     * 申请人姓名
     */
    private String applicantName;
    
    /**
     * 学号
     */
    private String studentNo;
    
    /**
     * 证书类型
     */
    private String certificateType;
    
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
     * 审批时间
     */
    private LocalDateTime approvalTime;
    
    /**
     * 申请当前状态
     */
    private String currentStatus;
    
    /**
     * 申请当前状态描述
     */
    private String currentStatusDesc;
}