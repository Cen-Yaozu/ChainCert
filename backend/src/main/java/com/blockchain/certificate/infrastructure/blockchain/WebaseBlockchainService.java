package com.blockchain.certificate.infrastructure.blockchain;

import com.blockchain.certificate.shared.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 基于 WeBASE-Front 的区块链服务
 * 通过 HTTP API 与区块链交互，无需 SDK 证书
 * 
 * 当 webase.enabled=true 时启用此服务
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "webase.enabled", havingValue = "true", matchIfMissing = false)
public class WebaseBlockchainService {

    @Autowired
    private WebaseFrontClient webaseFrontClient;

    /**
     * 存储证书到区块链（永久有效）
     * @param certificateNo 证书编号
     * @param fileHash 文件哈希值
     * @return 区块链存证结果
     */
    public BlockchainStorageResult storeCertificate(String certificateNo, String fileHash) {
        return storeCertificate(certificateNo, fileHash, 0);
    }

    /**
     * 存储证书到区块链（带过期时间）
     * @param certificateNo 证书编号
     * @param fileHash 文件哈希值
     * @param expiryDate 过期时间戳（秒），0表示永不过期
     * @return 区块链存证结果
     */
    public BlockchainStorageResult storeCertificate(String certificateNo, String fileHash, long expiryDate) {
        log.info("开始区块链存证（WeBASE），证书编号: {}, 文件哈希: {}, 过期时间: {}", 
                certificateNo, fileHash, expiryDate);
        
        try {
            WebaseFrontClient.TransactionResult txResult = 
                webaseFrontClient.storeCertificate(certificateNo, fileHash, expiryDate);
            
            if (!txResult.isSuccess()) {
                log.error("区块链存证失败: {}", txResult.getErrorMessage());
                throw new BusinessException("区块链存证失败: " + txResult.getErrorMessage());
            }
            
            // 构建返回结果
            BlockchainStorageResult result = new BlockchainStorageResult();
            result.setSuccess(true);
            result.setCertificateNo(certificateNo);
            result.setFileHash(fileHash);
            result.setTransactionHash(txResult.getTransactionHash());
            result.setBlockNumber(txResult.getBlockNumber());
            result.setTimestamp(System.currentTimeMillis());
            
            log.info("区块链存证成功（WeBASE），交易哈希: {}, 区块号: {}",
                txResult.getTransactionHash(), txResult.getBlockNumber());
            
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("区块链存证失败，证书编号: {}", certificateNo, e);
            throw new BusinessException("区块链存证失败: " + e.getMessage());
        }
    }

    /**
     * 验证证书
     * @param certificateNo 证书编号
     * @param fileHash 文件哈希值
     * @return 验证结果
     */
    public CertificateVerificationResult verifyCertificate(String certificateNo, String fileHash) {
        log.info("开始区块链验证（WeBASE），证书编号: {}, 文件哈希: {}", certificateNo, fileHash);
        
        try {
            WebaseFrontClient.VerificationResult verifyResult = 
                webaseFrontClient.verifyCertificate(certificateNo, fileHash);
            
            // 构建返回结果
            CertificateVerificationResult result = new CertificateVerificationResult();
            result.setCertificateNo(certificateNo);
            result.setFileHash(fileHash);
            result.setValid(verifyResult.isValid());
            result.setTimestamp(verifyResult.getTimestamp());
            result.setStatus(verifyResult.getStatus());
            result.setStatusDescription(verifyResult.getStatusDescription());
            result.setVerificationTime(System.currentTimeMillis());
            
            log.info("区块链验证完成（WeBASE），结果: {}, 状态: {}", 
                    verifyResult.isValid(), verifyResult.getStatusDescription());
            
            return result;
            
        } catch (Exception e) {
            log.error("区块链验证失败，证书编号: {}", certificateNo, e);
            throw new BusinessException("区块链验证失败: " + e.getMessage());
        }
    }

    /**
     * 获取证书信息
     * @param certificateNo 证书编号
     * @return 证书信息
     */
    public CertificateBlockchainInfo getCertificate(String certificateNo) {
        log.info("查询区块链证书信息（WeBASE），证书编号: {}", certificateNo);
        
        try {
            WebaseFrontClient.CertificateInfo certInfo = 
                webaseFrontClient.getCertificate(certificateNo);
            
            if (certInfo == null) {
                throw new BusinessException("证书不存在");
            }
            
            // 构建返回结果
            CertificateBlockchainInfo result = new CertificateBlockchainInfo();
            result.setCertificateNo(certInfo.getCertificateNo());
            result.setFileHash(certInfo.getFileHash());
            result.setIssuer(certInfo.getIssuer());
            result.setTimestamp(certInfo.getTimestamp());
            result.setExpiryDate(certInfo.getExpiryDate());
            result.setExists(certInfo.isExists());
            result.setRevoked(certInfo.isRevoked());
            result.setExpired(certInfo.isExpired());
            
            log.info("查询区块链证书信息成功（WeBASE）");
            
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询区块链证书信息失败，证书编号: {}", certificateNo, e);
            throw new BusinessException("查询区块链证书信息失败: " + e.getMessage());
        }
    }

