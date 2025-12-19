package com.blockchain.certificate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blockchain.certificate.common.PageResult;
import com.blockchain.certificate.common.Result;
import com.blockchain.certificate.model.dto.ApplicationRequest;
import com.blockchain.certificate.model.dto.ApplicationResponse;
import com.blockchain.certificate.model.vo.ApplicationListVO;
import com.blockchain.certificate.security.UserPrincipal;
import com.blockchain.certificate.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 申请控制器
 * 处理证书申请的创建、查询、取消等请求
 */
@Slf4j
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * 创建申请
     * 只有学生可以创建申请
     *
     * @param request 申请请求
     * @return 申请响应
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public Result<ApplicationResponse> createApplication(@Validated @ModelAttribute ApplicationRequest request) {
        String applicantId = getCurrentUserId();
        log.info("创建申请请求，申请人ID: {}, 标题: {}", applicantId, request.getTitle());
        
        ApplicationResponse response = applicationService.createApplication(request, applicantId);
        
        log.info("申请创建成功，申请ID: {}", response.getId());
        return Result.success("申请提交成功", response);
    }

    /**
     * 查询申请列表
     * 学生只能查看自己的申请，老师和管理员可以查看所有申请
     *
     * @param status 申请状态（可选）
     * @param certificateType 证书类型（可选）
     * @param keyword 关键词搜索（可选）
     * @param page 页码，默认1
     * @param size 每页大小，默认10
     * @return 分页申请列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'COLLEGE_TEACHER', 'SCHOOL_TEACHER', 'ADMIN')")
    public Result<PageResult<ApplicationListVO>> getApplicationList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String certificateType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        String currentUserId = getCurrentUserId();
        String currentUserRole = getCurrentUserRole();
        
        // 学生只能查看自己的申请
        String applicantId = "STUDENT".equals(currentUserRole) ? currentUserId : null;
        
        log.info("查询申请列表，用户ID: {}, 角色: {}, 状态: {}, 类型: {}, 关键词: {}, 页码: {}, 大小: {}", 
                currentUserId, currentUserRole, status, certificateType, keyword, page, size);
        
        IPage<ApplicationListVO> applicationPage = applicationService.getApplicationList(
                applicantId, status, certificateType, keyword, page, size);
        
        PageResult<ApplicationListVO> pageResult = new PageResult<>(
                applicationPage.getTotal(),
                applicationPage.getRecords(),
                applicationPage.getCurrent(),
                applicationPage.getSize(),
                applicationPage.getPages()
        );
        
        return Result.success(pageResult);
    }

    /**
     * 查询申请详情
     * 学生只能查看自己的申请详情，老师和管理员可以查看所有申请详情
     *
     * @param id 申请ID
     * @return 申请详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'COLLEGE_TEACHER', 'SCHOOL_TEACHER', 'ADMIN')")
    public Result<ApplicationResponse> getApplicationDetail(@PathVariable String id) {
        String currentUserId = getCurrentUserId();
        String currentUserRole = getCurrentUserRole();
        
        // 学生只能查看自己的申请
        String applicantId = "STUDENT".equals(currentUserRole) ? currentUserId : null;
        
        log.info("查询申请详情，申请ID: {}, 用户ID: {}, 角色: {}", id, currentUserId, currentUserRole);
        
        ApplicationResponse response = applicationService.getApplicationDetail(id, applicantId);
        
        return Result.success(response);
    }

    /**
     * 取消申请
     * 只有学生可以取消自己的申请，且只能取消待学院审批的申请
     *
     * @param id 申请ID
     * @return 取消结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> cancelApplication(@PathVariable String id) {
        String applicantId = getCurrentUserId();
        log.info("取消申请请求，申请ID: {}, 申请人ID: {}", id, applicantId);
        
        applicationService.cancelApplication(id, applicantId);
        
        log.info("申请取消成功，申请ID: {}", id);
        Result<Void> result = Result.<Void>success();
        result.setMessage("申请取消成功");
        return result;
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal) {
                return ((UserPrincipal) principal).getId();
            }
        }
        throw new RuntimeException("用户未登录");
    }

    /**
     * 获取当前用户角色
     *
     * @return 用户角色
     */
    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal) {
                return ((UserPrincipal) principal).getRole();
            }
            // 从authorities中获取角色信息作为备选方案
            return authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                    .orElse("STUDENT");
        }
        throw new RuntimeException("用户未登录");
    }
}