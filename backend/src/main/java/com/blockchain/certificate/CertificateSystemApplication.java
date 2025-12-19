package com.blockchain.certificate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 区块链证书存证系统主应用类
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableRetry
@MapperScan("com.blockchain.certificate.repository")
public class CertificateSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CertificateSystemApplication.class, args);
    }

}
