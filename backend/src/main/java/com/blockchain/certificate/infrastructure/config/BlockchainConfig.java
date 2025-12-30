package com.blockchain.certificate.infrastructure.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * FISCO BCOS 区块链配置属性类
 * 配置区块链连接、账户私钥等信息
 *
 * 注意：由于FISCO BCOS SDK版本不兼容问题，暂时禁用区块链功能
 * 要启用区块链功能，需要在application.yml中设置：blockchain.enabled=true
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "fisco")
public class BlockchainConfig {

    /**
     * 配置文件路径（外部路径，优先使用）
     */
    private String configPath;

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
     * 获取配置摘要信息
     */
    public String getConfigSummary() {
        return String.format("节点: %s, GroupID: %d, 合约: %s", nodes, groupId, contract.getAddress());
    }

    /**
     * 区块链 Bean 配置类
     * 只有在 blockchain.enabled=true 时才会加载
     */
    @Slf4j
    @Configuration
    @ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true", matchIfMissing = false)
    public static class BlockchainBeanConfig {

        private final BlockchainConfig blockchainConfig;

        public BlockchainBeanConfig(BlockchainConfig blockchainConfig) {
            this.blockchainConfig = blockchainConfig;
        }

        @Bean
        public org.fisco.bcos.sdk.BcosSDK bcosSDK() {
            try {
                log.info("初始化 FISCO BCOS SDK, 节点: {}", blockchainConfig.getNodes());
                
                // 验证配置文件路径
                String configPath = blockchainConfig.getConfigPath();
                if (configPath == null || configPath.isEmpty()) {
                    throw new RuntimeException("FISCO BCOS 配置文件路径未配置，请设置 fisco.config-path");
                }
                
                File configFile = new File(configPath);
                if (!configFile.exists()) {
                    throw new RuntimeException("FISCO BCOS 配置文件不存在: " + configPath);
                }
                
                // 使用绝对路径
                String absolutePath = configFile.getAbsolutePath();
                log.info("使用配置文件: {}", absolutePath);
                
                return org.fisco.bcos.sdk.BcosSDK.build(absolutePath);
            } catch (Exception e) {
                log.error("FISCO BCOS SDK 初始化失败", e);
                throw new RuntimeException("FISCO BCOS SDK 初始化失败: " + e.getMessage(), e);
            }
        }

        @Bean
        public org.fisco.bcos.sdk.client.Client bcosClient(org.fisco.bcos.sdk.BcosSDK bcosSDK) {
            log.info("创建 FISCO BCOS 客户端, GroupID: {}", blockchainConfig.getGroupId());
            return bcosSDK.getClient(blockchainConfig.getGroupId());
        }

        @Bean
        public org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair cryptoKeyPair(org.fisco.bcos.sdk.client.Client client) {
            org.fisco.bcos.sdk.crypto.CryptoSuite cryptoSuite = client.getCryptoSuite();
            
            String privateKey = blockchainConfig.getAccount().getPrivateKey();
            if (privateKey != null && !privateKey.isEmpty()) {
                log.info("使用配置的私钥创建密钥对");
                return cryptoSuite.createKeyPair(privateKey);
            } else {
                log.info("随机生成新的密钥对");
                return cryptoSuite.createKeyPair();
            }
        }
    }
}