package com.blockchain.certificate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 学院创建/更新请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeRequest {
    
    /**
     * 学院名称
     */
    @NotBlank(message = "学院名称不能为空")
    @Size(max = 100, message = "学院名称长度不能超过100")
    private String name;
    
    /**
     * 学院代码
     */
    @NotBlank(message = "学院代码不能为空")
    @Size(max = 20, message = "学院代码长度不能超过20")
    private String code;
    
    /**
     * 审批人ID（学院老师）
     */
    private String approverId;
}