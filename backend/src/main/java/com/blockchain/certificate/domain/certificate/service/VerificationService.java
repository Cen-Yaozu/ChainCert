package com.blockchain.certificate.domain.certificate.service;



import com.blockchain.certificate.infrastructure.ipfs.IpfsService;
import com.blockchain.certificate.infrastructure.blockchain.BlockchainService;
import com.blockchain.certificate.shared.exception.BusinessException;
import com.blockchain.certificate.domain.certificate.model.Certificate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;

/**
 * 证书核验服务类
 * 提供证书三级验证功能（数据库 + 区块链 + IPFS）
 */
@Service
@Slf4j
public class VerificationService {

    private final CertificateService certificateService;
    private final IpfsService ipfsService;
    
    // 区块链服务是可选的，如果未启用则为null
    private final BlockchainService blockchainService;
    
    public VerificationService(
            CertificateService certificateService,
            IpfsService ipfsService,
            @org.springframework.beans.factory.annotation.Autowired(required = false) BlockchainService blockchainService) {
        this.certificateService = certificateService;
        this.ipfsService = ipfsService;
        this.blockchainService = blockchainService;
    }

    /**
     * 验证证书
     * 三级验证：数据库查询 + 区块链验证 + IPFS 完整性检查
     * 
     * @param certificateNo 证书编号
     * @return 验证结果
     */
    public VerificationResult verifyCertificate(String certificateNo) {
        log.info("开始验证证书，证书编号: {}", certificateNo);

        VerificationResult result = new VerificationResult();
        result.setCertificateNo(certificateNo);
        result.setVerificationTime(System.currentTimeMillis());

        try {
            // 第一级：数据库查询
            Certificate certificate = performDatabaseCheck(certificateNo, result);
            if (certificate == null) {
                result.setValid(false);
                result.setMessage("证书不存在");
                return result;
            }

            // 检查证书状态
            if ("REVOKED".equals(certificate.getStatus())) {
                result.setValid(false);
                result.setMessage("证书已被撤销");
                result.setCertificate(buildCertificateInfo(certificate));
                return result;
            }

            // 第二级：区块链验证
            boolean blockchainValid = performBlockchainCheck(certificate, result);
            if (!blockchainValid) {
                result.setValid(false);
                result.setMessage("区块链验证失败");
                result.setCertificate(buildCertificateInfo(certificate));
                return result;
            }

            // 第三级：IPFS 完整性验证
            boolean ipfsValid = performIpfsCheck(certificate, result);
            if (!ipfsValid) {
                result.setValid(false);
                result.setMessage("IPFS 文件完整性验证失败");
                result.setCertificate(buildCertificateInfo(certificate));
                return result;
            }

            // 所有验证通过
            result.setValid(true);
            result.setMessage("证书验证成功");
            result.setCertificate(buildCertificateInfo(certificate));
            result.setDownloadUrl("/api/verification/download/" + certificate.getCertificateNo());

            log.info("证书验证成功，证书编号: {}", certificateNo);
            return result;

        } catch (Exception e) {
            log.error("证书验证异常，证书编号: {}", certificateNo, e);
            result.setValid(false);
            result.setMessage("证书验证失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 第一级验证：数据库查询
     * 
     * @param certificateNo 证书编号
     * @param result 验证结果
     * @return 证书信息
     */
    private Certificate performDatabaseCheck(String certificateNo, VerificationResult result) {
        try {
            Certificate certificate = certificateService.getCertificateByCertificateNo(certificateNo);
            result.setDatabaseCheck(true);
            log.info("数据库验证通过，证书编号: {}", certificateNo);
            return certificate;
        } catch (BusinessException e) {
            result.setDatabaseCheck(false);
            log.warn("数据库验证失败，证书编号: {}", certificateNo);
            return null;
        }
    }

    /**
     * 第二级验证：区块链验证
     * 
     * @param certificate 证书信息
     * @param result 验证结果
     * @return 是否验证通过
     */
    private boolean performBlockchainCheck(Certificate certificate, VerificationResult result) {
        try {
            // 如果区块链服务未启用，跳过区块链验证
            if (blockchainService == null) {
                log.warn("区块链服务未启用，跳过区块链验证，证书编号: {}", certificate.getCertificateNo());
                result.setBlockchainCheck(true);
                return true;
            }
            
            // 如果证书没有区块链交易哈希，跳过区块链验证
            if (StringUtils.isBlank(certificate.getBlockchainTxHash())) {
                log.warn("证书没有区块链交易哈希，跳过区块链验证，证书编号: {}", certificate.getCertificateNo());
                result.setBlockchainCheck(true);
                return true;
            }

            // 调用区块链服务验证证书
            BlockchainService.CertificateVerificationResult blockchainResult =
                blockchainService.verifyCertificate(certificate.getCertificateNo(), certificate.getFileHash());

            result.setBlockchainCheck(blockchainResult.isValid());
            result.setTransactionHash(certificate.getBlockchainTxHash());
            result.setBlockHeight(certificate.getBlockHeight());
            result.setBlockchainTimestamp(blockchainResult.getTimestamp());

            if (blockchainResult.isValid()) {
                log.info("区块链验证通过，证书编号: {}", certificate.getCertificateNo());
                return true;
            } else {
                log.warn("区块链验证失败，证书编号: {}", certificate.getCertificateNo());
                return false;
            }

        } catch (Exception e) {
            log.error("区块链验证异常，证书编号: {}", certificate.getCertificateNo(), e);
            result.setBlockchainCheck(false);
            return false;
        }
    }

    /**
     * 第三级验证：IPFS 完整性验证
     * 
     * @param certificate 证书信息
     * @param result 验证结果
     * @return 是否验证通过
     */
    private boolean performIpfsCheck(Certificate certificate, VerificationResult result) {
        try {
            // 从 IPFS 下载证书文件
            byte[] pdfContent = ipfsService.downloadFile(certificate.getIpfsCid());

            // 计算文件哈希
            String downloadedHash = calculateFileHash(pdfContent);

            // 验证哈希是否一致
            boolean hashMatch = downloadedHash.equals(certificate.getFileHash());

            result.setIpfsCheck(hashMatch);
            result.setIpfsCid(certificate.getIpfsCid());

            if (hashMatch) {
                log.info("IPFS 完整性验证通过，证书编号: {}", certificate.getCertificateNo());
                return true;
            } else {
                log.warn("IPFS 完整性验证失败，证书编号: {}, 期望哈希: {}, 实际哈希: {}", 
                        certificate.getCertificateNo(), certificate.getFileHash(), downloadedHash);
                return false;
            }

        } catch (Exception e) {
            log.error("IPFS 完整性验证异常，证书编号: {}", certificate.getCertificateNo(), e);
            result.setIpfsCheck(false);
            return false;
        }
    }

    /**
     * 下载证书（公开接口，用于核验后下载）
     * 
     * @param certificateNo 证书编号
     * @return 证书 PDF 内容
     * @throws BusinessException 业务异常
     */
    public byte[] downloadCertificateByNo(String certificateNo) throws BusinessException {
        log.info("下载证书，证书编号: {}", certificateNo);

        if (StringUtils.isBlank(certificateNo)) {
            throw new BusinessException("证书编号不能为空");
        }

        try {
            Certificate certificate = certificateService.getCertificateByCertificateNo(certificateNo);
            
            // 检查证书状态
            if ("REVOKED".equals(certificate.getStatus())) {
                throw new BusinessException("证书已被撤销，无法下载");
            }

            // 从 IPFS 下载证书
            byte[] pdfContent = ipfsService.downloadFile(certificate.getIpfsCid());
            
            // 验证文件完整性
            String downloadedHash = calculateFileHash(pdfContent);
            if (!downloadedHash.equals(certificate.getFileHash())) {
                log.error("证书文件哈希不匹配，证书编号: {}", certificateNo);
                throw new BusinessException("证书文件完整性验证失败");
            }

            log.info("证书下载成功，证书编号: {}", certificateNo);
            return pdfContent;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("下载证书失败，证书编号: {}", certificateNo, e);
            throw new BusinessException("下载证书失败");
        }
    }

    /**
     * 计算文件哈希
     * 
     * @param content 文件内容
     * @return SHA-256 哈希值
     */
    private String calculateFileHash(byte[] content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(content);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("计算文件哈希失败", e);
        }
    }

    /**
     * 构建证书信息
     * 
     * @param certificate 证书实体
     * @return 证书信息
     */
    private CertificateInfo buildCertificateInfo(Certificate certificate) {
        CertificateInfo info = new CertificateInfo();
        info.setCertificateNo(certificate.getCertificateNo());
        info.setTitle(certificate.getTitle());
        info.setCertificateType(certificate.getCertificateType());
        info.setStatus(certificate.getStatus());
        info.setIssueDate(certificate.getIssueDate().toString());
        return info;
    }

    /**
     * 验证结果类
     */
    public static class VerificationResult {
        private boolean valid;
        private String certificateNo;
        private String message;
        private CertificateInfo certificate;
        private boolean databaseCheck;
        private boolean blockchainCheck;
        private boolean ipfsCheck;
        private String transactionHash;
        private Long blockHeight;
        private Long blockchainTimestamp;
        private String ipfsCid;
        private String downloadUrl;
        private long verificationTime;

        // Getters and Setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public String getCertificateNo() { return certificateNo; }
        public void setCertificateNo(String certificateNo) { this.certificateNo = certificateNo; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public CertificateInfo getCertificate() { return certificate; }
        public void setCertificate(CertificateInfo certificate) { this.certificate = certificate; }

        public boolean isDatabaseCheck() { return databaseCheck; }
        public void setDatabaseCheck(boolean databaseCheck) { this.databaseCheck = databaseCheck; }

        public boolean isBlockchainCheck() { return blockchainCheck; }
        public void setBlockchainCheck(boolean blockchainCheck) { this.blockchainCheck = blockchainCheck; }

        public boolean isIpfsCheck() { return ipfsCheck; }
        public void setIpfsCheck(boolean ipfsCheck) { this.ipfsCheck = ipfsCheck; }

        public String getTransactionHash() { return transactionHash; }
        public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }

        public Long getBlockHeight() { return blockHeight; }
        public void setBlockHeight(Long blockHeight) { this.blockHeight = blockHeight; }

        public Long getBlockchainTimestamp() { return blockchainTimestamp; }
        public void setBlockchainTimestamp(Long blockchainTimestamp) { this.blockchainTimestamp = blockchainTimestamp; }

        public String getIpfsCid() { return ipfsCid; }
        public void setIpfsCid(String ipfsCid) { this.ipfsCid = ipfsCid; }

        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

        public long getVerificationTime() { return verificationTime; }
        public void setVerificationTime(long verificationTime) { this.verificationTime = verificationTime; }
    }

    /**
     * 证书信息类
     */
    public static class CertificateInfo {
        private String certificateNo;
        private String title;
        private String certificateType;
        private String status;
        private String issueDate;

        // Getters and Setters
        public String getCertificateNo() { return certificateNo; }
        public void setCertificateNo(String certificateNo) { this.certificateNo = certificateNo; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getCertificateType() { return certificateType; }
        public void setCertificateType(String certificateType) { this.certificateType = certificateType; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getIssueDate() { return issueDate; }
        public void setIssueDate(String issueDate) { this.issueDate = issueDate; }
    }
}