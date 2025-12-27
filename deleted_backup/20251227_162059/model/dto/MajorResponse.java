package com.blockchain.certificate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 专业响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MajorResponse {
    
    /**
     * 专业ID
     */
    private String id;
    
    /**
     * 专业名称
     */
    private String name;
    
    /**
     * 专业代码
     */
    private String code;
    
    /**
     * 所属学院ID
     */
    private String collegeId;
    
    /**
     * 所属学院名称
     */
    private String collegeName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}