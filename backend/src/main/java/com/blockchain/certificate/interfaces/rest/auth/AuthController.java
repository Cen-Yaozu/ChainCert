package com.blockchain.certificate.interfaces.rest.auth;

import com.blockchain.certificate.shared.common.Result;
import com.blockchain.certificate.model.dto.LoginRequest;
import com.blockchain.certificate.model.dto.RefreshTokenRequest;
import com.blockchain.certificate.model.dto.RegisterRequest;
import com.blockchain.certificate.model.vo.LoginResponse;
import com.blockchain.certificate.domain.user.service.AuthService;
import com.blockchain.certificate.infrastructure.security.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Validated @RequestBody RegisterRequest request) {
        log.info("User registration attempt: {}", request.getUsername());
        
        String userId = authService.register(request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("username", request.getUsername());
        result.put("message", "注册成功");
        
        log.info("User registered successfully: {}", request.getUsername());
        return Result.success(result);
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

    /**
     * 开发模式：初始化用户（跳过验证码）
     * 仅用于开发环境初始化账号
     *
     * @param request 注册请求（不需要验证码字段）
     * @return 注册结果
     */
    @PostMapping("/init-user")
    public Result<Map<String, Object>> initUser(@RequestBody RegisterRequest request) {
        log.warn("DEV MODE: Initializing user without captcha: {}", request.getUsername());
        
        // 跳过验证码验证
        String userId = authService.register(request, false);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("username", request.getUsername());
        result.put("message", "用户初始化成功");
        
        log.info("User initialized successfully: {}", request.getUsername());
        return Result.success(result);
    }
}
