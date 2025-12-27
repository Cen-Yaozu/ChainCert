package com.blockchain.certificate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * IPFS 配置类
 * 配置 IPFS 连接参数
 *
 * 当 enabled=true 时，使用 RealIpfsClient 连接真实 IPFS 节点
 * 当 enabled=false 时，使用 MockIpfsClient 模拟实现
 */
@Configuration
@ConfigurationProperties(prefix = "ipfs")
@Data
public class IpfsConfig {

    /**
     * 是否启用真实 IPFS 连接
     * true: 使用 RealIpfsClient 连接真实 IPFS 节点
     * false: 使用 MockIpfsClient 模拟实现（默认）
     */
    private boolean enabled = false;

    /**
     * IPFS 节点主机地址
     * 本地开发环境: localhost
     * 远程服务器: 139.196.72.119
     */
    private String host = "localhost";

    /**
     * IPFS 节点端口
     */
    private int port = 5001;

    /**
     * 连接超时时间（毫秒）
     */
    private int timeout = 30000;

    /**
     * IPFS Gateway 端口（用于 HTTP 访问）
     */
    private int gatewayPort = 8081;
}