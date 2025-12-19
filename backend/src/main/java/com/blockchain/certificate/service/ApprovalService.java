package com.blockchain.certificate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blockchain.certificate.exception.BusinessException;
import com.blockchain.certificate.model.dto.ApprovalRequest;
import com.blockchain.certificate.model.dto.ApprovalResponse;
import com.blockchain.certificate.model.entity.Application;
import com.blockchain.certificate.model.entity.Approval;
import com.blockchain.certificate.model.entity.College;
import com.blockchain.certificate.model.entity.User;
import com.blockchain.certificate.model.vo.ApprovalHistoryVO;
import com.blockchain.certificate.repository.ApplicationRepository;
import com.blockchain.certificate.repository.ApprovalRepository;
import com.blockchain.certificate.repository.CollegeRepository;
import com.blockchain.certificate.repository.UserRepository;
import com.blockchain.certificate.util.CryptoUtil;
import com.blockchain.certificate.util.SignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 审批服务类
 * 提供证书申请的审批功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final SignatureUtil signatureUtil;
    private final CryptoUtil cryptoUtil;
    private final CertificateService certificateService;

    /**
     * 审批动作枚举
     */
    public enum ApprovalAction {
        APPROVE("APPROVE", "通过"),
        REJECT("REJECT", "驳回");

        private final String code;
        private final String desc;

        ApprovalAction(String code, String desc) {
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
            for (ApprovalAction action : values()) {
                if (action.code.equals(code)) {
                    return action.desc;
                }
            }
            return code;
        }
    }

    /**
     * 审批级别枚举
     */
    public enum ApprovalLevel {
        COLLEGE("COLLEGE", "学院审批"),
        SCHOOL("SCHOOL", "学校审批");

        private final String code;
        private final String desc;

        ApprovalLevel(String code, String desc) {
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
            for (ApprovalLevel level : values()) {
                if (level.code.equals(code)) {
                    return level.desc;
                }
            }
            return code;
        }
    }

    /**
     * 申请状态枚举
     */
    public enum ApplicationStatus {
        PENDING_COLLEGE("PENDING_COLLEGE", "待学院审批"),
        PENDING_SCHOOL("PENDING_SCHOOL", "待学校审批"),
        APPROVED("APPROVED", "已通过"),
        REJECTED("REJECTED", "已驳回"),
        CANCELLED("CANCELLED", "已取消");

        private final String code;
        private final String desc;

        ApplicationStatus(String code, String desc) {
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
            for (ApplicationStatus status : values()) {
                if (status.code.equals(code)) {
                    return status.desc;
                }
            }
            return code;
        }
    }

    /**
     * 审批申请
     * 
     * @param applicationId 申请ID
     * @param request 审批请求
     * @param approverId 审批人ID
     * @return 审批响应
     * @throws BusinessException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public ApprovalResponse approveApplication(String applicationId, ApprovalRequest request, String approverId) throws BusinessException {
        log.info("开始审批申请，申请ID: {}, 审批人ID: {}, 动作: {}", applicationId, approverId, request.getAction());

        // 验证申请是否存在
        Application application = applicationRepository.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        // 获取审批人信息
        User approver = userRepository.selectById(approverId);
        if (approver == null) {
            throw new BusinessException("审批人不存在");
        }

        // 验证审批权限和确定审批级别
        String approvalLevel = validateApprovalPermission(application, approver);

        // 验证申请状态
        validateApplicationStatus(application, approvalLevel);

        // 验证审批动作
        if (!ApprovalAction.APPROVE.getCode().equals(request.getAction()) && 
            !ApprovalAction.REJECT.getCode().equals(request.getAction())) {
            throw new BusinessException("无效的审批动作");
        }

        // 验证数字签名
        validateDigitalSignature(request, approver, applicationId);

        // 创建审批记录
        Approval approval = createApprovalRecord(applicationId, approverId, approvalLevel, request);

        // 更新申请状态
        String newStatus = updateApplicationStatus(application, approvalLevel, request.getAction());

        // 如果是终审通过，触发证书生成
        if (ApprovalLevel.SCHOOL.getCode().equals(approvalLevel) &&
            ApprovalAction.APPROVE.getCode().equals(request.getAction())) {
            try {
                certificateService.generateCertificate(applicationId);
                log.info("终审通过，证书生成成功，申请ID: {}", applicationId);
            } catch (Exception e) {
                log.error("证书生成失败，申请ID: {}", applicationId, e);
                // 证书生成失败不影响审批流程，记录日志即可
            }
        }

        log.info("审批完成，申请ID: {}, 新状态: {}", applicationId, newStatus);

        return buildApprovalResponse(approval, approver, newStatus);
    }

    /**
     * 查询待审批列表（权限过滤）
     * 
     * @param approverId 审批人ID
     * @param certificateType 证书类型（可选）
     * @param keyword 关键词搜索（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 分页待审批列表
     */
    public IPage<ApprovalHistoryVO> getPendingApprovalList(String approverId, String certificateType, 
                                                          String keyword, int page, int size) {
        log.info("查询待审批列表，审批人ID: {}, 证书类型: {}, 关键词: {}, 页码: {}, 大小: {}", 
                approverId, certificateType, keyword, page, size);

        // 获取审批人信息
        User approver = userRepository.selectById(approverId);
        if (approver == null) {
            throw new BusinessException("审批人不存在");
        }

        // 根据审批人角色确定查询条件
        LambdaQueryWrapper<Application> queryWrapper = new LambdaQueryWrapper<>();
        
        if ("COLLEGE_TEACHER".equals(approver.getRole())) {
            // 学院老师只能看到自己学院的待学院审批申请
            queryWrapper.eq(Application::getStatus, ApplicationStatus.PENDING_COLLEGE.getCode())
                       .eq(Application::getCollegeId, approver.getCollegeId());
        } else if ("SCHOOL_TEACHER".equals(approver.getRole())) {
            // 学校老师可以看到所有待学校审批的申请
            queryWrapper.eq(Application::getStatus, ApplicationStatus.PENDING_SCHOOL.getCode());
        } else {
            // 其他角色无审批权限
            return new Page<>(page, size, 0);
        }

        // 添加其他筛选条件
        if (StringUtils.isNotBlank(certificateType)) {
            queryWrapper.eq(Application::getCertificateType, certificateType);
        }
        
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like(Application::getTitle, keyword);
        }

        // 按创建时间正序排列（先申请的先审批）
        queryWrapper.orderByAsc(Application::getCreateTime);

        // 分页查询
        Page<Application> pageParam = new Page<>(page, size);
        IPage<Application> applicationPage = applicationRepository.selectPage(pageParam, queryWrapper);

        // 转换为VO
        return convertToPendingApprovalVO(applicationPage);
    }

    /**
     * 查询审批历史
     * 
     * @param approverId 审批人ID（可选，为空时查询所有）
     * @param applicationId 申请ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 分页审批历史
     */
    public IPage<ApprovalHistoryVO> getApprovalHistory(String approverId, String applicationId, 
                                                      LocalDateTime startDate, LocalDateTime endDate,
                                                      int page, int size) {
        log.info("查询审批历史，审批人ID: {}, 申请ID: {}, 开始日期: {}, 结束日期: {}, 页码: {}, 大小: {}", 
                approverId, applicationId, startDate, endDate, page, size);

        // 构建查询条件
        LambdaQueryWrapper<Approval> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.isNotBlank(approverId)) {
            queryWrapper.eq(Approval::getApproverId, approverId);
        }
        
        if (StringUtils.isNotBlank(applicationId)) {
            queryWrapper.eq(Approval::getApplicationId, applicationId);
        }
        
        if (startDate != null) {
            queryWrapper.ge(Approval::getApprovalTime, startDate);
        }
        
        if (endDate != null) {
            queryWrapper.le(Approval::getApprovalTime, endDate);
        }

        // 按审批时间倒序排列
        queryWrapper.orderByDesc(Approval::getApprovalTime);

        // 分页查询
        Page<Approval> pageParam = new Page<>(page, size);
        IPage<Approval> approvalPage = approvalRepository.selectPage(pageParam, queryWrapper);

        // 转换为VO
        return convertToApprovalHistoryVO(approvalPage);
    }

    /**
     * 验证审批权限并确定审批级别
     * 
     * @param application 申请
     * @param approver 审批人
     * @return 审批级别
     * @throws BusinessException 业务异常
     */
    private String validateApprovalPermission(Application application, User approver) throws BusinessException {
        String approverRole = approver.getRole();
        String applicationStatus = application.getStatus();

        if ("COLLEGE_TEACHER".equals(approverRole)) {
            // 学院老师只能审批待学院审批的申请，且必须是同一学院
            if (!ApplicationStatus.PENDING_COLLEGE.getCode().equals(applicationStatus)) {
                throw new BusinessException("该申请不在学院审批阶段");
            }
            
            if (!approver.getCollegeId().equals(application.getCollegeId())) {
                throw new BusinessException("只能审批本学院的申请");
            }
            
            return ApprovalLevel.COLLEGE.getCode();
            
        } else if ("SCHOOL_TEACHER".equals(approverRole)) {
            // 学校老师只能审批待学校审批的申请
            if (!ApplicationStatus.PENDING_SCHOOL.getCode().equals(applicationStatus)) {
                throw new BusinessException("该申请不在学校审批阶段");
            }
            
            return ApprovalLevel.SCHOOL.getCode();
            
        } else {
            throw new BusinessException("无审批权限");
        }
    }

    /**
     * 验证申请状态
     * 
     * @param application 申请
     * @param approvalLevel 审批级别
     * @throws BusinessException 业务异常
     */
    private void validateApplicationStatus(Application application, String approvalLevel) throws BusinessException {
        String status = application.getStatus();
        
        // 检查申请是否已经被审批过
        LambdaQueryWrapper<Approval> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Approval::getApplicationId, application.getId())
                   .eq(Approval::getApprovalLevel, approvalLevel);
        
        long count = approvalRepository.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("该申请已经被审批过");
        }

        // 验证状态是否允许审批
        if (ApplicationStatus.APPROVED.getCode().equals(status) ||
            ApplicationStatus.REJECTED.getCode().equals(status) ||
            ApplicationStatus.CANCELLED.getCode().equals(status)) {
            throw new BusinessException("该申请已经结束，无法再次审批");
        }
    }

    /**
     * 验证数字签名
     * 
     * @param request 审批请求
     * @param approver 审批人
     * @param applicationId 申请ID
     * @throws BusinessException 业务异常
     */
    private void validateDigitalSignature(ApprovalRequest request, User approver, String applicationId) throws BusinessException {
        try {
            // 构建待签名数据
            String approvalData = buildApprovalData(applicationId, request.getAction(), request.getComment(), approver.getId());
            
            // 解密审批人的私钥
            String decryptedPrivateKey = cryptoUtil.decrypt(approver.getPrivateKey());
            
            // 验证签名（这里我们重新生成签名来验证）
            String expectedSignature = signatureUtil.sign(approvalData, decryptedPrivateKey);
            
            if (!expectedSignature.equals(request.getSignature())) {
                throw new BusinessException("数字签名验证失败");
            }
            
            log.info("数字签名验证成功，审批人ID: {}", approver.getId());
            
        } catch (Exception e) {
            log.error("数字签名验证失败，审批人ID: {}", approver.getId(), e);
            throw new BusinessException("数字签名验证失败");
        }
    }

    /**
     * 构建审批数据（用于数字签名）
     * 
     * @param applicationId 申请ID
     * @param action 审批动作
     * @param comment 审批意见
     * @param approverId 审批人ID
     * @return 审批数据字符串
     */
    private String buildApprovalData(String applicationId, String action, String comment, String approverId) {
        return String.format("applicationId=%s&action=%s&comment=%s&approverId=%s&timestamp=%d",
                applicationId, action, comment != null ? comment : "", approverId, System.currentTimeMillis());
    }

    /**
     * 创建审批记录
     * 
     * @param applicationId 申请ID
     * @param approverId 审批人ID
     * @param approvalLevel 审批级别
     * @param request 审批请求
     * @return 审批记录
     */
    private Approval createApprovalRecord(String applicationId, String approverId, String approvalLevel, ApprovalRequest request) {
        // 计算签名哈希
        String signatureHash = calculateSignatureHash(request.getSignature());
        
        Approval approval = Approval.builder()
                .applicationId(applicationId)
                .approverId(approverId)
                .approvalLevel(approvalLevel)
                .action(request.getAction())
                .comment(request.getComment())
                .signatureHash(signatureHash)
                .build();

        approvalRepository.insert(approval);
        
        log.info("审批记录创建成功，审批ID: {}", approval.getId());
        
        return approval;
    }

    /**
     * 更新申请状态
     * 
     * @param application 申请
     * @param approvalLevel 审批级别
     * @param action 审批动作
     * @return 新状态
     */
    private String updateApplicationStatus(Application application, String approvalLevel, String action) {
        String newStatus;
        
        if (ApprovalAction.REJECT.getCode().equals(action)) {
            // 任何级别的驳回都直接设为已驳回
            newStatus = ApplicationStatus.REJECTED.getCode();
        } else if (ApprovalLevel.COLLEGE.getCode().equals(approvalLevel)) {
            // 学院审批通过，转为待学校审批
            newStatus = ApplicationStatus.PENDING_SCHOOL.getCode();
        } else {
            // 学校审批通过，设为已通过
            newStatus = ApplicationStatus.APPROVED.getCode();
        }

        application.setStatus(newStatus);
        applicationRepository.updateById(application);
        
        log.info("申请状态更新成功，申请ID: {}, 新状态: {}", application.getId(), newStatus);
        
        return newStatus;
    }

    /**
     * 计算签名哈希
     * 
     * @param signature 签名
     * @return 签名哈希
     */
    private String calculateSignatureHash(String signature) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(signature.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("计算签名哈希失败", e);
        }
    }

    /**
     * 构建审批响应对象
     * 
     * @param approval 审批记录
     * @param approver 审批人
     * @param newStatus 新状态
     * @return 审批响应
     */
    private ApprovalResponse buildApprovalResponse(Approval approval, User approver, String newStatus) {
        return ApprovalResponse.builder()
                .id(approval.getId())
                .applicationId(approval.getApplicationId())
                .approverId(approval.getApproverId())
                .approverName(approver.getName())
                .approvalLevel(approval.getApprovalLevel())
                .approvalLevelDesc(ApprovalLevel.getDescByCode(approval.getApprovalLevel()))
                .action(approval.getAction())
                .actionDesc(ApprovalAction.getDescByCode(approval.getAction()))
                .comment(approval.getComment())
                .signatureHash(approval.getSignatureHash())
                .approvalTime(approval.getApprovalTime())
                .newStatus(newStatus)
                .newStatusDesc(ApplicationStatus.getDescByCode(newStatus))
                .build();
    }

    /**
     * 转换待审批申请为VO
     * 
     * @param applicationPage 申请分页结果
     * @return VO分页结果
     */
    private IPage<ApprovalHistoryVO> convertToPendingApprovalVO(IPage<Application> applicationPage) {
        List<Application> applications = applicationPage.getRecords();
        if (applications.isEmpty()) {
            return new Page<>(applicationPage.getCurrent(), applicationPage.getSize(), applicationPage.getTotal());
        }

        // 获取申请人信息
        Set<String> applicantIds = applications.stream()
                .map(Application::getApplicantId)
                .collect(Collectors.toSet());
        
        Map<String, User> applicantMap = userRepository.selectBatchIds(applicantIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 转换为VO
        List<ApprovalHistoryVO> voList = applications.stream()
                .map(application -> {
                    User applicant = applicantMap.get(application.getApplicantId());
                    
                    return ApprovalHistoryVO.builder()
                            .applicationId(application.getId())
                            .applicationTitle(application.getTitle())
                            .applicantName(applicant != null ? applicant.getName() : "未知")
                            .studentNo(applicant != null ? applicant.getStudentNo() : "未知")
                            .certificateType(application.getCertificateType())
                            .currentStatus(application.getStatus())
                            .currentStatusDesc(ApplicationStatus.getDescByCode(application.getStatus()))
                            .build();
                })
                .collect(Collectors.toList());

        Page<ApprovalHistoryVO> voPage = new Page<>(applicationPage.getCurrent(), applicationPage.getSize(), applicationPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }

    /**
     * 转换审批记录为VO
     * 
     * @param approvalPage 审批分页结果
     * @return VO分页结果
     */
    private IPage<ApprovalHistoryVO> convertToApprovalHistoryVO(IPage<Approval> approvalPage) {
        List<Approval> approvals = approvalPage.getRecords();
        if (approvals.isEmpty()) {
            return new Page<>(approvalPage.getCurrent(), approvalPage.getSize(), approvalPage.getTotal());
        }

        // 获取申请信息
        Set<String> applicationIds = approvals.stream()
                .map(Approval::getApplicationId)
                .collect(Collectors.toSet());
        
        Map<String, Application> applicationMap = applicationRepository.selectBatchIds(applicationIds)
                .stream()
                .collect(Collectors.toMap(Application::getId, app -> app));

        // 获取审批人信息
        Set<String> approverIds = approvals.stream()
                .map(Approval::getApproverId)
                .collect(Collectors.toSet());
        
        Map<String, User> approverMap = userRepository.selectBatchIds(approverIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 获取申请人信息
        Set<String> applicantIds = applicationMap.values().stream()
                .map(Application::getApplicantId)
                .collect(Collectors.toSet());
        
        Map<String, User> applicantMap = userRepository.selectBatchIds(applicantIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 转换为VO
        List<ApprovalHistoryVO> voList = approvals.stream()
                .map(approval -> {
                    Application application = applicationMap.get(approval.getApplicationId());
                    User approver = approverMap.get(approval.getApproverId());
                    User applicant = application != null ? applicantMap.get(application.getApplicantId()) : null;
                    
                    return ApprovalHistoryVO.builder()
                            .id(approval.getId())
                            .applicationId(approval.getApplicationId())
                            .applicationTitle(application != null ? application.getTitle() : "未知")
                            .applicantName(applicant != null ? applicant.getName() : "未知")
                            .studentNo(applicant != null ? applicant.getStudentNo() : "未知")
                            .certificateType(application != null ? application.getCertificateType() : "未知")
                            .approverId(approval.getApproverId())
                            .approverName(approver != null ? approver.getName() : "未知")
                            .approvalLevel(approval.getApprovalLevel())
                            .approvalLevelDesc(ApprovalLevel.getDescByCode(approval.getApprovalLevel()))
                            .action(approval.getAction())
                            .actionDesc(ApprovalAction.getDescByCode(approval.getAction()))
                            .comment(approval.getComment())
                            .approvalTime(approval.getApprovalTime())
                            .currentStatus(application != null ? application.getStatus() : "未知")
                            .currentStatusDesc(application != null ? ApplicationStatus.getDescByCode(application.getStatus()) : "未知")
                            .build();
                })
                .collect(Collectors.toList());

        Page<ApprovalHistoryVO> voPage = new Page<>(approvalPage.getCurrent(), approvalPage.getSize(), approvalPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }
}