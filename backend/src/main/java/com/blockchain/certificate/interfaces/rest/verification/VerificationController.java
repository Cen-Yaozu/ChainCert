package com.blockchain.certificate.interfaces.rest.verification;

import com.blockchain.certificate.shared.common.Result;
import com.blockchain.certificate.shared.exception.BusinessException;
import com.blockchain.certificate.domain.certificate.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 证书核验控制器
 * 提供公开的证书验证接口（无需登录）
 */
@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
@Slf4j
public class VerificationController {

    private final VerificationService verificationService;

    /**
     * 验证证书
     * 公开接口，无需登录
     * 
     * @param certificateNo 证书编号
     * @return 验证结果
     */
    @PostMapping("/verify")
    public Result<VerificationService.VerificationResult> verifyCertificate(
            @RequestParam String certificateNo) {
        
        try {
            log.info("收到证书验证请求，证书编号: {}", certificateNo);
            
            VerificationService.VerificationResult result = 
                verificationService.verifyCertificate(certificateNo);
            
            if (result.isValid()) {
                log.info("证书验证成功，证书编号: {}", certificateNo);
                return Result.success("证书验证成功", result);
            } else {
                log.warn("证书验证失败，证书编号: {}, 原因: {}", certificateNo, result.getMessage());
                return Result.success(result.getMessage(), result);
            }
            
        } catch (Exception e) {
            log.error("证书验证异常，证书编号: {}", certificateNo, e);
            return Result.error("证书验证失败: " + e.getMessage());
        }
    }

    /**
     * 通过 GET 方式验证证书
     * 公开接口，无需登录
     * 
     * @param certificateNo 证书编号
     * @return 验证结果
     */
    @GetMapping("/verify/{certificateNo}")
    public Result<VerificationService.VerificationResult> verifyCertificateByGet(
            @PathVariable String certificateNo) {
        
        try {
            log.info("收到证书验证请求（GET），证书编号: {}", certificateNo);
            
            VerificationService.VerificationResult result = 
                verificationService.verifyCertificate(certificateNo);
            
            if (result.isValid()) {
                log.info("证书验证成功，证书编号: {}", certificateNo);
                return Result.success("证书验证成功", result);
            } else {
                log.warn("证书验证失败，证书编号: {}, 原因: {}", certificateNo, result.getMessage());
                return Result.success(result.getMessage(), result);
            }
            
        } catch (Exception e) {
            log.error("证书验证异常，证书编号: {}", certificateNo, e);
            return Result.error("证书验证失败: " + e.getMessage());
        }
    }

    /**
     * 下载证书
     * 公开接口，无需登录
     * 
     * @param certificateNo 证书编号
     * @return 证书 PDF 文件
     */
    @GetMapping("/download/{certificateNo}")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable String certificateNo) {
        
        try {
            log.info("收到证书下载请求，证书编号: {}", certificateNo);
            
            // 下载证书
            byte[] pdfContent = verificationService.downloadCertificateByNo(certificateNo);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", certificateNo + ".pdf");
            headers.setContentLength(pdfContent.length);
            
            log.info("证书下载成功，证书编号: {}", certificateNo);
            
            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
            
        } catch (BusinessException e) {
            log.error("下载证书失败，证书编号: {}, 原因: {}", certificateNo, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("下载证书异常，证书编号: {}", certificateNo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 在线预览证书
     * 公开接口，无需登录
     * 
     * @param certificateNo 证书编号
     * @return 证书 PDF 文件（inline 方式）
     */
    @GetMapping("/preview/{certificateNo}")
    public ResponseEntity<byte[]> previewCertificate(@PathVariable String certificateNo) {
        
        try {
            log.info("收到证书预览请求，证书编号: {}", certificateNo);
            
            // 下载证书
            byte[] pdfContent = verificationService.downloadCertificateByNo(certificateNo);
            
            // 设置响应头（inline 方式，浏览器直接打开）
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + certificateNo + ".pdf");
            headers.setContentLength(pdfContent.length);
            
            log.info("证书预览成功，证书编号: {}", certificateNo);
            
            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
            
        } catch (BusinessException e) {
            log.error("预览证书失败，证书编号: {}, 原因: {}", certificateNo, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("预览证书异常，证书编号: {}", certificateNo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 健康检查接口
     * 
     * @return 服务状态
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Verification service is running");
    }
}