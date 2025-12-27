package com.blockchain.certificate.domain.application.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 申请实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_application", autoResultMap = true)
public class Application {
    
    /**
     * 申请ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    /**
     * 申请人ID
     */
    private String applicantId;
    
    /**
     * 申请标题
     */
    private String title;
    
    /**
     * 证书类型
     */
    private String certificateType;
    
    /**
     * 申请状态：PENDING_COLLEGE, PENDING_SCHOOL, APPROVED, REJECTED, CANCELLED
     */
    private String status;
    
    /**
     * 学院ID
     */
    private String collegeId;
    
    /**
     * 专业ID
     */
    private String majorId;
    
    /**
     * 证明文件列表（JSON格式）
     * 格式：[{"name": "file1.pdf", "cid": "Qm..."}]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, String>> proofFiles;
    
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
