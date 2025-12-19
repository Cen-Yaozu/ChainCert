package com.blockchain.certificate.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求 DTO
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "验证码不能为空")
    private String captcha;
    
    @NotBlank(message = "验证码Key不能为空")
    private String captchaKey;
    
    /**
     * 记住密码
     */
    private Boolean rememberMe = false;
}
