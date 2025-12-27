package com.blockchain.certificate.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * FISCO BCOS 区块链配置类
 * 配置区块链连接、账户私钥等信息
 *
 * 注意：由于FISCO BCOS SDK版本不兼容问题，暂时禁用区块链功能
 * 要启用区块链功能，需要在application.yml中设置：blockchain.enabled=true
 * 并升级FISCO BCOS SDK到兼容版本
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "fisco")
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true", matchIfMissing = false)
public class BlockchainConfig {

    /**
     * 区块链节点列表
     */
    private List<String> nodes;

    /**
     * 群组ID
     */
    private Integer groupId = 1;

    /**
     * 账户配置
     */
    private Account account = new Account();

    /**
     * 合约配置
     */
    private Contract contract = new Contract();

    @Data
    public static class Account {
        /**
         * 私钥（十六进制字符串）
         */
        private String privateKey;
    }

    @Data
    public static class Contract {
        /**
         * 合约地址
         */
        private String address;
    }

    @org.springframework.context.annotation.Bean
    public org.fisco.bcos.sdk.BcosSDK bcosSDK() {
        try {
            log.info("初始化 FISCO BCOS SDK, 节点: {}", nodes);
            // 使用 classpath 下的配置文件
            // 注意：在打包运行环境可能需要特殊处理，这里适用于开发环境
            String configFile = "classpath:fisco-config.yaml";
            return org.fisco.bcos.sdk.BcosSDK.build(configFile);
        } catch (Exception e) {
            log.error("FISCO BCOS SDK 初始化失败", e);
            throw new RuntimeException("FISCO BCOS SDK 初始化失败", e);
        }
    }

    @org.springframework.context.annotation.Bean
    public org.fisco.bcos.sdk.client.Client bcosClient(org.fisco.bcos.sdk.BcosSDK bcosSDK) {
        log.info("创建 FISCO BCOS 客户端, GroupID: {}", groupId);
        return bcosSDK.getClient(groupId);
    }

    @org.springframework.context.annotation.Bean
    public org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair cryptoKeyPair(org.fisco.bcos.sdk.client.Client client) {
        org.fisco.bcos.sdk.crypto.CryptoSuite cryptoSuite = client.getCryptoSuite();
        
        if (account.getPrivateKey() != null && !account.getPrivateKey().isEmpty()) {
            log.info("使用配置的私钥创建密钥对");
            return cryptoSuite.createKeyPair(account.getPrivateKey());
        } else {
            log.info("随机生成新的密钥对");
            return cryptoSuite.createKeyPair();
        }
    }

    /**
     * 获取合约地址
     */
    public String getContractAddress() {
        return contract.getAddress();
    }

    /**
     * 设置合约地址
     */
    public void setContractAddress(String address) {
        this.contract.setAddress(address);
        log.info("更新合约地址: {}", address);
    }

    /**
     * 检查区块链连接状态
     */
    public boolean isConnected() {
        // 由于没有直接注入 BcosSDK 或 Client，这里暂时无法检查
        // 实际应用中可以注入 Client 并调用 getBlockNumber() 来检查
        return true;
    }

    /**
     * 获取当前区块高度
     */
    public Long getCurrentBlockNumber() {
        // 同上
        return 0L;
    }

    /**
     * 获取配置摘要信息
     */
    public String getConfigSummary() {
        return String.format("节点: %s, GroupID: %d, 合约: %s", nodes, groupId, contract.getAddress());
    }
}