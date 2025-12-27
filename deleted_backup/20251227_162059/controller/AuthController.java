package com.blockchain.certificate.controller;

import com.blockchain.certificate.shared.common.Result;
import com.blockchain.certificate.model.dto.LoginRequest;
import com.blockchain.certificate.model.dto.RefreshTokenRequest;
import com.blockchain.certificate.model.vo.LoginResponse;
import com.blockchain.certificate.domain.user.service.AuthService;
import com.blockchain.certificate.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理登录、登出、验证码、令牌刷新等认证相关请求
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应（包含令牌和用户信息）
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        log.info("User login attempt: {}", request.getUsername());
        
        LoginResponse response = authService.login(request);
        
        log.info("User logged in successfully: {}", request.getUsername());
        return Result.success(response);
    }

    /**
     * 获取验证码
     *
     * @return 验证码数据（key 和 base64 图片）
     */
    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        CaptchaService.CaptchaResult captcha = captchaService.generateCaptcha();
        
        Map<String, String> result = new HashMap<>();
        result.put("key", captcha.getKey());
        result.put("image", captcha.getImage());
        
        return Result.success(result);
    }

    /**
     * 刷新访问令牌
     *
     * @param request 刷新令牌请求
     * @return 新的访问令牌
     */
    @PostMapping("/refresh")
    public Result<Map<String, String>> refreshToken(@Validated @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh attempt");
        
        String newAccessToken = authService.refreshToken(request);
        
        Map<String, String> result = new HashMap<>();
        result.put("token", newAccessToken);
        
        log.info("Token refreshed successfully");
        return Result.success(result);
    }

    /**
     * 用户登出
     *
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            authService.logout(username);
            log.info("User logged out: {}", username);
        }
        
        Result<Void> result = Result.<Void>success();
        result.setMessage("登出成功");
        return result;
    }
}
