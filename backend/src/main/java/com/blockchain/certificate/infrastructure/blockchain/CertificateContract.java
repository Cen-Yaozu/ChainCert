package com.blockchain.certificate.infrastructure.blockchain;

import com.blockchain.certificate.infrastructure.config.BlockchainConfig;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * 证书存证智能合约 Java 包装类
 * 提供与智能合约交互的高级接口
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true", matchIfMissing = false)
public class CertificateContract {

    @Autowired
    private Client client;

    @Autowired
    private CryptoKeyPair cryptoKeyPair;

    @Autowired
    private BlockchainConfig blockchainConfig;

    private AssembleTransactionProcessor transactionProcessor;

    /**
     * 合约ABI定义
     */
    private static final String CONTRACT_ABI = "[{\"constant\":false,\"inputs\":[{\"name\":\"issuer\",\"type\":\"address\"}],\"name\":\"addAuthorizedIssuer\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"authorizedIssuers\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"certificateExists\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"getCertificate\",\"outputs\":[{\"name\":\"certNo\",\"type\":\"string\"},{\"name\":\"fileHash\",\"type\":\"string\"},{\"name\":\"issuer\",\"type\":\"address\"},{\"name\":\"timestamp\",\"type\":\"uint256\"},{\"name\":\"exists\",\"type\":\"bool\"},{\"name\":\"revoked\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getVersion\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"pure\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"isCertificateRevoked\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"owner\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"issuer\",\"type\":\"address\"}],\"name\":\"removeAuthorizedIssuer\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"revokeCertificate\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"},{\"name\":\"fileHash\",\"type\":\"string\"}],\"name\":\"storeCertificate\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"},{\"name\":\"fileHash\",\"type\":\"string\"}],\"name\":\"verifyCertificate\",\"outputs\":[{\"name\":\"isValid\",\"type\":\"bool\"},{\"name\":\"timestamp\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"certificateNo\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"fileHash\",\"type\":\"string\"},{\"indexed\":true,\"name\":\"issuer\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"timestamp\",\"type\":\"uint256\"}],\"name\":\"CertificateStored\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"certificateNo\",\"type\":\"string\"},{\"indexed\":true,\"name\":\"revoker\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"timestamp\",\"type\":\"uint256\"}],\"name\":\"CertificateRevoked\",\"type\":\"event\"}]";

    private String contractAddress;
    
    @PostConstruct
    public void init() {
        try {
            // 获取合约地址
            this.contractAddress = blockchainConfig.getContractAddress();
            
            // 使用 AssembleTransactionProcessor 直接创建，不需要 ABI/BIN 目录
            this.transactionProcessor = TransactionProcessorFactory.createAssembleTransactionProcessor(
                client, cryptoKeyPair);
            
            if (StringUtils.hasText(contractAddress)) {
                log.info("证书合约包装类初始化成功，合约地址: {}", contractAddress);
            } else {
                log.warn("合约地址未配置，请先部署合约或配置合约地址");
            }
        } catch (Exception e) {
            log.error("初始化证书合约包装类失败", e);
            throw new RuntimeException("Failed to initialize CertificateContract", e);
        }
    }

    /**
     * 存储证书到区块链
     * @param certificateNo 证书编号
     * @param fileHash 文件哈希值
     * @return 交易回执
     */
    public TransactionReceipt storeCertificate(String certificateNo, String fileHash) throws Exception {
        validateContractAddress();
        
        log.info("存储证书到区块链，证书编号: {}, 文件哈希: {}", certificateNo, fileHash);
        
        try {
            // SDK 3.x: sendTransactionAndGetResponse需要传入函数名和参数列表（Object类型）
            List<Object> params = Arrays.asList(certificateNo, fileHash);
            TransactionResponse response = transactionProcessor.sendTransactionAndGetResponse(
                contractAddress, CONTRACT_ABI, "storeCertificate", params);
            
            TransactionReceipt receipt = response.getTransactionReceipt();
            
            if (receipt.isStatusOK()) {
                log.info("证书存储成功，交易哈希: {}, 区块号: {}",
                    receipt.getTransactionHash(), receipt.getBlockNumber());
            } else {
                log.error("证书存储失败，状态码: {}, 交易哈希: {}",
                    receipt.getStatus(), receipt.getTransactionHash());
            }
            
            return receipt;
        } catch (Exception e) {
            log.error("存储证书到区块链失败，证书编号: {}", certificateNo, e);
            throw e;
        }
    }

    /**
     * 验证证书
     * @param certificateNo 证书编号
     * @param fileHash 文件哈希值
     * @return 验证结果
     */
    public CertificateVerificationResult verifyCertificate(String certificateNo, String fileHash) throws Exception {
        validateContractAddress();
        
        log.debug("验证证书，证书编号: {}, 文件哈希: {}", certificateNo, fileHash);
        
        try {
            // SDK 3.x: sendCall需要传入合约地址、ABI、函数名和参数列表
            List<Object> params = Arrays.asList(certificateNo, fileHash);
            CallResponse response = transactionProcessor.sendCall(
                cryptoKeyPair.getAddress(), contractAddress, CONTRACT_ABI, "verifyCertificate", params);
            
            // SDK 3.x: 使用getReturnObject()获取返回值
            List<Object> results = response.getReturnObject();
            if (results != null && results.size() >= 2) {
                boolean isValid = (Boolean) results.get(0);
                BigInteger timestamp = (BigInteger) results.get(1);
                
                log.debug("证书验证结果，有效性: {}, 时间戳: {}", isValid, timestamp);
                
                return new CertificateVerificationResult(isValid, timestamp.longValue());
            } else {
                throw new RuntimeException("验证结果格式错误");
            }
        } catch (Exception e) {
            log.error("验证证书失败，证书编号: {}", certificateNo, e);
            throw e;
        }
    }

    /**
     * 获取证书信息
     * @param certificateNo 证书编号
     * @return 证书信息
     */
    public CertificateInfo getCertificate(String certificateNo) throws Exception {
        validateContractAddress();
        
        log.debug("获取证书信息，证书编号: {}", certificateNo);
        
        try {
            List<Object> params = Arrays.asList(certificateNo);
            CallResponse response = transactionProcessor.sendCall(
                cryptoKeyPair.getAddress(), contractAddress, CONTRACT_ABI, "getCertificate", params);
            
            List<Object> results = response.getReturnObject();
            if (results != null && results.size() >= 6) {
                String certNo = (String) results.get(0);
                String fileHash = (String) results.get(1);
                String issuer = (String) results.get(2);
                BigInteger timestamp = (BigInteger) results.get(3);
                Boolean exists = (Boolean) results.get(4);
                Boolean revoked = (Boolean) results.get(5);
                
                return new CertificateInfo(certNo, fileHash, issuer,
                    timestamp.longValue(), exists, revoked);
            } else {
                throw new RuntimeException("证书信息格式错误");
            }
        } catch (Exception e) {
            log.error("获取证书信息失败，证书编号: {}", certificateNo, e);
            throw e;
        }
    }

    /**
     * 撤销证书
     * @param certificateNo 证书编号
     * @return 交易回执
     */
    public TransactionReceipt revokeCertificate(String certificateNo) throws Exception {
        validateContractAddress();
        
        log.info("撤销证书，证书编号: {}", certificateNo);
        
        try {
            List<Object> params = Arrays.asList(certificateNo);
            TransactionResponse response = transactionProcessor.sendTransactionAndGetResponse(
                contractAddress, CONTRACT_ABI, "revokeCertificate", params);
            
            TransactionReceipt receipt = response.getTransactionReceipt();
            
            if (receipt.isStatusOK()) {
                log.info("证书撤销成功，交易哈希: {}", receipt.getTransactionHash());
            } else {
                log.error("证书撤销失败，状态码: {}", receipt.getStatus());
            }
            
            return receipt;
        } catch (Exception e) {
            log.error("撤销证书失败，证书编号: {}", certificateNo, e);
            throw e;
        }
    }

    /**
     * 检查证书是否存在
     * @param certificateNo 证书编号
     * @return 是否存在
     */
    public boolean certificateExists(String certificateNo) throws Exception {
        validateContractAddress();
        
        try {
            List<Object> params = Arrays.asList(certificateNo);
            CallResponse response = transactionProcessor.sendCall(
                cryptoKeyPair.getAddress(), contractAddress, CONTRACT_ABI, "certificateExists", params);
            
            List<Object> results = response.getReturnObject();
            if (results != null && !results.isEmpty()) {
                return (Boolean) results.get(0);
            }
            return false;
        } catch (Exception e) {
            log.error("检查证书存在性失败，证书编号: {}", certificateNo, e);
            throw e;
        }
    }

    /**
     * 检查证书是否已撤销
     * @param certificateNo 证书编号
     * @return 是否已撤销
     */
    public boolean isCertificateRevoked(String certificateNo) throws Exception {
        validateContractAddress();
        
        try {
            List<Object> params = Arrays.asList(certificateNo);
            CallResponse response = transactionProcessor.sendCall(
                cryptoKeyPair.getAddress(), contractAddress, CONTRACT_ABI, "isCertificateRevoked", params);
            
            List<Object> results = response.getReturnObject();
            if (results != null && !results.isEmpty()) {
                return (Boolean) results.get(0);
            }
            return false;
        } catch (Exception e) {
            log.error("检查证书撤销状态失败，证书编号: {}", certificateNo, e);
            throw e;
        }
    }

    /**
     * 获取合约版本
     * @return 版本号
     */
    public String getVersion() throws Exception {
        validateContractAddress();
        
        try {
            List<Object> params = Arrays.asList();
            CallResponse response = transactionProcessor.sendCall(
                cryptoKeyPair.getAddress(), contractAddress, CONTRACT_ABI, "getVersion", params);
            
            List<Object> results = response.getReturnObject();
            if (results != null && !results.isEmpty()) {
                return (String) results.get(0);
            }
            return "unknown";
        } catch (Exception e) {
            log.error("获取合约版本失败", e);
            throw e;
        }
    }

    /**
     * 更新合约地址
     * @param contractAddress 新的合约地址
     */
    public void updateContractAddress(String newContractAddress) {
        if (StringUtils.hasText(newContractAddress)) {
            // SDK 3.x: 需要重新创建交易处理器
            try {
                this.contractAddress = newContractAddress;
                this.transactionProcessor = TransactionProcessorFactory.createAssembleTransactionProcessor(
                    client, cryptoKeyPair, CONTRACT_ABI, "", newContractAddress);
                blockchainConfig.setContractAddress(newContractAddress);
                log.info("合约地址已更新: {}", newContractAddress);
            } catch (Exception e) {
                log.error("更新合约地址失败", e);
                throw new RuntimeException("Failed to update contract address", e);
            }
        }
    }

    /**
     * 验证合约地址是否已配置
     */
    private void validateContractAddress() {
        String contractAddress = blockchainConfig.getContractAddress();
        if (!StringUtils.hasText(contractAddress)) {
            throw new RuntimeException("合约地址未配置，请先部署合约或配置合约地址");
        }
    }

    /**
     * 证书验证结果类
     */
    public static class CertificateVerificationResult {
        private final boolean valid;
        private final long timestamp;

        public CertificateVerificationResult(boolean valid, long timestamp) {
            this.valid = valid;
            this.timestamp = timestamp;
        }

        public boolean isValid() {
            return valid;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "CertificateVerificationResult{" +
                    "valid=" + valid +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }

    /**
     * 证书信息类
     */
    public static class CertificateInfo {
        private final String certificateNo;
        private final String fileHash;
        private final String issuer;
        private final long timestamp;
        private final boolean exists;
        private final boolean revoked;

        public CertificateInfo(String certificateNo, String fileHash, String issuer, 
                             long timestamp, boolean exists, boolean revoked) {
            this.certificateNo = certificateNo;
            this.fileHash = fileHash;
            this.issuer = issuer;
            this.timestamp = timestamp;
            this.exists = exists;
            this.revoked = revoked;
        }

        // Getters
        public String getCertificateNo() { return certificateNo; }
        public String getFileHash() { return fileHash; }
        public String getIssuer() { return issuer; }
        public long getTimestamp() { return timestamp; }
        public boolean isExists() { return exists; }
        public boolean isRevoked() { return revoked; }

        @Override
        public String toString() {
            return "CertificateInfo{" +
                    "certificateNo='" + certificateNo + '\'' +
                    ", fileHash='" + fileHash + '\'' +
                    ", issuer='" + issuer + '\'' +
                    ", timestamp=" + timestamp +
                    ", exists=" + exists +
                    ", revoked=" + revoked +
                    '}';
        }
    }
}