    /**
     * 撤销证书
     * @param certificateNo 证书编号
     * @return 撤销结果
     */
    public BlockchainRevocationResult revokeCertificate(String certificateNo) {
        log.info("开始区块链撤销（WeBASE），证书编号: {}", certificateNo);
        
        try {
            WebaseFrontClient.TransactionResult txResult = 
                webaseFrontClient.revokeCertificate(certificateNo);
            
            if (!txResult.isSuccess()) {
                log.error("区块链撤销失败: {}", txResult.getErrorMessage());
                throw new BusinessException("区块链撤销失败: " + txResult.getErrorMessage());
            }
            
            // 构建返回结果
            BlockchainRevocationResult result = new BlockchainRevocationResult();
            result.setSuccess(true);
            result.setCertificateNo(certificateNo);
            result.setTransactionHash(txResult.getTransactionHash());
            result.setBlockNumber(txResult.getBlockNumber());
            result.setRevocationTime(System.currentTimeMillis());
            
            log.info("区块链撤销成功（WeBASE），交易哈希: {}", txResult.getTransactionHash());
            
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("区块链撤销失败，证书编号: {}", certificateNo, e);
            throw new BusinessException("区块链撤销失败: " + e.getMessage());
        }
    }

    /**
     * 更新证书过期时间
     * @param certificateNo 证书编号
     * @param newExpiryDate 新的过期时间戳（秒），0表示永不过期
     * @return 更新结果
     */
    public BlockchainUpdateResult updateExpiryDate(String certificateNo, long newExpiryDate) {
        log.info("开始更新证书过期时间（WeBASE），证书编号: {}, 新过期时间: {}", certificateNo, newExpiryDate);
        
        try {
            WebaseFrontClient.TransactionResult txResult = 
                webaseFrontClient.updateExpiryDate(certificateNo, newExpiryDate);
            
            if (!txResult.isSuccess()) {
                log.error("更新过期时间失败: {}", txResult.getErrorMessage());
                throw new BusinessException("更新过期时间失败: " + txResult.getErrorMessage());
            }
            
            // 构建返回结果
            BlockchainUpdateResult result = new BlockchainUpdateResult();
            result.setSuccess(true);
            result.setCertificateNo(certificateNo);
            result.setTransactionHash(txResult.getTransactionHash());
            result.setBlockNumber(txResult.getBlockNumber());
            result.setUpdateTime(System.currentTimeMillis());
            
            log.info("更新过期时间成功（WeBASE），交易哈希: {}", txResult.getTransactionHash());
            
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新过期时间失败，证书编号: {}", certificateNo, e);
            throw new BusinessException("更新过期时间失败: " + e.getMessage());
        }
    }

    /**
     * 检查区块链连接状态
     * @return 连接状态
     */
    public boolean isBlockchainConnected() {
        try {
            return webaseFrontClient.testConnection();
        } catch (Exception e) {
            log.warn("区块链连接检查失败（WeBASE）", e);
            return false;
        }
    }

    /**
     * 获取区块链网络状态
     * @return 网络状态
     */
    public Object getNetworkStatus() {
        try {
            return webaseFrontClient.getNodeVersion();
        } catch (Exception e) {
            log.error("获取区块链网络状态失败（WeBASE）", e);
            return null;
        }
    }

    /**
     * 获取当前区块高度
     * @return 区块高度
     */
    public Long getCurrentBlockNumber() {
        try {
            return webaseFrontClient.getBlockNumber();
        } catch (Exception e) {
            log.error("获取区块高度失败（WeBASE）", e);
            return null;
        }
    }

    /**
     * 获取合约版本
     * @return 合约版本
     */
    public String getContractVersion() {
        try {
            return webaseFrontClient.getContractVersion();
        } catch (Exception e) {
            log.error("获取合约版本失败（WeBASE）", e);
            return null;
        }
    }

    /**
     * 获取合约地址
     * @return 合约地址
     */
    public String getContractAddress() {
        return webaseFrontClient.getContractAddress();
    }

    // ========== 结果类 ==========

    /**
     * 区块链存证结果类
     */
    public static class BlockchainStorageResult {
        private boolean success;
        private String certificateNo;
        private String fileHash;
        private String transactionHash;
        private long blockNumber;
        private long timestamp;

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getCertificateNo() { return certificateNo; }
        public void setCertificateNo(String certificateNo) { this.certificateNo = certificateNo; }
        
        public String getFileHash() { return fileHash; }
        public void setFileHash(String fileHash) { this.fileHash = fileHash; }
        
        public String getTransactionHash() { return transactionHash; }
        public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }
        
