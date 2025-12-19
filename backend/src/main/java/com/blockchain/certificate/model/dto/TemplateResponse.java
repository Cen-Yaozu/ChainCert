package com.blockchain.certificate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 证书模板响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateResponse {
    
    /**
     * 模板ID
     */
    private String id;
    
    /**
     * 模板名称
     */
    private String name;
    
    /**
     * 模板类型（GRADUATION-毕业证书, DEGREE-学位证书, HONOR-荣誉证书）
     */
    private String type;
    
    /**
     * 模板内容（HTML格式）
     */
    private String content;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}