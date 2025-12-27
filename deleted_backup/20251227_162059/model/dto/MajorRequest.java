package com.blockchain.certificate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 专业请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MajorRequest {
    
    /**
     * 专业名称
     */
    @NotBlank(message = "专业名称不能为空")
    private String name;
    
    /**
     * 专业代码
     */
    private String code;
    
    /**
     * 所属学院ID
     */
    @NotBlank(message = "所属学院不能为空")
    private String collegeId;
}