package com.blockchain.certificate.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 证书视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateVO {
    
    /**
     * 证书ID
     */
    private String id;
    
    /**
     * 证书编号
     */
    private String certificateNo;
    
    /**
     * 证书标题
     */
    private String title;
    
    /**
     * 证书类型
     */
    private String certificateType;
    
    /**
     * 证书状态
     */
    private String status;
    
    /**
     * 证书状态描述
     */
    private String statusDesc;
    
    /**
     * 持有人ID
     */
    private String holderId;
    
    /**
     * 持有人姓名
     */
    private String holderName;
    
    /**
     * 学号
     */
    private String studentNo;
    
    /**
     * IPFS CID
     */
    private String ipfsCid;
    
    /**
     * 文件哈希
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
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}