package com.blockchain.certificate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 证书模板请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRequest {
    
    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    private String name;
    
    /**
     * 模板类型（GRADUATION-毕业证书, DEGREE-学位证书, HONOR-荣誉证书）
     */
    @NotBlank(message = "模板类型不能为空")
    private String type;
    
    /**
     * 模板内容（HTML格式）
     */
    @NotBlank(message = "模板内容不能为空")
    private String content;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
}