package com.blockchain.certificate.model.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 注册请求 DTO
 */
@Data
public class RegisterRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;
    
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
    
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String name;
    
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(STUDENT|COLLEGE_ADMIN|SCHOOL_ADMIN|SYSTEM_ADMIN)$", message = "角色类型无效")
    private String role;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 学院ID（学生和学院管理员必填）
     */
    private String collegeId;
    
    /**
     * 专业ID（学生必填）
     */
    private String majorId;
    
    /**
     * 学号（学生必填）
     */
    private String studentNo;
    
    /**
     * 工号（管理员可选）
     */
    private String employeeNo;
    
    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String captcha;
    
    /**
     * 验证码Key
     */
    @NotBlank(message = "验证码Key不能为空")
    private String captchaKey;
}