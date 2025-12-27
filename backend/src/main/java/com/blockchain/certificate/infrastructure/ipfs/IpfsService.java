package com.blockchain.certificate.infrastructure.ipfs;

import com.blockchain.certificate.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * IPFS 服务类
 * 提供文件上传、下载、验证等业务功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IpfsService {

    private final IpfsClient ipfsClient;

    /**
     * 支持的文件格式
     */
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("pdf", "jpg", "jpeg", "png");

    /**
     * 最大文件大小（10MB）
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 上传文件到 IPFS
     * 
     * @param file 上传的文件
     * @return IPFS CID
     * @throws BusinessException 业务异常
     */
    public String uploadFile(MultipartFile file) throws BusinessException {
        // 验证文件
        validateFile(file);

        try {
            String fileName = file.getOriginalFilename();
            byte[] content = file.getBytes();

            log.info("开始上传文件到 IPFS: {}, 大小: {} bytes", fileName, content.length);

            String cid = ipfsClient.uploadFile(fileName, content);

            log.info("文件上传到 IPFS 成功: {} -> CID: {}", fileName, cid);

            return cid;
        } catch (IOException e) {
            log.error("读取文件内容失败: {}", file.getOriginalFilename(), e);
            throw new BusinessException("读取文件内容失败");
        } catch (IpfsException e) {
            log.error("上传文件到 IPFS 失败: {}", file.getOriginalFilename(), e);
            throw new BusinessException("上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 上传字节数组到 IPFS
     * 
     * @param fileName 文件名
     * @param content 文件内容
     * @return IPFS CID
     * @throws BusinessException 业务异常
     */
    public String uploadFile(String fileName, byte[] content) throws BusinessException {
        // 验证文件名和内容
        if (StringUtils.isBlank(fileName)) {
            throw new BusinessException("文件名不能为空");
        }

        if (content == null || content.length == 0) {
            throw new BusinessException("文件内容不能为空");
        }

        // 验证文件格式
        validateFileFormat(fileName);

        // 验证文件大小
        if (content.length > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小超过限制，最大支持 10MB");
        }

        try {
            log.info("开始上传文件到 IPFS: {}, 大小: {} bytes", fileName, content.length);

            String cid = ipfsClient.uploadFile(fileName, content);

            log.info("文件上传到 IPFS 成功: {} -> CID: {}", fileName, cid);

            return cid;
        } catch (IpfsException e) {
            log.error("上传文件到 IPFS 失败: {}", fileName, e);
            throw new BusinessException("上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 从 IPFS 下载文件
     * 
     * @param cid IPFS CID
     * @return 文件内容
     * @throws BusinessException 业务异常
     */
    public byte[] downloadFile(String cid) throws BusinessException {
        if (StringUtils.isBlank(cid)) {
            throw new BusinessException("CID 不能为空");
        }

        try {
            log.info("开始从 IPFS 下载文件: {}", cid);

            byte[] content = ipfsClient.downloadFile(cid);

            log.info("从 IPFS 下载文件成功: {}, 大小: {} bytes", cid, content.length);

            return content;
        } catch (IpfsException e) {
            log.error("从 IPFS 下载文件失败: {}", cid, e);
            throw new BusinessException("下载文件失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     * 
     * @param cid IPFS CID
     * @return 文件是否存在
     */
    public boolean fileExists(String cid) {
        if (StringUtils.isBlank(cid)) {
            return false;
        }

        try {
            return ipfsClient.fileExists(cid);
        } catch (Exception e) {
            log.error("检查 IPFS 文件是否存在失败: {}", cid, e);
            return false;
        }
    }

    /**
     * 删除文件
     * 
     * @param cid IPFS CID
     * @throws BusinessException 业务异常
     */
    public void deleteFile(String cid) throws BusinessException {
        if (StringUtils.isBlank(cid)) {
            throw new BusinessException("CID 不能为空");
        }

        try {
            log.info("开始删除 IPFS 文件: {}", cid);

            ipfsClient.deleteFile(cid);

            log.info("删除 IPFS 文件成功: {}", cid);
        } catch (IpfsException e) {
            log.error("删除 IPFS 文件失败: {}", cid, e);
            throw new BusinessException("删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 测试 IPFS 连接
     * 
     * @return 连接是否正常
     */
    public boolean testConnection() {
        return ipfsClient.testConnection();
    }

    /**
     * 获取 IPFS 节点信息
     * 
     * @return 节点信息
     * @throws BusinessException 业务异常
     */
    public String getNodeInfo() throws BusinessException {
        try {
            return ipfsClient.getNodeInfo();
        } catch (IpfsException e) {
            log.error("获取 IPFS 节点信息失败", e);
            throw new BusinessException("获取节点信息失败: " + e.getMessage());
        }
    }

    /**
     * 验证上传的文件
     * 
     * @param file 上传的文件
     * @throws BusinessException 验证失败时抛出异常
     */
    private void validateFile(MultipartFile file) throws BusinessException {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 检查文件名
        String fileName = file.getOriginalFilename();
        if (StringUtils.isBlank(fileName)) {
            throw new BusinessException("文件名不能为空");
        }

        // 验证文件格式
        validateFileFormat(fileName);

        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小超过限制，最大支持 10MB");
        }

        log.debug("文件验证通过: {}, 大小: {} bytes", fileName, file.getSize());
    }

    /**
     * 验证文件格式
     * 
     * @param fileName 文件名
     * @throws BusinessException 格式不支持时抛出异常
     */
    private void validateFileFormat(String fileName) throws BusinessException {
        if (StringUtils.isBlank(fileName)) {
            throw new BusinessException("文件名不能为空");
        }

        // 获取文件扩展名
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            throw new BusinessException("文件必须有扩展名");
        }

        String extension = fileName.substring(lastDotIndex + 1).toLowerCase();

        // 检查是否为支持的格式
        if (!SUPPORTED_FORMATS.contains(extension)) {
            throw new BusinessException("不支持的文件格式，仅支持: " + String.join(", ", SUPPORTED_FORMATS));
        }

        log.debug("文件格式验证通过: {}", extension);
    }

    /**
     * 验证 CID 格式
     * 
     * @param cid IPFS CID
     * @return 是否为有效的 CID 格式
     */
    private boolean isValidCid(String cid) {
        if (StringUtils.isBlank(cid)) {
            return false;
        }
        
        // 基本的 CID 格式验证（以 Qm 开头，长度合理）
        return cid.startsWith("Qm") && cid.length() >= 46 && cid.length() <= 59;
    }

    /**
     * 获取文件大小的人类可读格式
     * 
     * @param bytes 字节数
     * @return 格式化的大小字符串
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
}
