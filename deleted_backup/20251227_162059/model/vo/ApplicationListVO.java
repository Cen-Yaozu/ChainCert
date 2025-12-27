package com.blockchain.certificate.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 申请列表视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationListVO {
    
    /**
     * 申请ID
     */
    private String id;
    
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
     * 申请人姓名
     */
    private String applicantName;
    
    /**
     * 申请人学号
     */
    private String studentNo;
    
    /**
     * 学院名称
     */
    private String collegeName;
    
    /**
     * 证明文件数量
     */
    private Integer fileCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}