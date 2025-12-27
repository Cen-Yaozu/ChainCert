package com.blockchain.certificate.interfaces.rest.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blockchain.certificate.shared.common.Result;
import com.blockchain.certificate.shared.exception.BusinessException;
import com.blockchain.certificate.domain.certificate.model.Certificate;
import com.blockchain.certificate.infrastructure.security.UserPrincipal;
import com.blockchain.certificate.domain.certificate.service.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 证书控制器
 * 提供证书查询、下载、撤销等接口
 */
@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
@Slf4j
public class CertificateController {

    private final CertificateService certificateService;

    /**
     * 查询证书列表
     * 
     * @param certificateNo 证书编号（可选）
     * @param certificateType 证书类型（可选）
     * @param status 证书状态（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param page 页码
     * @param size 每页大小
     * @param userPrincipal 当前用户
     * @return 证书列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'COLLEGE_TEACHER', 'SCHOOL_TEACHER', 'ADMIN')")
    public Result<IPage<Certificate>> getCertificateList(
            @RequestParam(required = false) String certificateNo,
            @RequestParam(required = false) String certificateType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        try {
            String holderId = null;
            
            // 学生只能查看自己的证书
            if ("STUDENT".equals(userPrincipal.getRole())) {
                holderId = userPrincipal.getId();
            }
            
            IPage<Certificate> certificatePage = certificateService.getCertificateList(
                    holderId, certificateNo, certificateType, status, 
                    startDate, endDate, page, size);
            
            return Result.success(certificatePage);
            
        } catch (BusinessException e) {
            log.error("查询证书列表失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询证书列表异常", e);
            return Result.error("查询证书列表失败");
        }
    }

    /**
     * 查询我的证书列表（学生端）
     * 
     * @param certificateType 证书类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param page 页码
     * @param size 每页大小
     * @param userPrincipal 当前用户
     * @return 证书列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<IPage<Certificate>> getMyCertificates(
            @RequestParam(required = false) String certificateType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        try {
            IPage<Certificate> certificatePage = certificateService.getCertificateList(
                    userPrincipal.getId(), null, certificateType, 
                    CertificateService.CertificateStatus.VALID.getCode(),
                    startDate, endDate, page, size);
            
            return Result.success(certificatePage);
            
        } catch (BusinessException e) {
            log.error("查询我的证书列表失败，用户ID: {}", userPrincipal.getId(), e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询我的证书列表异常，用户ID: {}", userPrincipal.getId(), e);
            return Result.error("查询证书列表失败");
        }
    }

    /**
     * 查询证书详情
     * 
     * @param id 证书ID
     * @param userPrincipal 当前用户
     * @return 证书详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'COLLEGE_TEACHER', 'SCHOOL_TEACHER', 'ADMIN')")
    public Result<Certificate> getCertificateDetail(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        try {
            Certificate certificate = certificateService.getCertificateDetail(id);
            
            // 学生只能查看自己的证书
            if ("STUDENT".equals(userPrincipal.getRole()) && 
                !userPrincipal.getId().equals(certificate.getHolderId())) {
                return Result.error("无权查看该证书");
            }
            
            return Result.success(certificate);
            
        } catch (BusinessException e) {
            log.error("查询证书详情失败，证书ID: {}", id, e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询证书详情异常，证书ID: {}", id, e);
            return Result.error("查询证书详情失败");
        }
    }

    /**
     * 根据证书编号查询证书
     * 
     * @param certificateNo 证书编号
     * @return 证书信息
     */
    @GetMapping("/by-no/{certificateNo}")
    @PreAuthorize("hasAnyRole('STUDENT', 'COLLEGE_TEACHER', 'SCHOOL_TEACHER', 'ADMIN')")
    public Result<Certificate> getCertificateByCertificateNo(
            @PathVariable String certificateNo,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        try {
            Certificate certificate = certificateService.getCertificateByCertificateNo(certificateNo);
            
            // 学生只能查看自己的证书
            if ("STUDENT".equals(userPrincipal.getRole()) && 
                !userPrincipal.getId().equals(certificate.getHolderId())) {
                return Result.error("无权查看该证书");
            }
            
            return Result.success(certificate);
            
        } catch (BusinessException e) {
            log.error("根据证书编号查询证书失败，证书编号: {}", certificateNo, e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("根据证书编号查询证书异常，证书编号: {}", certificateNo, e);
            return Result.error("查询证书失败");
        }
    }

    /**
     * 下载证书
     * 
     * @param id 证书ID
     * @param userPrincipal 当前用户
     * @return 证书 PDF 文件
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('STUDENT', 'COLLEGE_TEACHER', 'SCHOOL_TEACHER', 'ADMIN')")
    public ResponseEntity<byte[]> downloadCertificate(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        try {
            // 获取证书信息
            Certificate certificate = certificateService.getCertificateDetail(id);
            
            // 学生只能下载自己的证书
            if ("STUDENT".equals(userPrincipal.getRole()) && 
                !userPrincipal.getId().equals(certificate.getHolderId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // 下载证书
            byte[] pdfContent = certificateService.downloadCertificate(id);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    certificate.getCertificateNo() + ".pdf");
            headers.setContentLength(pdfContent.length);
            
            log.info("证书下载成功，证书ID: {}, 用户ID: {}", id, userPrincipal.getId());
            
            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
            
        } catch (BusinessException e) {
            log.error("下载证书失败，证书ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("下载证书异常，证书ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 撤销证书（管理员）
     * 
     * @param id 证书ID
     * @param reason 撤销原因
     * @return 操作结果
     */
    @PutMapping("/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> revokeCertificate(
            @PathVariable String id,
            @RequestParam(required = false) String reason) {
        
        try {
            certificateService.revokeCertificate(id, reason);
            log.info("证书撤销成功，证书ID: {}", id);
            Result<Void> result = Result.success();
            result.setMessage("证书撤销成功");
            return result;
            
        } catch (BusinessException e) {
            log.error("撤销证书失败，证书ID: {}", id, e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("撤销证书异常，证书ID: {}", id, e);
            return Result.error("撤销证书失败");
        }
    }

    /**
     * 统计证书数量
     * 
     * @param status 证书状态（可选）
     * @param userPrincipal 当前用户
     * @return 证书数量
     */
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public Result<Long> countCertificates(
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        try {
            String holderId = null;
            
            // 学生只能统计自己的证书
            if ("STUDENT".equals(userPrincipal.getRole())) {
                holderId = userPrincipal.getId();
            }
            
            long count = certificateService.countCertificates(holderId, status);
            return Result.success(count);
            
        } catch (Exception e) {
            log.error("统计证书数量异常", e);
            return Result.error("统计证书数量失败");
        }
    }
}