package com.blockchain.certificate.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_system_config")
public class SystemConfig {
    
    /**
     * 配置ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    /**
     * 配置键（唯一）
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 配置描述
     */
    private String description;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
