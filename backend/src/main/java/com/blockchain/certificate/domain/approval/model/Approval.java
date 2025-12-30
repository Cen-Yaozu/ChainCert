package com.blockchain.certificate.domain.approval.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审批记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_approval")
public class Approval {
    
    /**
     * 审批记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 申请ID
     */
    private Long applicationId;
    
    /**
     * 审批人ID
     */
    private Long approverId;
    
    /**
     * 审批级别：COLLEGE, SCHOOL
     */
    private String approvalLevel;
    
    /**
     * 审批动作：APPROVE, REJECT
     */
    private String action;
    
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime approvalTime;
}
