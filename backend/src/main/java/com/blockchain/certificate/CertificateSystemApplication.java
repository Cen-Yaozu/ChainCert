package com.blockchain.certificate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 区块链证书存证系统主应用类
 *
 * DDD架构说明:
 * - domain: 领域层 (核心业务逻辑)
 * - infrastructure: 基础设施层 (区块链、IPFS、数据库)
 * - interfaces: 接口层 (REST API)
 * - shared: 共享层 (工具类、异常处理)
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableRetry
@ComponentScan(basePackages = {
    "com.blockchain.certificate.domain",          // 领域层
    "com.blockchain.certificate.infrastructure",   // 基础设施层
    "com.blockchain.certificate.interfaces",       // 接口层
    "com.blockchain.certificate.shared",           // 共享层
    "com.blockchain.certificate"                   // 保留旧包(向后兼容)
})
@MapperScan({
    "com.blockchain.certificate.domain.*.repository",  // 新Repository扫描
    "com.blockchain.certificate.repository"            // 保留旧的(向后兼容)
})
public class CertificateSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CertificateSystemApplication.class, args);
    }

}
