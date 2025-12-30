package com.blockchain.certificate.infrastructure.ipfs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 模拟的 IPFS 客户端，用于开发和测试环境
 * 在内存中模拟 IPFS 的文件存储功能
 *
 * 启用条件：配置 ipfs.enabled=false 或未配置（默认）
 */
@Component
@ConditionalOnProperty(name = "ipfs.enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class MockIpfsClient implements IpfsClient {

    // 模拟 IPFS 存储，使用内存存储文件
    private final ConcurrentMap<String, byte[]> storage = new ConcurrentHashMap<>();

    /**
     * 上传文件到模拟 IPFS
     * 
     * @param fileName 文件名
     * @param content 文件内容
     * @return 模拟的 IPFS CID
     * @throws IpfsException IPFS 操作异常
     */
    @Override
    public String uploadFile(String fileName, byte[] content) throws IpfsException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IpfsException("文件名不能为空");
        }
        
        if (content == null || content.length == 0) {
            throw new IpfsException("文件内容不能为空");
        }
        
        try {
            log.debug("模拟上传文件到 IPFS: {}, 大小: {} bytes", fileName, content.length);
            
            // 生成模拟的 CID（基于文件内容的哈希）
            String cid = generateMockCid(content);
            
            // 存储文件内容
            storage.put(cid, content.clone());
            
            log.debug("模拟文件上传成功: {} -> CID: {}", fileName, cid);
            
            return cid;
        } catch (Exception e) {
            log.error("模拟上传文件到 IPFS 失败: {}", fileName, e);
            throw new IpfsException("上传文件到 IPFS 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从模拟 IPFS 下载文件
     * 
     * @param cid IPFS CID
     * @return 文件内容
     * @throws IpfsException IPFS 操作异常
     */
    @Override
    public byte[] downloadFile(String cid) throws IpfsException {
        if (cid == null || cid.trim().isEmpty()) {
            throw new IpfsException("CID 不能为空");
        }
        
        log.debug("模拟从 IPFS 下载文件: {}", cid);
        
        byte[] content = storage.get(cid);
        if (content == null) {
            throw new IpfsException("文件不存在: " + cid);
        }
        
        log.debug("模拟文件下载成功: {}, 大小: {} bytes", cid, content.length);
        
        return content.clone();
    }

    /**
     * 检查文件是否存在于模拟 IPFS
     * 
     * @param cid IPFS CID
     * @return 文件是否存在
     */
    @Override
    public boolean fileExists(String cid) {
        boolean exists = storage.containsKey(cid);
        log.debug("模拟检查 IPFS 文件是否存在: {} -> {}", cid, exists);
        return exists;
    }

    /**
     * 删除模拟 IPFS 文件
     * 
     * @param cid IPFS CID
     * @throws IpfsException IPFS 操作异常
     */
    @Override
    public void deleteFile(String cid) throws IpfsException {
        log.debug("模拟删除 IPFS 文件: {}", cid);
        
        byte[] removed = storage.remove(cid);
        if (removed == null) {
            log.warn("尝试删除不存在的文件: {}", cid);
        } else {
            log.debug("模拟文件删除成功: {}", cid);
        }
    }

    /**
     * 获取模拟 IPFS 节点信息
     * 
     * @return 节点信息
     */
    @Override
    public String getNodeInfo() {
        return "Mock IPFS Node v1.0.0";
    }

    /**
     * 测试模拟 IPFS 连接
     * 
     * @return 连接是否正常
     */
    @Override
    public boolean testConnection() {
        log.debug("模拟 IPFS 连接测试");
        return true;
    }

    /**
     * 获取模拟 IPFS 网络统计信息
     * 
     * @return 网络统计信息
     */
    @Override
    public String getNetworkStats() {
        return String.format("Mock IPFS Stats - Files: %d, Total Size: %d bytes", 
                storage.size(), 
                storage.values().stream().mapToInt(content -> content.length).sum());
    }

    /**
     * 清空模拟存储（用于测试清理）
     */
    public void clearStorage() {
        storage.clear();
        log.debug("模拟 IPFS 存储已清空");
    }

    /**
     * 获取存储的文件数量（用于测试验证）
     */
    public int getStoredFileCount() {
        return storage.size();
    }

    /**
     * 生成模拟的 CID
     * 基于文件内容生成一个类似 IPFS CID 的字符串
     * 
     * @param content 文件内容
     * @return 模拟的 CID
     */
    private String generateMockCid(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            
            // 取前 32 字节，编码为 Base58 风格的字符串
            String base64Hash = Base64.getEncoder().encodeToString(hash);
            
            // 生成类似 IPFS CIDv0 的格式（以 Qm 开头）
            String mockCid = "Qm" + base64Hash.substring(0, 44).replace("+", "A").replace("/", "B").replace("=", "");
            
            // 确保长度为 46（标准 CIDv0 长度）
            if (mockCid.length() > 46) {
                mockCid = mockCid.substring(0, 46);
            } else if (mockCid.length() < 46) {
                // 使用 StringBuilder 替代 String.repeat() 以兼容 Java 8
                StringBuilder padding = new StringBuilder();
                int repeatCount = 46 - mockCid.length();
                for (int i = 0; i < repeatCount; i++) {
                    padding.append("A");
                }
                mockCid = mockCid + padding.toString();
            }
            
            return mockCid;
        } catch (Exception e) {
            // 如果哈希生成失败，使用简单的方法
            return "Qm" + String.format("%044d", Math.abs(content.hashCode()));
        }
    }
}