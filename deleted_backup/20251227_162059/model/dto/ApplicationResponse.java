package com.blockchain.certificate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 申请响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    
    /**
     * 申请ID
     */
    private String id;
    
    /**
     * 申请人ID
     */
    private String applicantId;
    
    /**
     * 申请人姓名
     */
    private String applicantName;
    
    /**
     * 申请人学号
     */
    private String studentNo;
    
    /**
     * 申请标题
     */
    private String title;
    
    /**
     * 证书类型
     */
    private String certificateType;
    
    /**
     * 申请状态
     */
    private String status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
    /**
     * 学院ID
     */
    private String collegeId;
    
    /**
     * 学院名称
     */
    private String collegeName;
    
    /**
     * 证明文件列表
     */
    private List<Map<String, String>> proofFiles;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}