package com.blockchain.certificate.controller;

import com.blockchain.certificate.shared.common.Result;
import com.blockchain.certificate.model.dto.PasswordChangeRequest;
import com.blockchain.certificate.model.dto.UserResponse;
import com.blockchain.certificate.infrastructure.security.UserPrincipal;
import com.blockchain.certificate.domain.user.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 个人中心控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Validated
@Tag(name = "个人中心", description = "个人中心相关接口")
public class ProfileController {
    
    private final ProfileService profileService;
    
    /**
     * 获取个人信息
     */
    @GetMapping
    @Operation(summary = "获取个人信息", description = "获取当前登录用户的个人信息")
    public Result<UserResponse> getProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("获取个人信息: userId={}", currentUser.getId());
        UserResponse response = profileService.getProfile(currentUser.getId());
        return Result.success(response);
    }
    
    /**
     * 更新个人信息
     */
    @PutMapping
    @Operation(summary = "更新个人信息", description = "更新当前登录用户的个人信息")
    public Result<UserResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody UserResponse request) {
        log.info("更新个人信息: userId={}", currentUser.getId());
        UserResponse response = profileService.updateProfile(currentUser.getId(), request);
        return Result.success(response);
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    public Result<Void> changePassword(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody PasswordChangeRequest request) {
        log.info("修改密码: userId={}", currentUser.getId());
        profileService.changePassword(currentUser.getId(), request);
        return Result.success();
    }
    
    /**
     * 更新头像
     */
    @PutMapping("/avatar")
    @Operation(summary = "更新头像", description = "更新当前登录用户的头像")
    public Result<Void> updateAvatar(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "头像URL") @RequestParam String avatarUrl) {
        log.info("更新头像: userId={}", currentUser.getId());
        profileService.updateAvatar(currentUser.getId(), avatarUrl);
        return Result.success();
    }
}