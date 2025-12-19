package com.blockchain.certificate.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 统计数据视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsVO {
    
    /**
     * 总申请数
     */
    private Long totalApplications;
    
    /**
     * 待审批数
     */
    private Long pendingApplications;
    
    /**
     * 已通过数
     */
    private Long approvedApplications;
    
    /**
     * 已拒绝数
     */
    private Long rejectedApplications;
    
    /**
     * 已撤销数
     */
    private Long cancelledApplications;
    
    /**
     * 总证书数
     */
    private Long totalCertificates;
    
    /**
     * 有效证书数
     */
    private Long validCertificates;
    
    /**
     * 已撤销证书数
     */
    private Long revokedCertificates;
    
    /**
     * 总用户数
     */
    private Long totalUsers;
    
    /**
     * 学生用户数
     */
    private Long studentUsers;
    
    /**
     * 教师用户数
     */
    private Long teacherUsers;
    
    /**
     * 管理员用户数
     */
    private Long adminUsers;
    
    /**
     * 按学院统计申请数
     */
    private Map<String, Long> applicationsByCollege;
    
    /**
     * 按专业统计申请数
     */
    private Map<String, Long> applicationsByMajor;
    
    /**
     * 按月份统计申请数
     */
    private Map<String, Long> applicationsByMonth;
    
    /**
     * 按证书类型统计
     */
    private Map<String, Long> certificatesByType;
    
    /**
     * 区块链交易总数
     */
    private Long totalBlockchainTransactions;
    
    /**
     * IPFS 存储文件数
     */
    private Long totalIpfsFiles;
}