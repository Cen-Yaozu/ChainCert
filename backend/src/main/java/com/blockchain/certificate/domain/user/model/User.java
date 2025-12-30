package com.blockchain.certificate.domain.user.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user")
public class User {
    
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名（唯一）
     */
    private String username;
    
    /**
     * 密码（BCrypt加密）
     */
    private String password;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 角色：STUDENT, COLLEGE_TEACHER, SCHOOL_TEACHER, ADMIN
     */
    private String role;
    
    /**
     * 学院ID
     */
    private Long collegeId;
    
    /**
     * 专业ID
     */
    private Long majorId;
    
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
     * 私钥（AES加密存储）
     */
    private String privateKey;
    
    /**
     * 账户状态：ACTIVE, LOCKED, DISABLED
     */
    private String status;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 登录失败次数
     */
    private Integer failedLoginCount;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标记（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;
    
    /**
     * 真实姓名（别名，用于兼容）
     */
    public String getRealName() {
        return this.name;
    }
    
    public void setRealName(String realName) {
        this.name = realName;
    }
    
    /**
     * 学号（别名，用于兼容）
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
    public static UserBuilder builder() {
        return new UserBuilder();
    }
    
    public static class UserBuilder {
        private Long id;
        private String username;
        private String password;
        private String name;
        private String role;
        private Long collegeId;
        private Long majorId;
        private String studentNo;
        private String employeeNo;
        private String email;
        private String phone;
        private String privateKey;
        private String status;
        private Boolean enabled;
        private Integer failedLoginCount;
        private LocalDateTime lastLoginTime;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private Integer deleted;
        
        UserBuilder() {
        }
        
        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }
        
        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }
        
        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public UserBuilder realName(String realName) {
            this.name = realName;
            return this;
        }
        
        public UserBuilder role(String role) {
            this.role = role;
            return this;
        }
        
        public UserBuilder collegeId(Long collegeId) {
            this.collegeId = collegeId;
            return this;
        }
        
        public UserBuilder majorId(Long majorId) {
            this.majorId = majorId;
            return this;
        }
        
        public UserBuilder studentNo(String studentNo) {
            this.studentNo = studentNo;
            return this;
        }
        
        public UserBuilder studentId(String studentId) {
            this.studentNo = studentId;
            return this;
        }
        
        public UserBuilder employeeNo(String employeeNo) {
            this.employeeNo = employeeNo;
            return this;
        }
        
        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UserBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public UserBuilder privateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }
        
        public UserBuilder status(String status) {
            this.status = status;
            return this;
        }
        
        public UserBuilder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public UserBuilder failedLoginCount(Integer failedLoginCount) {
            this.failedLoginCount = failedLoginCount;
            return this;
        }
        
        public UserBuilder lastLoginTime(LocalDateTime lastLoginTime) {
            this.lastLoginTime = lastLoginTime;
            return this;
        }
        
        public UserBuilder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }
        
        public UserBuilder updateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }
        
        public UserBuilder deleted(Integer deleted) {
            this.deleted = deleted;
            return this;
        }
        
        public User build() {
            User user = new User();
            user.id = this.id;
            user.username = this.username;
            user.password = this.password;
            user.name = this.name;
            user.role = this.role;
            user.collegeId = this.collegeId;
            user.majorId = this.majorId;
            user.studentNo = this.studentNo;
            user.employeeNo = this.employeeNo;
            user.email = this.email;
            user.phone = this.phone;
            user.privateKey = this.privateKey;
            user.status = this.status;
            user.enabled = this.enabled;
            user.failedLoginCount = this.failedLoginCount;
            user.lastLoginTime = this.lastLoginTime;
            user.createTime = this.createTime;
            user.updateTime = this.updateTime;
            user.deleted = this.deleted;
            return user;
        }
    }
}
