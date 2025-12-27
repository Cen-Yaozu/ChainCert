package com.blockchain.certificate.interfaces.rest.approval;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blockchain.certificate.shared.common.Result;
import com.blockchain.certificate.model.dto.ApprovalRequest;
import com.blockchain.certificate.model.dto.ApprovalResponse;
import com.blockchain.certificate.model.vo.ApprovalHistoryVO;
import com.blockchain.certificate.infrastructure.security.UserPrincipal;
import com.blockchain.certificate.domain.approval.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 审批控制器
 * 提供证书申请审批相关的REST API
 */
@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ApprovalController {

    private final ApprovalService approvalService;

    /**
     * 审批申请
     * 
     * @param applicationId 申请ID
     * @param request 审批请求
     * @param userPrincipal 当前用户
     * @return 审批结果
     */
    @PostMapping("/{applicationId}")
    @PreAuthorize("hasRole('COLLEGE_TEACHER') or hasRole('SCHOOL_TEACHER')")
    public Result<ApprovalResponse> approveApplication(
            @PathVariable @NotBlank(message = "申请ID不能为空") String applicationId,
            @RequestBody @Valid ApprovalRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("收到审批请求，申请ID: {}, 审批人: {}, 动作: {}", 
                applicationId, userPrincipal.getUsername(), request.getAction());

        try {
            ApprovalResponse response = approvalService.approveApplication(
                    applicationId, request, userPrincipal.getId());
            
            log.info("审批成功，申请ID: {}, 新状态: {}", applicationId, response.getNewStatus());
            
            return Result.success("审批成功", response);
            
        } catch (Exception e) {
            log.error("审批失败，申请ID: {}, 审批人: {}", applicationId, userPrincipal.getUsername(), e);
            return Result.error("审批失败：" + e.getMessage());
        }
    }

    /**
     * 查询待审批列表
     * 
     * @param certificateType 证书类型（可选）
     * @param keyword 关键词搜索（可选）
     * @param page 页码，默认1
     * @param size 每页大小，默认10
     * @param userPrincipal 当前用户
     * @return 待审批列表
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('COLLEGE_TEACHER') or hasRole('SCHOOL_TEACHER')")
    public Result<IPage<ApprovalHistoryVO>> getPendingApprovalList(
            @RequestParam(required = false) String certificateType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Integer size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("查询待审批列表，审批人: {}, 证书类型: {}, 关键词: {}, 页码: {}, 大小: {}", 
                userPrincipal.getUsername(), certificateType, keyword, page, size);

        try {
            IPage<ApprovalHistoryVO> result = approvalService.getPendingApprovalList(
                    userPrincipal.getId(), certificateType, keyword, page, size);
            
            log.info("查询待审批列表成功，审批人: {}, 总数: {}", userPrincipal.getUsername(), result.getTotal());
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("查询待审批列表失败，审批人: {}", userPrincipal.getUsername(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询我的审批记录
     * 
     * @param applicationId 申请ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param page 页码，默认1
     * @param size 每页大小，默认10
     * @param userPrincipal 当前用户
     * @return 我的审批记录
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('COLLEGE_TEACHER') or hasRole('SCHOOL_TEACHER')")
    public Result<IPage<ApprovalHistoryVO>> getMyApprovalHistory(
            @RequestParam(required = false) String applicationId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Integer size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("查询我的审批记录，审批人: {}, 申请ID: {}, 开始日期: {}, 结束日期: {}, 页码: {}, 大小: {}", 
                userPrincipal.getUsername(), applicationId, startDate, endDate, page, size);

        try {
            IPage<ApprovalHistoryVO> result = approvalService.getApprovalHistory(
                    userPrincipal.getId(), applicationId, startDate, endDate, page, size);
            
            log.info("查询我的审批记录成功，审批人: {}, 总数: {}", userPrincipal.getUsername(), result.getTotal());
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("查询我的审批记录失败，审批人: {}", userPrincipal.getUsername(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询申请的审批历史（管理员或申请人可查看）
     * 
     * @param applicationId 申请ID
     * @param userPrincipal 当前用户
     * @return 申请的审批历史
     */
    @GetMapping("/history/{applicationId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT') or hasRole('COLLEGE_TEACHER') or hasRole('SCHOOL_TEACHER')")
    public Result<IPage<ApprovalHistoryVO>> getApplicationApprovalHistory(
            @PathVariable @NotBlank(message = "申请ID不能为空") String applicationId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Integer size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("查询申请审批历史，申请ID: {}, 查询人: {}, 页码: {}, 大小: {}", 
                applicationId, userPrincipal.getUsername(), page, size);

        try {
            IPage<ApprovalHistoryVO> result = approvalService.getApprovalHistory(
                    null, applicationId, null, null, page, size);
            
            log.info("查询申请审批历史成功，申请ID: {}, 总数: {}", applicationId, result.getTotal());
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("查询申请审批历史失败，申请ID: {}, 查询人: {}", applicationId, userPrincipal.getUsername(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有审批记录（仅管理员）
     * 
     * @param approverId 审批人ID（可选）
     * @param applicationId 申请ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param page 页码，默认1
     * @param size 每页大小，默认10
     * @return 所有审批记录
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<ApprovalHistoryVO>> getAllApprovalHistory(
            @RequestParam(required = false) String approverId,
            @RequestParam(required = false) String applicationId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Integer size) {
        
        log.info("查询所有审批记录，审批人ID: {}, 申请ID: {}, 开始日期: {}, 结束日期: {}, 页码: {}, 大小: {}", 
                approverId, applicationId, startDate, endDate, page, size);

        try {
            IPage<ApprovalHistoryVO> result = approvalService.getApprovalHistory(
                    approverId, applicationId, startDate, endDate, page, size);
            
            log.info("查询所有审批记录成功，总数: {}", result.getTotal());
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("查询所有审批记录失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}