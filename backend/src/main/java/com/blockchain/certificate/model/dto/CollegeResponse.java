package com.blockchain.certificate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学院响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeResponse {
    
    /**
     * 学院ID
     */
    private String id;
    
    /**
     * 学院名称
     */
    private String name;
    
    /**
     * 学院代码
     */
    private String code;
    
    /**
     * 审批人ID
     */
    private String approverId;
    
    /**
     * 审批人姓名
     */
    private String approverName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}