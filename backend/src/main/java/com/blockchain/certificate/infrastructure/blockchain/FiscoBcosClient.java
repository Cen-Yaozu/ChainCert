package com.blockchain.certificate.infrastructure.blockchain;

import com.blockchain.certificate.infrastructure.config.BlockchainConfig;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

/**
 * FISCO BCOS 客户端封装类
 * 提供区块链交互的基础功能
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true", matchIfMissing = false)
public class FiscoBcosClient {

    @Autowired
    private Client client;

    @Autowired
    private CryptoKeyPair cryptoKeyPair;

    @Autowired
    private BlockchainConfig blockchainConfig;

    /**
     * 获取当前区块高度
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public BigInteger getBlockNumber() throws Exception {
        try {
            log.debug("获取当前区块高度");
            BigInteger blockNumber = client.getBlockNumber().getBlockNumber();
            log.debug("当前区块高度: {}", blockNumber);
            return blockNumber;
        } catch (Exception e) {
            log.error("获取区块高度失败", e);
            throw e;
        }
    }

    /**
     * 根据交易哈希获取交易回执
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public TransactionReceipt getTransactionReceipt(String transactionHash) throws Exception {
        try {
            log.debug("获取交易回执，交易哈希: {}", transactionHash);
            // SDK 3.x返回Optional<TransactionReceipt>，需要处理
            TransactionReceipt receipt = client.getTransactionReceipt(transactionHash)
                .getTransactionReceipt()
                .orElse(null);
            
            if (receipt != null) {
                log.debug("交易回执获取成功，状态: {}, 区块号: {}",
                    receipt.getStatus(), receipt.getBlockNumber());
            } else {
                log.warn("交易回执不存在，交易哈希: {}", transactionHash);
            }
            
            return receipt;
        } catch (Exception e) {
            log.error("获取交易回执失败，交易哈希: {}", transactionHash, e);
            throw e;
        }
    }

    /**
     * 检查交易是否成功
     */
    public boolean isTransactionSuccessful(TransactionReceipt receipt) {
        if (receipt == null) {
            return false;
        }
        
        // FISCO BCOS 中，状态码为 "0x0" 表示成功
        String status = receipt.getStatus();
        boolean isSuccess = "0x0".equals(status);
        
        if (!isSuccess) {
            log.warn("交易执行失败，状态码: {}, 交易哈希: {}", status, receipt.getTransactionHash());
        }
        
        return isSuccess;
    }

    /**
     * 检查区块链连接状态
     */
    public boolean isConnected() {
        try {
            getBlockNumber();
            return true;
        } catch (Exception e) {
            log.warn("区块链连接检查失败", e);
            return false;
        }
    }

    /**
     * 获取网络状态信息
     */
    public NetworkStatus getNetworkStatus() {
        NetworkStatus status = new NetworkStatus();
        
        try {
            // 获取区块高度
            BigInteger blockNumber = getBlockNumber();
            status.setBlockNumber(blockNumber.longValue());
            status.setConnected(true);
            
            // 获取节点信息
            status.setNodeCount(blockchainConfig.getNodes().size());
            status.setGroupId(blockchainConfig.getGroupId());
            
            log.debug("网络状态: 连接正常, 区块高度: {}, 节点数: {}", 
                blockNumber, status.getNodeCount());
                
        } catch (Exception e) {
            log.error("获取网络状态失败", e);
            status.setConnected(false);
            status.setErrorMessage(e.getMessage());
        }
        
        return status;
    }

    /**
     * 获取客户端实例
     */
    public Client getClient() {
        return client;
    }

    /**
     * 获取密钥对
     */
    public CryptoKeyPair getCryptoKeyPair() {
        return cryptoKeyPair;
    }

    /**
     * 网络状态信息类
     */
    public static class NetworkStatus {
        private boolean connected;
        private long blockNumber;
        private int nodeCount;
        private int groupId;
        private String errorMessage;

        // Getters and Setters
        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        public long getBlockNumber() {
            return blockNumber;
        }

        public void setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
        }

        public int getNodeCount() {
            return nodeCount;
        }

        public void setNodeCount(int nodeCount) {
            this.nodeCount = nodeCount;
        }

        public int getGroupId() {
            return groupId;
        }

        public void setGroupId(int groupId) {
            this.groupId = groupId;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        @Override
        public String toString() {
            return "NetworkStatus{" +
                    "connected=" + connected +
                    ", blockNumber=" + blockNumber +
                    ", nodeCount=" + nodeCount +
                    ", groupId=" + groupId +
                    ", errorMessage='" + errorMessage + '\'' +
                    '}';
        }
    }
}