package com.blockchain.certificate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blockchain.certificate.exception.BusinessException;
import com.blockchain.certificate.model.entity.Application;
import com.blockchain.certificate.model.entity.Certificate;
import com.blockchain.certificate.model.entity.CertificateTemplate;
import com.blockchain.certificate.model.entity.User;
import com.blockchain.certificate.repository.ApplicationRepository;
import com.blockchain.certificate.repository.CertificateRepository;
import com.blockchain.certificate.repository.CertificateTemplateRepository;
import com.blockchain.certificate.repository.UserRepository;
import com.blockchain.certificate.util.CertificateNumberGenerator;
import com.blockchain.certificate.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 证书服务类
 * 提供证书生成、查询、撤销、下载等功能
 */
@Service
@Slf4j
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CertificateTemplateRepository certificateTemplateRepository;
    private final CertificateNumberGenerator certificateNumberGenerator;
    private final PdfGenerator pdfGenerator;
    private final IpfsService ipfsService;
    
    // 区块链服务是可选的，如果未启用则为null
    private final BlockchainService blockchainService;
    
    public CertificateService(
            CertificateRepository certificateRepository,
            ApplicationRepository applicationRepository,
            UserRepository userRepository,
            CertificateTemplateRepository certificateTemplateRepository,
            CertificateNumberGenerator certificateNumberGenerator,
            PdfGenerator pdfGenerator,
            IpfsService ipfsService,
            @org.springframework.beans.factory.annotation.Autowired(required = false) BlockchainService blockchainService) {
        this.certificateRepository = certificateRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.certificateTemplateRepository = certificateTemplateRepository;
        this.certificateNumberGenerator = certificateNumberGenerator;
        this.pdfGenerator = pdfGenerator;
        this.ipfsService = ipfsService;
        this.blockchainService = blockchainService;
    }

    /**
     * 证书状态枚举
     */
    public enum CertificateStatus {
        VALID("VALID", "有效"),
        REVOKED("REVOKED", "已撤销"),
        EXPIRED("EXPIRED", "已过期");

        private final String code;
        private final String desc;

        CertificateStatus(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static String getDescByCode(String code) {
            for (CertificateStatus status : values()) {
                if (status.code.equals(code)) {
                    return status.desc;
                }
            }
            return code;
        }
    }

    /**
     * 生成证书（终审通过后调用）
     * 
     * @param applicationId 申请ID
     * @return 证书信息
     * @throws BusinessException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public Certificate generateCertificate(String applicationId) throws BusinessException {
        log.info("开始生成证书，申请ID: {}", applicationId);

        // 获取申请信息
        Application application = applicationRepository.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        // 验证申请状态
        if (!"APPROVED".equals(application.getStatus())) {
            throw new BusinessException("只有已通过的申请才能生成证书");
        }

        // 检查是否已生成证书
        LambdaQueryWrapper<Certificate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Certificate::getApplicationId, applicationId);
        Certificate existingCertificate = certificateRepository.selectOne(queryWrapper);
        if (existingCertificate != null) {
            log.warn("证书已存在，申请ID: {}, 证书编号: {}", applicationId, existingCertificate.getCertificateNo());
            return existingCertificate;
        }

        // 获取持有人信息
        User holder = userRepository.selectById(application.getApplicantId());
        if (holder == null) {
            throw new BusinessException("证书持有人不存在");
        }

        // 生成唯一证书编号
        String certificateNo = certificateNumberGenerator.generate();
        log.info("生成证书编号: {}", certificateNo);

        // 获取证书模板
        CertificateTemplate template = getDefaultTemplate();

        // 准备证书数据
        Map<String, String> certificateData = prepareCertificateData(application, holder, certificateNo);

        // 转换模板为PdfGenerator需要的格式
        PdfGenerator.CertificateTemplate pdfTemplate = new PdfGenerator.CertificateTemplate();
        // 如果模板有背景图片路径，可以设置
        // pdfTemplate.setBackgroundImagePath(template.getBackgroundImagePath());
        
        // 生成 PDF
        byte[] pdfContent = pdfGenerator.generateCertificatePdf(pdfTemplate, certificateData);
        log.info("证书 PDF 生成成功，大小: {} bytes", pdfContent.length);

        // 计算文件哈希
        String fileHash = calculateFileHash(pdfContent);
        log.info("证书文件哈希: {}", fileHash);

        // 上传到 IPFS
        String ipfsCid = ipfsService.uploadFile(certificateNo + ".pdf", pdfContent);
        log.info("证书上传到 IPFS 成功，CID: {}", ipfsCid);

        // 创建证书记录
        Certificate certificate = Certificate.builder()
                .certificateNo(certificateNo)
                .applicationId(applicationId)
                .holderId(holder.getId())
                .title(application.getTitle())
                .certificateType(application.getCertificateType())
                .status(CertificateStatus.VALID.getCode())
                .ipfsCid(ipfsCid)
                .fileHash(fileHash)
                .issueDate(LocalDate.now())
                .build();

        try {
            // 保存证书记录
            certificateRepository.insert(certificate);
            log.info("证书记录保存成功，证书ID: {}", certificate.getId());

            // 区块链存证（如果区块链服务可用）
            if (blockchainService != null) {
                try {
                    BlockchainService.BlockchainStorageResult blockchainResult =
                        blockchainService.storeCertificate(certificateNo, fileHash);
                    
                    // 更新区块链信息
                    certificate.setBlockchainTxHash(blockchainResult.getTransactionHash());
                    certificate.setBlockHeight(blockchainResult.getBlockNumber());
                    certificateRepository.updateById(certificate);
                    
                    log.info("证书区块链存证成功，交易哈希: {}", blockchainResult.getTransactionHash());
                } catch (Exception e) {
                    log.error("证书区块链存证失败，证书编号: {}", certificateNo, e);
                    // 区块链存证失败不影响证书生成，记录日志即可
                }
            } else {
                log.warn("区块链服务未启用，跳过区块链存证");
            }

            log.info("证书生成完成，证书编号: {}", certificateNo);
            return certificate;

        } catch (Exception e) {
            // 如果保存失败，清理 IPFS 文件
            try {
                ipfsService.deleteFile(ipfsCid);
            } catch (Exception cleanupException) {
                log.warn("清理 IPFS 文件失败，CID: {}", ipfsCid, cleanupException);
            }
            log.error("证书生成失败，申请ID: {}", applicationId, e);
            throw new BusinessException("证书生成失败");
        }
    }

    /**
     * 查询证书列表
     * 
     * @param holderId 持有人ID（可选）
     * @param certificateNo 证书编号（可选）
     * @param certificateType 证书类型（可选）
     * @param status 证书状态（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 分页证书列表
     */
    public IPage<Certificate> getCertificateList(String holderId, String certificateNo, 
                                                 String certificateType, String status,
                                                 LocalDate startDate, LocalDate endDate,
                                                 int page, int size) {
        log.info("查询证书列表，持有人ID: {}, 证书编号: {}, 类型: {}, 状态: {}, 页码: {}, 大小: {}", 
                holderId, certificateNo, certificateType, status, page, size);

        // 构建查询条件
        LambdaQueryWrapper<Certificate> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.isNotBlank(holderId)) {
            queryWrapper.eq(Certificate::getHolderId, holderId);
        }
        
        if (StringUtils.isNotBlank(certificateNo)) {
            queryWrapper.eq(Certificate::getCertificateNo, certificateNo);
        }
        
        if (StringUtils.isNotBlank(certificateType)) {
            queryWrapper.eq(Certificate::getCertificateType, certificateType);
        }
        
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq(Certificate::getStatus, status);
        }
        
        if (startDate != null) {
            queryWrapper.ge(Certificate::getIssueDate, startDate);
        }
        
        if (endDate != null) {
            queryWrapper.le(Certificate::getIssueDate, endDate);
        }

        // 按颁发日期倒序排列
        queryWrapper.orderByDesc(Certificate::getIssueDate);

        // 分页查询
        Page<Certificate> pageParam = new Page<>(page, size);
        return certificateRepository.selectPage(pageParam, queryWrapper);
    }

    /**
     * 查询证书详情
     * 
     * @param certificateId 证书ID
     * @return 证书详情
     * @throws BusinessException 业务异常
     */
    public Certificate getCertificateDetail(String certificateId) throws BusinessException {
        log.info("查询证书详情，证书ID: {}", certificateId);

        Certificate certificate = certificateRepository.selectById(certificateId);
        if (certificate == null) {
            throw new BusinessException("证书不存在");
        }

        return certificate;
    }

    /**
     * 根据证书编号查询证书
     * 
     * @param certificateNo 证书编号
     * @return 证书信息
     * @throws BusinessException 业务异常
     */
    public Certificate getCertificateByCertificateNo(String certificateNo) throws BusinessException {
        log.info("根据证书编号查询证书: {}", certificateNo);

        if (StringUtils.isBlank(certificateNo)) {
            throw new BusinessException("证书编号不能为空");
        }

        LambdaQueryWrapper<Certificate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Certificate::getCertificateNo, certificateNo);
        
        Certificate certificate = certificateRepository.selectOne(queryWrapper);
        if (certificate == null) {
            throw new BusinessException("证书不存在");
        }

        return certificate;
    }

    /**
     * 下载证书
     * 
     * @param certificateId 证书ID
     * @return 证书 PDF 内容
     * @throws BusinessException 业务异常
     */
    public byte[] downloadCertificate(String certificateId) throws BusinessException {
        log.info("下载证书，证书ID: {}", certificateId);

        Certificate certificate = certificateRepository.selectById(certificateId);
        if (certificate == null) {
            throw new BusinessException("证书不存在");
        }

        // 从 IPFS 下载证书
        byte[] pdfContent = ipfsService.downloadFile(certificate.getIpfsCid());
        
        // 验证文件完整性
        String downloadedHash = calculateFileHash(pdfContent);
        if (!downloadedHash.equals(certificate.getFileHash())) {
            log.error("证书文件哈希不匹配，证书编号: {}, 期望: {}, 实际: {}", 
                    certificate.getCertificateNo(), certificate.getFileHash(), downloadedHash);
            throw new BusinessException("证书文件完整性验证失败");
        }

        log.info("证书下载成功，证书编号: {}, 大小: {} bytes", certificate.getCertificateNo(), pdfContent.length);
        return pdfContent;
    }

    /**
     * 撤销证书
     * 
     * @param certificateId 证书ID
     * @param reason 撤销原因
     * @throws BusinessException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void revokeCertificate(String certificateId, String reason) throws BusinessException {
        log.info("开始撤销证书，证书ID: {}, 原因: {}", certificateId, reason);

        Certificate certificate = certificateRepository.selectById(certificateId);
        if (certificate == null) {
            throw new BusinessException("证书不存在");
        }

        // 验证证书状态
        if (CertificateStatus.REVOKED.getCode().equals(certificate.getStatus())) {
            throw new BusinessException("证书已被撤销");
        }

        // 更新证书状态
        certificate.setStatus(CertificateStatus.REVOKED.getCode());
        certificate.setUpdateTime(LocalDateTime.now());
        certificateRepository.updateById(certificate);

        // 区块链撤销（如果区块链服务可用）
        if (blockchainService != null) {
            try {
                blockchainService.revokeCertificate(certificate.getCertificateNo());
                log.info("证书区块链撤销成功，证书编号: {}", certificate.getCertificateNo());
            } catch (Exception e) {
                log.error("证书区块链撤销失败，证书编号: {}", certificate.getCertificateNo(), e);
                // 区块链撤销失败不影响数据库状态更新
            }
        } else {
            log.warn("区块链服务未启用，跳过区块链撤销");
        }

        log.info("证书撤销成功，证书编号: {}", certificate.getCertificateNo());
    }

    /**
     * 获取默认证书模板
     * 
     * @return 证书模板
     * @throws BusinessException 业务异常
     */
    private CertificateTemplate getDefaultTemplate() throws BusinessException {
        LambdaQueryWrapper<CertificateTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CertificateTemplate::getIsDefault, true);
        
        CertificateTemplate template = certificateTemplateRepository.selectOne(queryWrapper);
        if (template == null) {
            throw new BusinessException("未找到默认证书模板");
        }
        
        return template;
    }

    /**
     * 准备证书数据
     * 
     * @param application 申请信息
     * @param holder 持有人信息
     * @param certificateNo 证书编号
     * @return 证书数据
     */
    private Map<String, String> prepareCertificateData(Application application, User holder, String certificateNo) {
        Map<String, String> data = new HashMap<>();
        
        // 证书基本信息
        data.put("certificateNo", certificateNo);
        data.put("title", application.getTitle());
        data.put("certificateType", application.getCertificateType());
        data.put("issueDate", LocalDate.now().toString());
        
        // 持有人信息
        data.put("holderName", holder.getName());
        data.put("studentNo", holder.getStudentNo());
        
        // 其他信息
        data.put("issuer", "区块链证书存证系统");
        
        return data;
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
     * 统计证书数量
     * 
     * @param holderId 持有人ID（可选）
     * @param status 证书状态（可选）
     * @return 证书数量
     */
    public long countCertificates(String holderId, String status) {
        LambdaQueryWrapper<Certificate> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.isNotBlank(holderId)) {
            queryWrapper.eq(Certificate::getHolderId, holderId);
        }
        
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq(Certificate::getStatus, status);
        }
        
        return certificateRepository.selectCount(queryWrapper);
    }
}