package com.blockchain.certificate.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 证书模板实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_certificate_template", autoResultMap = true)
public class CertificateTemplate {
    
    /**
     * 模板ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    /**
     * 模板名称
     */
    private String name;
    
    /**
     * 证书类型
     */
    private String type;
    
    /**
     * 模板内容
     */
    private String content;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 背景图片路径
     */
    private String backgroundImage;
    
    /**
     * 字段定义（JSON格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> fields;
    
    /**
     * 是否为默认模板
     */
    private Boolean isDefault;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
