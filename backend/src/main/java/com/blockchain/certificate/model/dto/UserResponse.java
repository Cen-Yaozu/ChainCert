package com.blockchain.certificate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    /**
     * 用户ID
     */
    private String id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 角色
     */
    private String role;
    
    /**
     * 角色描述
     */
    private String roleDesc;
    
    /**
     * 学院ID
     */
    private String collegeId;
    
    /**
     * 学院名称
     */
    private String collegeName;
    
    /**
     * 专业ID
     */
    private String majorId;
    
    /**
     * 专业名称
     */
    private String majorName;
    
    /**
     * 学号
     */
    private String studentNo;
    
    /**
     * 工号
     */
    private String employeeNo;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
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
     * 学生ID别名（用于兼容）
     */
    public String getStudentId() {
        return this.studentNo;
    }
    
    public void setStudentId(String studentId) {
        this.studentNo = studentId;
    }
    
    /**
     * 自定义Builder，支持别名字段
     */
    public static UserResponseBuilder builder() {
        return new UserResponseBuilder();
    }
    
    public static class UserResponseBuilder {
        private String id;
        private String username;
        private String name;
        private String role;
        private String roleDesc;
        private String collegeId;
        private String collegeName;
        private String majorId;
        private String majorName;
        private String studentNo;
        private String employeeNo;
        private String email;
        private String phone;
        private String status;
        private String statusDesc;
        private Boolean enabled;
        private LocalDateTime lastLoginTime;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        
        UserResponseBuilder() {
        }
        
        public UserResponseBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public UserResponseBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public UserResponseBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public UserResponseBuilder realName(String realName) {
            this.name = realName;
            return this;
        }
        
        public UserResponseBuilder role(String role) {
            this.role = role;
            return this;
        }
        
        public UserResponseBuilder roleDesc(String roleDesc) {
            this.roleDesc = roleDesc;
            return this;
        }
        
        public UserResponseBuilder collegeId(String collegeId) {
            this.collegeId = collegeId;
            return this;
        }
        
        public UserResponseBuilder collegeName(String collegeName) {
            this.collegeName = collegeName;
            return this;
        }
        
        public UserResponseBuilder majorId(String majorId) {
            this.majorId = majorId;
            return this;
        }
        
        public UserResponseBuilder majorName(String majorName) {
            this.majorName = majorName;
            return this;
        }
        
        public UserResponseBuilder studentNo(String studentNo) {
            this.studentNo = studentNo;
            return this;
        }
        
        public UserResponseBuilder studentId(String studentId) {
            this.studentNo = studentId;
            return this;
        }
        
        public UserResponseBuilder employeeNo(String employeeNo) {
            this.employeeNo = employeeNo;
            return this;
        }
        
        public UserResponseBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UserResponseBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public UserResponseBuilder status(String status) {
            this.status = status;
            return this;
        }
        
        public UserResponseBuilder statusDesc(String statusDesc) {
            this.statusDesc = statusDesc;
            return this;
        }
        
        public UserResponseBuilder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public UserResponseBuilder lastLoginTime(LocalDateTime lastLoginTime) {
            this.lastLoginTime = lastLoginTime;
            return this;
        }
        
        public UserResponseBuilder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }
        
        public UserResponseBuilder updateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }
        
        public UserResponse build() {
            UserResponse response = new UserResponse();
            response.id = this.id;
            response.username = this.username;
            response.name = this.name;
            response.role = this.role;
            response.roleDesc = this.roleDesc;
            response.collegeId = this.collegeId;
            response.collegeName = this.collegeName;
            response.majorId = this.majorId;
            response.majorName = this.majorName;
            response.studentNo = this.studentNo;
            response.employeeNo = this.employeeNo;
            response.email = this.email;
            response.phone = this.phone;
            response.status = this.status;
            response.statusDesc = this.statusDesc;
            response.enabled = this.enabled;
            response.lastLoginTime = this.lastLoginTime;
            response.createTime = this.createTime;
            response.updateTime = this.updateTime;
            return response;
        }
    }
}