package com.blockchain.certificate;

import com.blockchain.certificate.blockchain.ContractDeployer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * 智能合约部署应用
 * 用于独立部署智能合约
 * 
 * 使用方法:
 * mvn spring-boot:run -Dspring-boot.run.profiles=deploy
 */
@Slf4j
@SpringBootApplication
public class DeployContractApplication {

    public static void main(String[] args) {
        // 设置 profile 为 deploy
        System.setProperty("spring.profiles.active", "deploy");
        SpringApplication.run(DeployContractApplication.class, args);
    }

    @Bean
    @Profile("deploy")
    public CommandLineRunner deployContract(ContractDeployer contractDeployer) {
        return args -> {
            try {
                log.info("========================================");
                log.info("开始部署证书存证智能合约");
                log.info("========================================");
                
                // 检查合约是否已部署
                String currentAddress = contractDeployer.getCurrentContractAddress();
                if (currentAddress != null && !currentAddress.isEmpty()) {
                    log.info("检测到已配置的合约地址: {}", currentAddress);
                    
                    if (contractDeployer.isContractDeployed(currentAddress)) {
                        log.info("合约已部署且可用，无需重新部署");
                        log.info("如需重新部署，请先清空配置文件中的合约地址");
                        System.exit(0);
                        return;
                    } else {
                        log.warn("配置的合约地址无效，将重新部署");
                    }
                }
                
                // 部署新合约
                String contractAddress = contractDeployer.deployCertificateRegistry();
                
                log.info("========================================");
                log.info("✅ 合约部署成功！");
                log.info("========================================");
                log.info("合约地址: {}", contractAddress);
                log.info("");
                log.info("请将此地址配置到 application.yml 中：");
                log.info("");
                log.info("fisco:");
                log.info("  contract:");
                log.info("    address: {}", contractAddress);
                log.info("");
                log.info("========================================");
                
                // 成功退出
                System.exit(0);
                
            } catch (Exception e) {
                log.error("========================================");
                log.error("❌ 合约部署失败");
                log.error("========================================");
                log.error("错误信息: {}", e.getMessage(), e);
                log.error("");
                log.error("请检查：");
                log.error("1. FISCO BCOS 节点是否正常运行");
                log.error("2. 网络连接是否正常");
                log.error("3. SDK 证书是否正确配置");
                log.error("========================================");
                
                // 失败退出
                System.exit(1);
            }
        };
    }
}