package com.blockchain.certificate.domain.user.service;

import com.blockchain.certificate.shared.exception.BusinessException;
import com.blockchain.certificate.model.dto.PasswordChangeRequest;
import com.blockchain.certificate.model.dto.UserResponse;
import com.blockchain.certificate.domain.user.model.User;
import com.blockchain.certificate.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 个人中心服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 获取个人信息
     */
    public UserResponse getProfile(String userId) {
        log.info("获取个人信息: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        return convertToResponse(user);
    }
    
    /**
     * 更新个人信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserResponse updateProfile(String userId, UserResponse request) {
        log.info("更新个人信息: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 检查邮箱是否被其他用户占用
        if (StringUtils.hasText(request.getEmail()) && 
            !request.getEmail().equals(user.getEmail()) &&
            userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被使用");
        }
        
        // 更新允许修改的字段
        if (StringUtils.hasText(request.getRealName())) {
            user.setRealName(request.getRealName());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        
        user.setUpdateTime(LocalDateTime.now());
        userRepository.updateById(user);
        
        log.info("个人信息更新成功: userId={}", userId);
        return convertToResponse(user);
    }
    
    /**
     * 修改密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String userId, PasswordChangeRequest request) {
        log.info("修改密码: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        // 验证新密码和确认密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }
        
        // 验证新密码不能与旧密码相同
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userRepository.updateById(user);
        
        log.info("密码修改成功: userId={}", userId);
    }
    
    /**
     * 更新头像
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAvatar(String userId, String avatarUrl) {
        log.info("更新头像: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        user.setUpdateTime(LocalDateTime.now());
        userRepository.updateById(user);
        
        log.info("头像更新成功: userId={}", userId);
    }
    
    /**
     * 转换为响应对象
     */
    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .studentId(user.getStudentId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .collegeId(user.getCollegeId())
                .majorId(user.getMajorId())
                .enabled(user.getEnabled())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }
}