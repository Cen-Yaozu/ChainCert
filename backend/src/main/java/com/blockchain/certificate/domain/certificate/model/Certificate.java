package com.blockchain.certificate.domain.certificate.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 证书实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_certificate")
public class Certificate {
    
    /**
     * 证书ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 证书编号（全局唯一）
     */
    private String certificateNo;
    
    /**
     * 申请ID
     */
    private Long applicationId;
    
    /**
     * 持有人ID
     */
    private Long holderId;
    
    /**
     * 证书标题
     */
    private String title;
    
    /**
     * 证书类型
     */
    private String certificateType;
    
    /**
     * 证书状态：VALID, REVOKED, EXPIRED
     */
    private String status;
    
    /**
     * IPFS CID
     */
    private String ipfsCid;
    
    /**
     * 文件哈希值
     */
    private String fileHash;
    
    /**
     * 区块链交易哈希
     */
    private String blockchainTxHash;
    
    /**
     * 区块高度
     */
    private Long blockHeight;
    
    /**
     * 颁发日期
     */
    private LocalDate issueDate;
    
    /**
     * 过期日期
     */
    private LocalDate expiryDate;
    
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
    
    /**
     * 类型别名（用于兼容）
     */
    public String getType() {
        return this.certificateType;
    }
    
    public void setType(String type) {
        this.certificateType = type;
    }
}
