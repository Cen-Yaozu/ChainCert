package com.blockchain.certificate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blockchain.certificate.shared.exception.BusinessException;
import com.blockchain.certificate.model.dto.ApplicationRequest;
import com.blockchain.certificate.model.dto.ApplicationResponse;
import com.blockchain.certificate.domain.application.model.Application;
import com.blockchain.certificate.domain.organization.model.College;
import com.blockchain.certificate.domain.user.model.User;
import com.blockchain.certificate.model.vo.ApplicationListVO;
import com.blockchain.certificate.domain.application.repository.ApplicationRepository;
import com.blockchain.certificate.domain.organization.repository.CollegeRepository;
import com.blockchain.certificate.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 申请服务类
 * 提供证书申请的创建、查询、取消等功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final IpfsService ipfsService;

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
     * 创建申请
     * 
     * @param request 申请请求
     * @param applicantId 申请人ID
     * @return 申请响应
     * @throws BusinessException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public ApplicationResponse createApplication(ApplicationRequest request, String applicantId) throws BusinessException {
        log.info("开始创建申请，申请人ID: {}, 标题: {}", applicantId, request.getTitle());

        // 获取申请人信息
        User applicant = userRepository.selectById(applicantId);
        if (applicant == null) {
            throw new BusinessException("申请人不存在");
        }

        // 验证申请人是否为学生
        if (!"STUDENT".equals(applicant.getRole())) {
            throw new BusinessException("只有学生可以提交申请");
        }

        // 验证申请人是否有学院信息
        if (StringUtils.isBlank(applicant.getCollegeId())) {
            throw new BusinessException("申请人未分配学院，请联系管理员");
        }

        // 验证学院是否存在
        College college = collegeRepository.selectById(applicant.getCollegeId());
        if (college == null) {
            throw new BusinessException("申请人所属学院不存在");
        }

        // 上传证明文件到IPFS
        List<Map<String, String>> proofFiles = uploadProofFiles(request.getFiles());

        // 创建申请记录
        Application application = Application.builder()
                .applicantId(applicantId)
                .title(request.getTitle())
                .certificateType(request.getCertificateType())
                .status(ApplicationStatus.PENDING_COLLEGE.getCode())
                .collegeId(applicant.getCollegeId())
                .proofFiles(proofFiles)
                .build();

        try {
            applicationRepository.insert(application);
            log.info("申请创建成功，申请ID: {}", application.getId());

            return buildApplicationResponse(application, applicant, college);
        } catch (Exception e) {
            // 如果数据库操作失败，需要清理已上传的IPFS文件
            cleanupUploadedFiles(proofFiles);
            log.error("创建申请失败，申请人ID: {}", applicantId, e);
            throw new BusinessException("创建申请失败");
        }
    }

    /**
     * 查询申请列表（支持筛选和搜索）
     * 
     * @param applicantId 申请人ID（可选，为空时查询所有）
     * @param status 申请状态（可选）
     * @param certificateType 证书类型（可选）
     * @param keyword 关键词搜索（可选，搜索标题）
     * @param page 页码
     * @param size 每页大小
     * @return 分页申请列表
     */
    public IPage<ApplicationListVO> getApplicationList(String applicantId, String status, 
                                                      String certificateType, String keyword, 
                                                      int page, int size) {
        log.info("查询申请列表，申请人ID: {}, 状态: {}, 类型: {}, 关键词: {}, 页码: {}, 大小: {}", 
                applicantId, status, certificateType, keyword, page, size);

        // 构建查询条件
        LambdaQueryWrapper<Application> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.isNotBlank(applicantId)) {
            queryWrapper.eq(Application::getApplicantId, applicantId);
        }
        
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq(Application::getStatus, status);
        }
        
        if (StringUtils.isNotBlank(certificateType)) {
            queryWrapper.eq(Application::getCertificateType, certificateType);
        }
        
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like(Application::getTitle, keyword);
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Application::getCreateTime);

        // 分页查询
        Page<Application> pageParam = new Page<>(page, size);
        IPage<Application> applicationPage = applicationRepository.selectPage(pageParam, queryWrapper);

        // 转换为VO
        return convertToApplicationListVO(applicationPage);
    }

    /**
     * 查询申请详情
     * 
     * @param applicationId 申请ID
     * @param applicantId 申请人ID（用于权限验证，可选）
     * @return 申请详情
     * @throws BusinessException 业务异常
     */
    public ApplicationResponse getApplicationDetail(String applicationId, String applicantId) throws BusinessException {
        log.info("查询申请详情，申请ID: {}, 申请人ID: {}", applicationId, applicantId);

        Application application = applicationRepository.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        // 权限验证：如果指定了申请人ID，则只能查看自己的申请
        if (StringUtils.isNotBlank(applicantId) && !applicantId.equals(application.getApplicantId())) {
            throw new BusinessException("无权查看该申请");
        }

        // 获取申请人信息
        User applicant = userRepository.selectById(application.getApplicantId());
        if (applicant == null) {
            throw new BusinessException("申请人信息不存在");
        }

        // 获取学院信息
        College college = collegeRepository.selectById(application.getCollegeId());
        if (college == null) {
            throw new BusinessException("学院信息不存在");
        }

        return buildApplicationResponse(application, applicant, college);
    }

    /**
     * 取消申请（删除数据库记录和IPFS文件）
     * 
     * @param applicationId 申请ID
     * @param applicantId 申请人ID
     * @throws BusinessException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelApplication(String applicationId, String applicantId) throws BusinessException {
        log.info("开始取消申请，申请ID: {}, 申请人ID: {}", applicationId, applicantId);

        Application application = applicationRepository.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        // 权限验证：只能取消自己的申请
        if (!applicantId.equals(application.getApplicantId())) {
            throw new BusinessException("无权取消该申请");
        }

        // 状态验证：只能取消未审批的申请
        if (!ApplicationStatus.PENDING_COLLEGE.getCode().equals(application.getStatus())) {
            throw new BusinessException("只能取消待学院审批的申请");
        }

        // 删除IPFS文件
        if (application.getProofFiles() != null && !application.getProofFiles().isEmpty()) {
            cleanupUploadedFiles(application.getProofFiles());
        }

        // 删除数据库记录
        applicationRepository.deleteById(applicationId);

        log.info("申请取消成功，申请ID: {}", applicationId);
    }

    /**
     * 上传证明文件到IPFS
     * 
     * @param files 文件列表
     * @return 文件信息列表
     * @throws BusinessException 业务异常
     */
    private List<Map<String, String>> uploadProofFiles(List<MultipartFile> files) throws BusinessException {
        if (files == null || files.isEmpty()) {
            throw new BusinessException("证明文件不能为空");
        }

        if (files.size() > 3) {
            throw new BusinessException("证明文件数量不能超过3个");
        }

        List<Map<String, String>> proofFiles = new ArrayList<>();
        List<String> uploadedCids = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }

                // 上传到IPFS
                String cid = ipfsService.uploadFile(file);
                uploadedCids.add(cid);

                // 构建文件信息
                Map<String, String> fileInfo = new HashMap<>();
                fileInfo.put("name", file.getOriginalFilename());
                fileInfo.put("cid", cid);
                fileInfo.put("size", String.valueOf(file.getSize()));
                fileInfo.put("contentType", file.getContentType());

                proofFiles.add(fileInfo);
            }

            if (proofFiles.isEmpty()) {
                throw new BusinessException("没有有效的证明文件");
            }

            log.info("证明文件上传成功，文件数量: {}", proofFiles.size());
            return proofFiles;

        } catch (Exception e) {
            // 如果上传过程中出现异常，清理已上传的文件
            for (String cid : uploadedCids) {
                try {
                    ipfsService.deleteFile(cid);
                } catch (Exception cleanupException) {
                    log.warn("清理IPFS文件失败，CID: {}", cid, cleanupException);
                }
            }
            throw e;
        }
    }

    /**
     * 清理已上传的IPFS文件
     * 
     * @param proofFiles 文件信息列表
     */
    private void cleanupUploadedFiles(List<Map<String, String>> proofFiles) {
        if (proofFiles == null || proofFiles.isEmpty()) {
            return;
        }

        for (Map<String, String> fileInfo : proofFiles) {
            String cid = fileInfo.get("cid");
            if (StringUtils.isNotBlank(cid)) {
                try {
                    ipfsService.deleteFile(cid);
                    log.info("清理IPFS文件成功，CID: {}", cid);
                } catch (Exception e) {
                    log.warn("清理IPFS文件失败，CID: {}", cid, e);
                }
            }
        }
    }

    /**
     * 构建申请响应对象
     * 
     * @param application 申请实体
     * @param applicant 申请人
     * @param college 学院
     * @return 申请响应
     */
    private ApplicationResponse buildApplicationResponse(Application application, User applicant, College college) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .applicantId(application.getApplicantId())
                .applicantName(applicant.getName())
                .studentNo(applicant.getStudentNo())
                .title(application.getTitle())
                .certificateType(application.getCertificateType())
                .status(application.getStatus())
                .statusDesc(ApplicationStatus.getDescByCode(application.getStatus()))
                .collegeId(application.getCollegeId())
                .collegeName(college.getName())
                .proofFiles(application.getProofFiles())
                .createTime(application.getCreateTime())
                .updateTime(application.getUpdateTime())
                .build();
    }

    /**
     * 转换申请分页结果为VO
     * 
     * @param applicationPage 申请分页结果
     * @return VO分页结果
     */
    private IPage<ApplicationListVO> convertToApplicationListVO(IPage<Application> applicationPage) {
        List<Application> applications = applicationPage.getRecords();
        if (applications.isEmpty()) {
            return new Page<ApplicationListVO>(applicationPage.getCurrent(), applicationPage.getSize(), applicationPage.getTotal());
        }

        // 获取所有申请人ID和学院ID
        Set<String> applicantIds = applications.stream()
                .map(Application::getApplicantId)
                .collect(Collectors.toSet());
        
        Set<String> collegeIds = applications.stream()
                .map(Application::getCollegeId)
                .collect(Collectors.toSet());

        // 批量查询申请人和学院信息
        Map<String, User> applicantMap = userRepository.selectBatchIds(applicantIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        
        Map<String, College> collegeMap = collegeRepository.selectBatchIds(collegeIds)
                .stream()
                .collect(Collectors.toMap(College::getId, college -> college));

        // 转换为VO
        List<ApplicationListVO> voList = applications.stream()
                .map(application -> {
                    User applicant = applicantMap.get(application.getApplicantId());
                    College college = collegeMap.get(application.getCollegeId());
                    
                    return ApplicationListVO.builder()
                            .id(application.getId())
                            .title(application.getTitle())
                            .certificateType(application.getCertificateType())
                            .status(application.getStatus())
                            .statusDesc(ApplicationStatus.getDescByCode(application.getStatus()))
                            .applicantName(applicant != null ? applicant.getName() : "未知")
                            .studentNo(applicant != null ? applicant.getStudentNo() : "未知")
                            .collegeName(college != null ? college.getName() : "未知")
                            .fileCount(application.getProofFiles() != null ? application.getProofFiles().size() : 0)
                            .createTime(application.getCreateTime())
                            .updateTime(application.getUpdateTime())
                            .build();
                })
                .collect(Collectors.toList());

        // 构建分页结果
        Page<ApplicationListVO> voPage = new Page<>(applicationPage.getCurrent(), applicationPage.getSize(), applicationPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }
}