        public long getBlockNumber() { return blockNumber; }
        public void setBlockNumber(long blockNumber) { this.blockNumber = blockNumber; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        @Override
        public String toString() {
            return "BlockchainStorageResult{" +
                    "success=" + success +
                    ", certificateNo='" + certificateNo + '\'' +
                    ", transactionHash='" + transactionHash + '\'' +
                    ", blockNumber=" + blockNumber +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }

    /**
     * 证书验证结果类
     */
    public static class CertificateVerificationResult {
        private String certificateNo;
        private String fileHash;
        private boolean valid;
        private long timestamp;
        private int status;
        private String statusDescription;
        private long verificationTime;

        // Getters and Setters
        public String getCertificateNo() { return certificateNo; }
        public void setCertificateNo(String certificateNo) { this.certificateNo = certificateNo; }
        
        public String getFileHash() { return fileHash; }
        public void setFileHash(String fileHash) { this.fileHash = fileHash; }
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }
        
        public String getStatusDescription() { return statusDescription; }
        public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }
        
        public long getVerificationTime() { return verificationTime; }
        public void setVerificationTime(long verificationTime) { this.verificationTime = verificationTime; }

        @Override
        public String toString() {
            return "CertificateVerificationResult{" +
                    "certificateNo='" + certificateNo + '\'' +
                    ", valid=" + valid +
                    ", status=" + status +
                    ", statusDescription='" + statusDescription + '\'' +
                    ", timestamp=" + timestamp +
                    ", verificationTime=" + verificationTime +
                    '}';
        }
    }

    /**
     * 证书区块链信息类
     */
    public static class CertificateBlockchainInfo {
        private String certificateNo;
        private String fileHash;
        private String issuer;
        private long timestamp;
        private long expiryDate;
        private boolean exists;
        private boolean revoked;
        private boolean expired;

        // Getters and Setters
        public String getCertificateNo() { return certificateNo; }
        public void setCertificateNo(String certificateNo) { this.certificateNo = certificateNo; }
        
        public String getFileHash() { return fileHash; }
        public void setFileHash(String fileHash) { this.fileHash = fileHash; }
        
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public long getExpiryDate() { return expiryDate; }
        public void setExpiryDate(long expiryDate) { this.expiryDate = expiryDate; }
        
        public boolean isExists() { return exists; }
        public void setExists(boolean exists) { this.exists = exists; }
        
        public boolean isRevoked() { return revoked; }
        public void setRevoked(boolean revoked) { this.revoked = revoked; }
        
        public boolean isExpired() { return expired; }
        public void setExpired(boolean expired) { this.expired = expired; }
        
        public boolean isPermanent() { return expiryDate == 0; }

        @Override
        public String toString() {
            return "CertificateBlockchainInfo{" +
                    "certificateNo='" + certificateNo + '\'' +
                    ", fileHash='" + fileHash + '\'' +
                    ", issuer='" + issuer + '\'' +
                    ", timestamp=" + timestamp +
                    ", expiryDate=" + expiryDate +
                    ", exists=" + exists +
                    ", revoked=" + revoked +
                    ", expired=" + expired +
                    '}';
        }
    }

    /**
     * 区块链撤销结果类
     */
    public static class BlockchainRevocationResult {
        private boolean success;
        private String certificateNo;
        private String transactionHash;
        private long blockNumber;
        private long revocationTime;

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getCertificateNo() { return certificateNo; }
        public void setCertificateNo(String certificateNo) { this.certificateNo = certificateNo; }
        
        public String getTransactionHash() { return transactionHash; }
        public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }
        
        public long getBlockNumber() { return blockNumber; }
        public void setBlockNumber(long blockNumber) { this.blockNumber = blockNumber; }
        
        public long getRevocationTime() { return revocationTime; }
        public void setRevocationTime(long revocationTime) { this.revocationTime = revocationTime; }

        @Override
        public String toString() {
            return "BlockchainRevocationResult{" +
                    "success=" + success +
                    ", certificateNo='" + certificateNo + '\'' +
                    ", transactionHash='" + transactionHash + '\'' +
                    ", blockNumber=" + blockNumber +
                    ", revocationTime=" + revocationTime +
                    '}';
        }
    }

    /**
     * 区块链更新结果类
     */
    public static class BlockchainUpdateResult {
        private boolean success;
        private String certificateNo;
        private String transactionHash;
        private long blockNumber;
        private long updateTime;

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getCertificateNo() { return certificateNo; }
        public void setCertificateNo(String certificateNo) { this.certificateNo = certificateNo; }
        
        public String getTransactionHash() { return transactionHash; }
        public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }
        
        public long getBlockNumber() { return blockNumber; }
        public void setBlockNumber(long blockNumber) { this.blockNumber = blockNumber; }
        
        public long getUpdateTime() { return updateTime; }
        public void setUpdateTime(long updateTime) { this.updateTime = updateTime; }

        @Override
        public String toString() {
            return "BlockchainUpdateResult{" +
                    "success=" + success +
                    ", certificateNo='" + certificateNo + '\'' +
                    ", transactionHash='" + transactionHash + '\'' +
                    ", blockNumber=" + blockNumber +
                    ", updateTime=" + updateTime +
                    '}';
        }
    }
}