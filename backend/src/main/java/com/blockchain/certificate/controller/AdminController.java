package com.blockchain.certificate.controller;

import com.blockchain.certificate.common.PageResult;
import com.blockchain.certificate.common.Result;
import com.blockchain.certificate.model.dto.*;
import com.blockchain.certificate.model.entity.SystemLog;
import com.blockchain.certificate.model.vo.StatisticsVO;
import com.blockchain.certificate.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
@Tag(name = "管理员", description = "管理员相关接口")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final CollegeService collegeService;
    private final MajorService majorService;
    private final TemplateService templateService;
    private final SystemLogService systemLogService;
    private final StatisticsService statisticsService;
    
    // ========== 学院管理 ==========
    
    @PostMapping("/colleges")
    @Operation(summary = "创建学院", description = "管理员创建新学院")
    public Result<CollegeResponse> createCollege(@Valid @RequestBody CollegeRequest request) {
        log.info("创建学院请求: {}", request.getName());
        CollegeResponse response = collegeService.createCollege(request);
        return Result.success(response);
    }
    
    @PutMapping("/colleges/{collegeId}")
    @Operation(summary = "更新学院", description = "管理员更新学院信息")
    public Result<CollegeResponse> updateCollege(
            @Parameter(description = "学院ID") @PathVariable String collegeId,
            @Valid @RequestBody CollegeRequest request) {
        log.info("更新学院请求: collegeId={}", collegeId);
        CollegeResponse response = collegeService.updateCollege(collegeId, request);
        return Result.success(response);
    }
    
    @DeleteMapping("/colleges/{collegeId}")
    @Operation(summary = "删除学院", description = "管理员删除学院")
    public Result<Void> deleteCollege(
            @Parameter(description = "学院ID") @PathVariable String collegeId) {
        log.info("删除学院请求: collegeId={}", collegeId);
        collegeService.deleteCollege(collegeId);
        return Result.success();
    }
    
    @GetMapping("/colleges/{collegeId}")
    @Operation(summary = "获取学院详情", description = "获取指定学院的详细信息")
    public Result<CollegeResponse> getCollegeById(
            @Parameter(description = "学院ID") @PathVariable String collegeId) {
        log.info("获取学院详情请求: collegeId={}", collegeId);
        CollegeResponse response = collegeService.getCollegeById(collegeId);
        return Result.success(response);
    }
    
    @GetMapping("/colleges")
    @Operation(summary = "分页查询学院列表", description = "分页查询学院列表，支持关键字搜索")
    public Result<PageResult<CollegeResponse>> getCollegeList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword) {
        log.info("分页查询学院列表: page={}, size={}, keyword={}", page, size, keyword);
        PageResult<CollegeResponse> result = collegeService.getCollegeList(page, size, keyword);
        return Result.success(result);
    }
    
    @GetMapping("/colleges/all")
    @Operation(summary = "获取所有学院", description = "获取所有学院列表（不分页）")
    public Result<List<CollegeResponse>> getAllColleges() {
        log.info("获取所有学院列表");
        List<CollegeResponse> result = collegeService.getAllColleges();
        return Result.success(result);
    }
    
    @PutMapping("/colleges/{collegeId}/approver")
    @Operation(summary = "分配审批人", description = "为学院分配审批人")
    public Result<Void> assignApprover(
            @Parameter(description = "学院ID") @PathVariable String collegeId,
            @Parameter(description = "审批人ID") @RequestParam String approverId) {
        log.info("分配审批人请求: collegeId={}, approverId={}", collegeId, approverId);
        collegeService.assignApprover(collegeId, approverId);
        return Result.success();
    }
    
    // ========== 专业管理 ==========
    
    @PostMapping("/majors")
    @Operation(summary = "创建专业", description = "管理员创建新专业")
    public Result<MajorResponse> createMajor(@Valid @RequestBody MajorRequest request) {
        log.info("创建专业请求: {}", request.getName());
        MajorResponse response = majorService.createMajor(request);
        return Result.success(response);
    }
    
    @PutMapping("/majors/{majorId}")
    @Operation(summary = "更新专业", description = "管理员更新专业信息")
    public Result<MajorResponse> updateMajor(
            @Parameter(description = "专业ID") @PathVariable String majorId,
            @Valid @RequestBody MajorRequest request) {
        log.info("更新专业请求: majorId={}", majorId);
        MajorResponse response = majorService.updateMajor(majorId, request);
        return Result.success(response);
    }
    
    @DeleteMapping("/majors/{majorId}")
    @Operation(summary = "删除专业", description = "管理员删除专业")
    public Result<Void> deleteMajor(
            @Parameter(description = "专业ID") @PathVariable String majorId) {
        log.info("删除专业请求: majorId={}", majorId);
        majorService.deleteMajor(majorId);
        return Result.success();
    }
    
    @GetMapping("/majors/{majorId}")
    @Operation(summary = "获取专业详情", description = "获取指定专业的详细信息")
    public Result<MajorResponse> getMajorById(
            @Parameter(description = "专业ID") @PathVariable String majorId) {
        log.info("获取专业详情请求: majorId={}", majorId);
        MajorResponse response = majorService.getMajorById(majorId);
        return Result.success(response);
    }
    
    @GetMapping("/majors")
    @Operation(summary = "分页查询专业列表", description = "分页查询专业列表，支持关键字搜索和学院筛选")
    public Result<PageResult<MajorResponse>> getMajorList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "学院ID") @RequestParam(required = false) String collegeId) {
        log.info("分页查询专业列表: page={}, size={}, keyword={}, collegeId={}", 
                page, size, keyword, collegeId);
        PageResult<MajorResponse> result = majorService.getMajorList(page, size, keyword, collegeId);
        return Result.success(result);
    }
    
    @GetMapping("/majors/by-college/{collegeId}")
    @Operation(summary = "获取学院的所有专业", description = "获取指定学院的所有专业（不分页）")
    public Result<List<MajorResponse>> getMajorsByCollege(
            @Parameter(description = "学院ID") @PathVariable String collegeId) {
        log.info("获取学院的所有专业: collegeId={}", collegeId);
        List<MajorResponse> result = majorService.getMajorsByCollege(collegeId);
        return Result.success(result);
    }
    
    @GetMapping("/majors/all")
    @Operation(summary = "获取所有专业", description = "获取所有专业列表（不分页）")
    public Result<List<MajorResponse>> getAllMajors() {
        log.info("获取所有专业列表");
        List<MajorResponse> result = majorService.getAllMajors();
        return Result.success(result);
    }
    
    // ========== 证书模板管理 ==========
    
    @PostMapping("/templates")
    @Operation(summary = "创建证书模板", description = "管理员创建新证书模板")
    public Result<TemplateResponse> createTemplate(@Valid @RequestBody TemplateRequest request) {
        log.info("创建证书模板请求: {}", request.getName());
        TemplateResponse response = templateService.createTemplate(request);
        return Result.success(response);
    }
    
    @PutMapping("/templates/{templateId}")
    @Operation(summary = "更新证书模板", description = "管理员更新证书模板")
    public Result<TemplateResponse> updateTemplate(
            @Parameter(description = "模板ID") @PathVariable String templateId,
            @Valid @RequestBody TemplateRequest request) {
        log.info("更新证书模板请求: templateId={}", templateId);
        TemplateResponse response = templateService.updateTemplate(templateId, request);
        return Result.success(response);
    }
    
    @DeleteMapping("/templates/{templateId}")
    @Operation(summary = "删除证书模板", description = "管理员删除证书模板")
    public Result<Void> deleteTemplate(
            @Parameter(description = "模板ID") @PathVariable String templateId) {
        log.info("删除证书模板请求: templateId={}", templateId);
        templateService.deleteTemplate(templateId);
        return Result.success();
    }
    
    @GetMapping("/templates/{templateId}")
    @Operation(summary = "获取证书模板详情", description = "获取指定证书模板的详细信息")
    public Result<TemplateResponse> getTemplateById(
            @Parameter(description = "模板ID") @PathVariable String templateId) {
        log.info("获取证书模板详情请求: templateId={}", templateId);
        TemplateResponse response = templateService.getTemplateById(templateId);
        return Result.success(response);
    }
    
    @GetMapping("/templates")
    @Operation(summary = "分页查询证书模板列表", description = "分页查询证书模板列表，支持关键字搜索和筛选")
    public Result<PageResult<TemplateResponse>> getTemplateList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "模板类型") @RequestParam(required = false) String type,
            @Parameter(description = "是否启用") @RequestParam(required = false) Boolean enabled) {
        log.info("分页查询证书模板列表: page={}, size={}, keyword={}, type={}, enabled={}", 
                page, size, keyword, type, enabled);
        PageResult<TemplateResponse> result = templateService.getTemplateList(page, size, keyword, type, enabled);
        return Result.success(result);
    }
    
    @PutMapping("/templates/{templateId}/status")
    @Operation(summary = "启用/禁用模板", description = "管理员启用或禁用证书模板")
    public Result<Void> toggleTemplateStatus(
            @Parameter(description = "模板ID") @PathVariable String templateId,
            @Parameter(description = "是否启用") @RequestParam Boolean enabled) {
        log.info("切换模板状态请求: templateId={}, enabled={}", templateId, enabled);
        templateService.toggleTemplateStatus(templateId, enabled);
        return Result.success();
    }
    
    // ========== 系统日志 ==========
    
    @GetMapping("/logs")
    @Operation(summary = "分页查询系统日志", description = "分页查询系统日志，支持多条件筛选")
    public Result<PageResult<SystemLog>> getLogList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "模块") @RequestParam(required = false) String module,
            @Parameter(description = "用户ID") @RequestParam(required = false) String userId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        log.info("分页查询系统日志: page={}, size={}, keyword={}, module={}, userId={}", 
                page, size, keyword, module, userId);
        PageResult<SystemLog> result = systemLogService.getLogList(page, size, keyword, module, userId, startTime, endTime);
        return Result.success(result);
    }
    
    @DeleteMapping("/logs/clean")
    @Operation(summary = "清理过期日志", description = "清理指定天数之前的日志")
    public Result<Integer> cleanExpiredLogs(
            @Parameter(description = "保留天数") @RequestParam(defaultValue = "90") Integer retentionDays) {
        log.info("清理过期日志请求: retentionDays={}", retentionDays);
        int count = systemLogService.cleanExpiredLogs(retentionDays);
        return Result.success(count);
    }
    
    // ========== 统计数据 ==========
    
    @GetMapping("/statistics")
    @Operation(summary = "获取系统统计数据", description = "获取系统整体统计数据")
    public Result<StatisticsVO> getSystemStatistics() {
        log.info("获取系统统计数据");
        StatisticsVO statistics = statisticsService.getSystemStatistics();
        return Result.success(statistics);
    }
    
    @GetMapping("/statistics/college/{collegeId}")
    @Operation(summary = "获取学院统计数据", description = "获取指定学院的统计数据")
    public Result<StatisticsVO> getCollegeStatistics(
            @Parameter(description = "学院ID") @PathVariable String collegeId) {
        log.info("获取学院统计数据: collegeId={}", collegeId);
        StatisticsVO statistics = statisticsService.getCollegeStatistics(collegeId);
        return Result.success(statistics);
    }
    
    @GetMapping("/statistics/date-range")
    @Operation(summary = "获取时间范围统计数据", description = "获取指定时间范围的统计数据")
    public Result<StatisticsVO> getStatisticsByDateRange(
            @Parameter(description = "开始时间") @RequestParam 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        log.info("获取时间范围统计数据: startTime={}, endTime={}", startTime, endTime);
        StatisticsVO statistics = statisticsService.getStatisticsByDateRange(startTime, endTime);
        return Result.success(statistics);
    }
}