package com.blockchain.certificate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blockchain.certificate.domain.application.model.Application;
import com.blockchain.certificate.domain.certificate.model.Certificate;
import com.blockchain.certificate.domain.user.model.User;
import com.blockchain.certificate.domain.organization.model.College;
import com.blockchain.certificate.model.vo.StatisticsVO;
import com.blockchain.certificate.domain.application.repository.ApplicationRepository;
import com.blockchain.certificate.domain.certificate.repository.CertificateRepository;
import com.blockchain.certificate.domain.user.repository.UserRepository;
import com.blockchain.certificate.domain.organization.repository.CollegeRepository;
import com.blockchain.certificate.domain.approval.repository.ApprovalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    
    private final ApplicationRepository applicationRepository;
    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final ApprovalRepository approvalRepository;
    
    /**
     * 获取系统统计数据
     */
    public StatisticsVO getSystemStatistics() {
        log.info("获取系统统计数据");
        
        StatisticsVO statistics = new StatisticsVO();
        
        // 申请统计
        statistics.setTotalApplications(countAllApplications());
        statistics.setPendingApplications(countApplicationsByStatus("PENDING"));
        statistics.setApprovedApplications(countApplicationsByStatus("APPROVED"));
        statistics.setRejectedApplications(countApplicationsByStatus("REJECTED"));
        statistics.setCancelledApplications(countApplicationsByStatus("CANCELLED"));
        
        // 证书统计
        statistics.setTotalCertificates(countAllCertificates());
        statistics.setValidCertificates(countCertificatesByStatus("VALID"));
        statistics.setRevokedCertificates(countCertificatesByStatus("REVOKED"));
        
        // 用户统计
        statistics.setTotalUsers(countAllUsers());
        statistics.setStudentUsers(countUsersByRole("STUDENT"));
        statistics.setTeacherUsers(countUsersByRole("TEACHER"));
        statistics.setAdminUsers(countUsersByRole("ADMIN"));
        
        // 按学院统计申请数
        statistics.setApplicationsByCollege(countApplicationsByCollege());
        
        // 按专业统计申请数
        statistics.setApplicationsByMajor(countApplicationsByMajor());
        
        // 按月份统计申请数
        statistics.setApplicationsByMonth(countApplicationsByMonth());
        
        // 按证书类型统计
        statistics.setCertificatesByType(countCertificatesByType());
        
        // 区块链和IPFS统计（示例数据）
        statistics.setTotalBlockchainTransactions(countAllCertificates());
        statistics.setTotalIpfsFiles(countAllCertificates());
        
        return statistics;
    }
    
    /**
     * 获取指定学院的统计数据
     */
    public StatisticsVO getCollegeStatistics(String collegeId) {
        log.info("获取学院统计数据: collegeId={}", collegeId);
        
        StatisticsVO statistics = new StatisticsVO();
        
        // 该学院的申请统计
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getCollegeId, collegeId);
        statistics.setTotalApplications((long) applicationRepository.selectCount(wrapper));
        
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getCollegeId, collegeId);
        wrapper.eq(Application::getStatus, "PENDING");
        statistics.setPendingApplications((long) applicationRepository.selectCount(wrapper));
        
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getCollegeId, collegeId);
        wrapper.eq(Application::getStatus, "APPROVED");
        statistics.setApprovedApplications((long) applicationRepository.selectCount(wrapper));
        
        // 该学院的用户统计
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getCollegeId, collegeId);
        statistics.setTotalUsers((long) userRepository.selectCount(userWrapper));
        
        return statistics;
    }
    
    /**
     * 获取指定时间范围的统计数据
     */
    public StatisticsVO getStatisticsByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("获取时间范围统计数据: startTime={}, endTime={}", startTime, endTime);
        
        StatisticsVO statistics = new StatisticsVO();
        
        // 时间范围内的申请统计
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(Application::getCreateTime, startTime, endTime);
        statistics.setTotalApplications((long) applicationRepository.selectCount(wrapper));
        
        // 时间范围内的证书统计
        LambdaQueryWrapper<Certificate> certWrapper = new LambdaQueryWrapper<>();
        certWrapper.between(Certificate::getCreateTime, startTime, endTime);
        statistics.setTotalCertificates((long) certificateRepository.selectCount(certWrapper));
        
        return statistics;
    }
    
    // ========== 私有辅助方法 ==========
    
    private Long countAllApplications() {
        return (long) applicationRepository.selectCount(null);
    }
    
    private Long countApplicationsByStatus(String status) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getStatus, status);
        return (long) applicationRepository.selectCount(wrapper);
    }
    
    private Long countAllCertificates() {
        return (long) certificateRepository.selectCount(null);
    }
    
    private Long countCertificatesByStatus(String status) {
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Certificate::getStatus, status);
        return (long) certificateRepository.selectCount(wrapper);
    }
    
    private Long countAllUsers() {
        return (long) userRepository.selectCount(null);
    }
    
    private Long countUsersByRole(String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, role);
        return (long) userRepository.selectCount(wrapper);
    }
    
    private Map<String, Long> countApplicationsByCollege() {
        List<Application> applications = applicationRepository.selectList(null);
        List<College> colleges = collegeRepository.selectList(null);
        
        Map<String, String> collegeIdToName = colleges.stream()
                .collect(Collectors.toMap(College::getId, College::getName));
        
        Map<String, Long> result = applications.stream()
                .filter(app -> app.getCollegeId() != null)
                .collect(Collectors.groupingBy(
                        app -> collegeIdToName.getOrDefault(app.getCollegeId(), "未知学院"),
                        Collectors.counting()
                ));
        
        return result;
    }
    
    private Map<String, Long> countApplicationsByMajor() {
        List<Application> applications = applicationRepository.selectList(null);
        
        Map<String, Long> result = applications.stream()
                .filter(app -> app.getMajorId() != null)
                .collect(Collectors.groupingBy(
                        Application::getMajorId,
                        Collectors.counting()
                ));
        
        return result;
    }
    
    private Map<String, Long> countApplicationsByMonth() {
        List<Application> applications = applicationRepository.selectList(null);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        Map<String, Long> result = applications.stream()
                .collect(Collectors.groupingBy(
                        app -> app.getCreateTime().format(formatter),
                        Collectors.counting()
                ));
        
        return result;
    }
    
    private Map<String, Long> countCertificatesByType() {
        List<Certificate> certificates = certificateRepository.selectList(null);
        
        Map<String, Long> result = certificates.stream()
                .filter(cert -> cert.getType() != null)
                .collect(Collectors.groupingBy(
                        Certificate::getType,
                        Collectors.counting()
                ));
        
        return result;
    }
}