package com.blockchain.certificate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 审批请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {
    
    /**
     * 审批动作：APPROVE, REJECT
     */
    @NotBlank(message = "审批动作不能为空")
    private String action;
    
    /**
     * 审批意见
     */
    private String comment;
    
    /**
     * 数字签名
     */
    @NotBlank(message = "数字签名不能为空")
    private String signature;
}