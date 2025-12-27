package com.blockchain.certificate.controller;

import com.blockchain.certificate.shared.common.PageResult;
import com.blockchain.certificate.shared.common.Result;
import com.blockchain.certificate.model.dto.PasswordChangeRequest;
import com.blockchain.certificate.model.dto.UserRequest;
import com.blockchain.certificate.model.dto.UserResponse;
import com.blockchain.certificate.model.vo.UserListVO;
import com.blockchain.certificate.infrastructure.security.UserPrincipal;
import com.blockchain.certificate.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户管理", description = "用户管理相关接口")
public class UserController {
    
    private final UserService userService;
    
    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建用户", description = "管理员创建新用户")
    public Result<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        log.info("创建用户请求: {}", request.getUsername());
        UserResponse response = userService.createUser(request);
        return Result.success(response);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新用户信息", description = "管理员更新用户信息")
    public Result<UserResponse> updateUser(
            @Parameter(description = "用户ID") @PathVariable String userId,
            @Valid @RequestBody UserRequest request) {
        log.info("更新用户信息请求: userId={}", userId);
        UserResponse response = userService.updateUser(userId, request);
        return Result.success(response);
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除用户", description = "管理员删除用户")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable String userId) {
        log.info("删除用户请求: userId={}", userId);
        userService.deleteUser(userId);
        return Result.success();
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "获取用户详情", description = "获取指定用户的详细信息")
    public Result<UserResponse> getUserById(
            @Parameter(description = "用户ID") @PathVariable String userId) {
        log.info("获取用户详情请求: userId={}", userId);
        UserResponse response = userService.getUserById(userId);
        return Result.success(response);
    }
    
    /**
     * 分页查询用户列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "分页查询用户列表", description = "分页查询用户列表，支持关键字搜索和筛选")
    public Result<PageResult<UserListVO>> getUserList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "角色") @RequestParam(required = false) String role,
            @Parameter(description = "学院ID") @RequestParam(required = false) String collegeId) {
        log.info("分页查询用户列表: page={}, size={}, keyword={}, role={}, collegeId={}", 
                page, size, keyword, role, collegeId);
        PageResult<UserListVO> result = userService.getUserList(page, size, keyword, role, collegeId);
        return Result.success(result);
    }
    
    /**
     * 启用/禁用用户
     */
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "启用/禁用用户", description = "管理员启用或禁用用户账户")
    public Result<Void> toggleUserStatus(
            @Parameter(description = "用户ID") @PathVariable String userId,
            @Parameter(description = "是否启用") @RequestParam Boolean enabled) {
        log.info("切换用户状态请求: userId={}, enabled={}", userId, enabled);
        userService.toggleUserStatus(userId, enabled);
        return Result.success();
    }
    
    /**
     * 重置用户密码
     */
    @PutMapping("/{userId}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "重置用户密码", description = "管理员重置用户密码为默认密码")
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID") @PathVariable String userId,
            @Parameter(description = "新密码") @RequestParam @NotBlank String newPassword) {
        log.info("重置用户密码请求: userId={}", userId);
        userService.resetPassword(userId, newPassword);
        return Result.success();
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    @Operation(summary = "修改密码", description = "用户修改自己的密码")
    public Result<Void> changePassword(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody PasswordChangeRequest request) {
        log.info("修改密码请求: userId={}", currentUser.getId());
        userService.changePassword(currentUser.getId(), request);
        return Result.success();
    }
    
    /**
     * 批量导入用户
     */
    @PostMapping("/batch-import")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量导入用户", description = "管理员批量导入用户")
    public Result<Void> batchImportUsers(@Valid @RequestBody List<UserRequest> userRequests) {
        log.info("批量导入用户请求: count={}", userRequests.size());
        userService.batchImportUsers(userRequests);
        return Result.success();
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public Result<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("获取当前用户信息: userId={}", currentUser.getId());
        UserResponse response = userService.getUserById(currentUser.getId());
        return Result.success(response);
    }
}