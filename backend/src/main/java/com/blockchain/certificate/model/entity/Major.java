package com.blockchain.certificate.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 专业实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_major")
public class Major {
    
    /**
     * 专业ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    /**
     * 学院ID
     */
    private String collegeId;
    
    /**
     * 专业名称
     */
    private String name;
    
    /**
     * 专业代码
     */
    private String code;
    
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
