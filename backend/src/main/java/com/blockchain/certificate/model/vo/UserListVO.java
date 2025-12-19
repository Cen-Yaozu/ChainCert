package com.blockchain.certificate.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户列表视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListVO {
    
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
     * 学院名称
     */
    private String collegeName;
    
    /**
     * 学号/工号
     */
    private String idNumber;
    
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
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 自定义Builder，支持realName别名
     */
    public static UserListVOBuilder builder() {
        return new UserListVOBuilder();
    }
    
    public static class UserListVOBuilder {
        private String id;
        private String username;
        private String name;
        private String role;
        private String roleDesc;
        private String collegeName;
        private String idNumber;
        private String email;
        private String phone;
        private String status;
        private String statusDesc;
        private LocalDateTime lastLoginTime;
        private LocalDateTime createTime;
        
        UserListVOBuilder() {
        }
        
        public UserListVOBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public UserListVOBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public UserListVOBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public UserListVOBuilder realName(String realName) {
            this.name = realName;
            return this;
        }
        
        public UserListVOBuilder role(String role) {
            this.role = role;
            return this;
        }
        
        public UserListVOBuilder roleDesc(String roleDesc) {
            this.roleDesc = roleDesc;
            return this;
        }
        
        public UserListVOBuilder collegeName(String collegeName) {
            this.collegeName = collegeName;
            return this;
        }
        
        public UserListVOBuilder collegeId(String collegeId) {
            // UserListVO没有collegeId字段，忽略此设置
            return this;
        }
        
        public UserListVOBuilder enabled(Boolean enabled) {
            // UserListVO没有enabled字段，忽略此设置
            return this;
        }
        
        public UserListVOBuilder idNumber(String idNumber) {
            this.idNumber = idNumber;
            return this;
        }
        
        public UserListVOBuilder studentId(String studentId) {
            this.idNumber = studentId;
            return this;
        }
        
        public UserListVOBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UserListVOBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public UserListVOBuilder status(String status) {
            this.status = status;
            return this;
        }
        
        public UserListVOBuilder statusDesc(String statusDesc) {
            this.statusDesc = statusDesc;
            return this;
        }
        
        public UserListVOBuilder lastLoginTime(LocalDateTime lastLoginTime) {
            this.lastLoginTime = lastLoginTime;
            return this;
        }
        
        public UserListVOBuilder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }
        
        public UserListVO build() {
            UserListVO vo = new UserListVO();
            vo.id = this.id;
            vo.username = this.username;
            vo.name = this.name;
            vo.role = this.role;
            vo.roleDesc = this.roleDesc;
            vo.collegeName = this.collegeName;
            vo.idNumber = this.idNumber;
            vo.email = this.email;
            vo.phone = this.phone;
            vo.status = this.status;
            vo.statusDesc = this.statusDesc;
            vo.lastLoginTime = this.lastLoginTime;
            vo.createTime = this.createTime;
            return vo;
        }
    }
}