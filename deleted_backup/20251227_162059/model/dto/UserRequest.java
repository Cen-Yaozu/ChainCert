package com.blockchain.certificate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户创建/更新请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    /**
     * 密码（创建时必填，更新时可选）
     */
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;
    
    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50")
    private String name;
    
    /**
     * 角色
     */
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(STUDENT|COLLEGE_TEACHER|SCHOOL_TEACHER|ADMIN)$", 
             message = "角色必须是STUDENT、COLLEGE_TEACHER、SCHOOL_TEACHER或ADMIN")
    private String role;
    
    /**
     * 学院ID（学生和学院老师必填）
     */
    private String collegeId;
    
    /**
     * 学号（学生必填）
     */
    private String studentNo;
    
    /**
     * 学号别名（用于兼容）
     */
    public String getStudentId() {
        return this.studentNo;
    }
    
    public void setStudentId(String studentId) {
        this.studentNo = studentId;
    }
    
    /**
     * 真实姓名别名（用于兼容）
     */
    public String getRealName() {
        return this.name;
    }
    
    public void setRealName(String realName) {
        this.name = realName;
    }
    
    /**
     * 专业ID
     */
    private String majorId;
    
    public String getMajorId() {
        return this.majorId;
    }
    
    public void setMajorId(String majorId) {
        this.majorId = majorId;
    }
    
    /**
     * 工号（老师必填）
     */
    private String employeeNo;
    
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}