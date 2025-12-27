package com.blockchain.certificate.infrastructure.blockchain;

import com.blockchain.certificate.shared.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 区块链服务类
 * 提供证书存证、验证等区块链相关功能
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true", matchIfMissing = false)
public class BlockchainService {

    @Autowired
    private CertificateContract certificateContract;
    
    @Autowired
    private Client client;

    /**
     * 存储证书到区块链
     * @param certificateNo 证书编号
     * @param fileHash 文件哈希值
     * @return 区块链存证结果
     */
    public BlockchainStorageResult storeCertificate(String certificateNo, String fileHash) {
        log.info("开始区块链存证，证书编号: {}, 文件哈希: {}", certificateNo, fileHash);
        
        try {
            // 调用智能合约存储证书
            TransactionReceipt receipt = certificateContract.storeCertificate(certificateNo, fileHash);
            
            if (!receipt.isStatusOK()) {
                log.error("区块链存证失败，状态码: {}", receipt.getStatus());
                throw new BusinessException("区块链存证失败");
            }
            
            // 构建返回结果
            BlockchainStorageResult result = new BlockchainStorageResult();
            result.setSuccess(true);
            result.setCertificateNo(certificateNo);
            result.setFileHash(fileHash);
            result.setTransactionHash(receipt.getTransactionHash());
            result.setBlockNumber(Long.parseLong(receipt.getBlockNumber()));
            result.setTimestamp(System.currentTimeMillis());
            
            log.info("区块链存证成功，交易哈希: {}, 区块号: {}",
                receipt.getTransactionHash(), receipt.getBlockNumber());
            
            return result;
            
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
        log.info("开始区块链验证，证书编号: {}, 文件哈希: {}", certificateNo, fileHash);
        
        try {
            // 调用智能合约验证证书
            CertificateContract.CertificateVerificationResult contractResult =
                certificateContract.verifyCertificate(certificateNo, fileHash);
            
            // 构建返回结果
            CertificateVerificationResult result = new CertificateVerificationResult();
            result.setCertificateNo(certificateNo);
            result.setFileHash(fileHash);
            result.setValid(contractResult.isValid());
            result.setTimestamp(contractResult.getTimestamp());
            result.setVerificationTime(System.currentTimeMillis());
            
            log.info("区块链验证完成，结果: {}", contractResult.isValid());
            
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
        log.info("查询区块链证书信息，证书编号: {}", certificateNo);
        
        try {
            // 调用智能合约获取证书信息
            CertificateContract.CertificateInfo contractInfo =
                certificateContract.getCertificate(certificateNo);
            
            // 构建返回结果
            CertificateBlockchainInfo result = new CertificateBlockchainInfo();
            result.setCertificateNo(contractInfo.getCertificateNo());
            result.setFileHash(contractInfo.getFileHash());
            result.setIssuer(contractInfo.getIssuer());
            result.setTimestamp(contractInfo.getTimestamp());
            result.setExists(contractInfo.isExists());
            result.setRevoked(contractInfo.isRevoked());
            
            log.info("查询区块链证书信息成功");
            
            return result;
            
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
        log.info("开始区块链撤销，证书编号: {}", certificateNo);
        
        try {
            // 调用智能合约撤销证书
            TransactionReceipt receipt = certificateContract.revokeCertificate(certificateNo);
            
            if (!receipt.isStatusOK()) {
                log.error("区块链撤销失败，状态码: {}", receipt.getStatus());
                throw new BusinessException("区块链撤销失败");
            }
            
            // 构建返回结果
            BlockchainRevocationResult result = new BlockchainRevocationResult();
            result.setSuccess(true);
            result.setCertificateNo(certificateNo);
            result.setTransactionHash(receipt.getTransactionHash());
            result.setBlockNumber(Long.parseLong(receipt.getBlockNumber()));
            result.setRevocationTime(System.currentTimeMillis());
            
            log.info("区块链撤销成功，交易哈希: {}", receipt.getTransactionHash());
            
            return result;
            
        } catch (Exception e) {
            log.error("区块链撤销失败，证书编号: {}", certificateNo, e);
            throw new BusinessException("区块链撤销失败: " + e.getMessage());
        }
    }

    /**
     * 检查区块链连接状态
     * @return 连接状态
     */
    public boolean isBlockchainConnected() {
        try {
            // 尝试获取区块高度来检查连接
            client.getBlockNumber();
            return true;
        } catch (Exception e) {
            log.warn("区块链连接检查失败", e);
            return false;
        }
    }

    /**
     * 获取区块链网络状态
     * @return 网络状态
     */
    public Object getNetworkStatus() {
        try {
            return client.getNodeVersion();
        } catch (Exception e) {
            log.error("获取区块链网络状态失败", e);
            return null;
        }
    }

    /**
     * 获取当前区块高度
     * @return 区块高度
     */
    public Long getCurrentBlockNumber() {
        try {
            return client.getBlockNumber().getBlockNumber().longValue();
        } catch (Exception e) {
            log.error("获取区块高度失败", e);
            return null;
        }
    }

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
        
        public long getVerificationTime() { return verificationTime; }
        public void setVerificationTime(long verificationTime) { this.verificationTime = verificationTime; }

        @Override
        public String toString() {
            return "CertificateVerificationResult{" +
                    "certificateNo='" + certificateNo + '\'' +
                    ", valid=" + valid +
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
        private boolean exists;
        private boolean revoked;

        // Getters and Setters
        public String getCertificateNo() { return certificateNo; }
        public void setCertificateNo(String certificateNo) { this.certificateNo = certificateNo; }
        
        public String getFileHash() { return fileHash; }
        public void setFileHash(String fileHash) { this.fileHash = fileHash; }
        
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public boolean isExists() { return exists; }
        public void setExists(boolean exists) { this.exists = exists; }
        
        public boolean isRevoked() { return revoked; }
        public void setRevoked(boolean revoked) { this.revoked = revoked; }

        @Override
        public String toString() {
            return "CertificateBlockchainInfo{" +
                    "certificateNo='" + certificateNo + '\'' +
                    ", fileHash='" + fileHash + '\'' +
                    ", issuer='" + issuer + '\'' +
                    ", timestamp=" + timestamp +
                    ", exists=" + exists +
                    ", revoked=" + revoked +
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
}
