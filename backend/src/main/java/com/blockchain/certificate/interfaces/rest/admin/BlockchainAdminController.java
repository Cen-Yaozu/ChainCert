package com.blockchain.certificate.interfaces.rest.admin;

import com.blockchain.certificate.infrastructure.blockchain.BlockchainService;
import com.blockchain.certificate.infrastructure.blockchain.ContractDeployer;
import com.blockchain.certificate.infrastructure.config.BlockchainConfig;
import com.blockchain.certificate.shared.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 区块链管理控制器
 * 提供合约部署、状态查询等管理功能
 */
@Slf4j
@RestController
@RequestMapping("/admin/blockchain")
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true", matchIfMissing = false)
public class BlockchainAdminController {

    @Autowired
    private ContractDeployer contractDeployer;

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private BlockchainConfig blockchainConfig;

    /**
     * 部署智能合约
     */
    @PostMapping("/deploy")
    public Result<Map<String, Object>> deployContract() {
        log.info("收到合约部署请求");
        
        try {
            // 检查是否已部署
            if (contractDeployer.isContractDeployed()) {
                String existingAddress = contractDeployer.getContractAddress();
                log.warn("合约已部署，地址: {}", existingAddress);
                
                Map<String, Object> data = new HashMap<>();
                data.put("contractAddress", existingAddress);
                data.put("alreadyDeployed", true);
                data.put("message", "合约已存在，如需重新部署请先清除配置");
                
                return Result.success(data);
            }
            
            // 部署合约
            String contractAddress = contractDeployer.deployContract();
            
            Map<String, Object> data = new HashMap<>();
            data.put("contractAddress", contractAddress);
            data.put("alreadyDeployed", false);
            data.put("message", "合约部署成功");
            
            log.info("合约部署成功，地址: {}", contractAddress);
            return Result.success(data);
            
        } catch (Exception e) {
            log.error("合约部署失败", e);
            return Result.error("合约部署失败: " + e.getMessage());
        }
    }

    /**
     * 获取区块链状态
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getBlockchainStatus() {
        log.info("查询区块链状态");
        
        try {
            Map<String, Object> status = new HashMap<>();
            
            // 连接状态
            boolean connected = blockchainService.isBlockchainConnected();
            status.put("connected", connected);
            
            // 合约状态
            boolean contractDeployed = contractDeployer.isContractDeployed();
            status.put("contractDeployed", contractDeployed);
            
            if (contractDeployed) {
                status.put("contractAddress", contractDeployer.getContractAddress());
            }
            
            // 区块高度
            if (connected) {
                Long blockNumber = blockchainService.getCurrentBlockNumber();
                status.put("blockNumber", blockNumber);
                
                // 网络版本
                Object networkStatus = blockchainService.getNetworkStatus();
                status.put("networkStatus", networkStatus);
            }
            
            // 配置信息
            status.put("nodes", blockchainConfig.getNodes());
            status.put("groupId", blockchainConfig.getGroupId());
            
            return Result.success(status);
            
        } catch (Exception e) {
            log.error("获取区块链状态失败", e);
            return Result.error("获取区块链状态失败: " + e.getMessage());
        }
    }

    /**
     * 更新合约地址
     */
    @PostMapping("/contract-address")
    public Result<String> updateContractAddress(@RequestBody Map<String, String> request) {
        String newAddress = request.get("contractAddress");
        
        if (newAddress == null || newAddress.trim().isEmpty()) {
            return Result.error("合约地址不能为空");
        }
        
        log.info("更新合约地址: {}", newAddress);
        
        try {
            blockchainConfig.setContractAddress(newAddress);
            return Result.success("合约地址更新成功");
        } catch (Exception e) {
            log.error("更新合约地址失败", e);
            return Result.error("更新合约地址失败: " + e.getMessage());
        }
    }

    /**
     * 测试区块链连接
     */
    @GetMapping("/test-connection")
    public Result<Map<String, Object>> testConnection() {
        log.info("测试区块链连接");
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            boolean connected = blockchainService.isBlockchainConnected();
            result.put("connected", connected);
            
            if (connected) {
                Long blockNumber = blockchainService.getCurrentBlockNumber();
                result.put("blockNumber", blockNumber);
                result.put("message", "区块链连接正常，当前区块高度: " + blockNumber);
            } else {
                result.put("message", "区块链连接失败");
            }
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("测试区块链连接失败", e);
            return Result.error("测试连接失败: " + e.getMessage());
        }
    }
}