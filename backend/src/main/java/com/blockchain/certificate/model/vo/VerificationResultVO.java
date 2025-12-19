package com.blockchain.certificate.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 证书核验结果视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResultVO {
    
    /**
     * 验证是否通过
     */
    private Boolean valid;
    
    /**
     * 证书编号
     */
    private String certificateNo;
    
    /**
     * 验证消息
     */
    private String message;
    
    /**
     * 证书信息
     */
    private CertificateInfo certificate;
    
    /**
     * 数据库验证结果
     */
    private Boolean databaseCheck;
    
    /**
     * 区块链验证结果
     */
    private Boolean blockchainCheck;
    
    /**
     * IPFS验证结果
     */
    private Boolean ipfsCheck;
    
    /**
     * 区块链交易哈希
     */
    private String transactionHash;
    
    /**
     * 区块高度
     */
    private Long blockHeight;
    
    /**
     * 区块链时间戳
     */
    private Long blockchainTimestamp;
    
    /**
     * IPFS CID
     */
    private String ipfsCid;
    
    /**
     * 下载链接
     */
    private String downloadUrl;
    
    /**
     * 验证时间
     */
    private Long verificationTime;
    
    /**
     * 证书信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateInfo {
        private String certificateNo;
        private String title;
        private String certificateType;
        private String status;
        private String issueDate;
    }
}