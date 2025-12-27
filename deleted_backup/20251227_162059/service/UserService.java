package com.blockchain.certificate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blockchain.certificate.shared.common.PageResult;
import com.blockchain.certificate.shared.exception.BusinessException;
import com.blockchain.certificate.model.dto.PasswordChangeRequest;
import com.blockchain.certificate.model.dto.UserRequest;
import com.blockchain.certificate.model.dto.UserResponse;
import com.blockchain.certificate.domain.user.model.User;
import com.blockchain.certificate.model.vo.UserListVO;
import com.blockchain.certificate.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 创建用户
     */
    @Transactional(rollbackFor = Exception.class)
    public UserResponse createUser(UserRequest request) {
        log.info("创建用户: {}", request.getUsername());
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查学号/工号是否已存在
        if (StringUtils.hasText(request.getStudentId()) && 
            userRepository.existsByStudentId(request.getStudentId())) {
            throw new BusinessException("学号/工号已存在");
        }
        
        // 检查邮箱是否已存在
        if (StringUtils.hasText(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }
        
        // 创建用户实体
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .realName(request.getRealName())
                .studentId(request.getStudentId())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(request.getRole())
                .collegeId(request.getCollegeId())
                .majorId(request.getMajorId())
                .enabled(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        userRepository.save(user);
        log.info("用户创建成功: {}", user.getId());
        
        return convertToResponse(user);
    }
    
    /**
     * 更新用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserResponse updateUser(String userId, UserRequest request) {
        log.info("更新用户信息: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 检查用户名是否被其他用户占用
        if (!user.getUsername().equals(request.getUsername()) && 
            userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查学号/工号是否被其他用户占用
        if (StringUtils.hasText(request.getStudentId()) && 
            !request.getStudentId().equals(user.getStudentId()) &&
            userRepository.existsByStudentId(request.getStudentId())) {
            throw new BusinessException("学号/工号已存在");
        }
        
        // 检查邮箱是否被其他用户占用
        if (StringUtils.hasText(request.getEmail()) && 
            !request.getEmail().equals(user.getEmail()) &&
            userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }
        
        // 更新用户信息
        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setStudentId(request.getStudentId());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setCollegeId(request.getCollegeId());
        user.setMajorId(request.getMajorId());
        user.setUpdateTime(LocalDateTime.now());
        
        // 如果提供了新密码，则更新密码
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        userRepository.updateById(user);
        log.info("用户信息更新成功: {}", userId);
        
        return convertToResponse(user);
    }
    
    /**
     * 删除用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String userId) {
        log.info("删除用户: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 检查是否为系统管理员
        if ("ADMIN".equals(user.getRole())) {
            // 检查是否为最后一个管理员
            long adminCount = userRepository.countByRole("ADMIN");
            if (adminCount <= 1) {
                throw new BusinessException("不能删除最后一个管理员账户");
            }
        }
        
        userRepository.deleteById(userId);
        log.info("用户删除成功: {}", userId);
    }
    
    /**
     * 获取用户详情
     */
    public UserResponse getUserById(String userId) {
        log.info("获取用户详情: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        return convertToResponse(user);
    }
    
    /**
     * 分页查询用户列表
     */
    public PageResult<UserListVO> getUserList(Integer page, Integer size, 
                                              String keyword, String role, 
                                              String collegeId) {
        log.info("分页查询用户列表: page={}, size={}, keyword={}, role={}, collegeId={}", 
                page, size, keyword, role, collegeId);
        
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 关键字搜索（用户名、真实姓名、学号/工号）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getStudentId, keyword));
        }
        
        // 角色筛选
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        
        // 学院筛选
        if (StringUtils.hasText(collegeId)) {
            wrapper.eq(User::getCollegeId, collegeId);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        
        IPage<User> userPage = userRepository.page(pageParam, wrapper);
        
        List<UserListVO> voList = userPage.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());
        
        return PageResult.<UserListVO>builder()
                .records(voList)
                .total(userPage.getTotal())
                .current(userPage.getCurrent())
                .size(userPage.getSize())
                .build();
    }
    
    /**
     * 启用/禁用用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleUserStatus(String userId, Boolean enabled) {
        log.info("切换用户状态: userId={}, enabled={}", userId, enabled);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 检查是否为系统管理员
        if ("ADMIN".equals(user.getRole()) && !enabled) {
            // 检查是否为最后一个启用的管理员
            long enabledAdminCount = userRepository.countByRoleAndEnabled("ADMIN", true);
            if (enabledAdminCount <= 1) {
                throw new BusinessException("不能禁用最后一个管理员账户");
            }
        }
        
        user.setEnabled(enabled);
        user.setUpdateTime(LocalDateTime.now());
        userRepository.updateById(user);
        
        log.info("用户状态切换成功: userId={}, enabled={}", userId, enabled);
    }
    
    /**
     * 重置用户密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String userId, String newPassword) {
        log.info("重置用户密码: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userRepository.updateById(user);
        
        log.info("用户密码重置成功: {}", userId);
    }
    
    /**
     * 修改密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String userId, PasswordChangeRequest request) {
        log.info("修改用户密码: {}", userId);
        
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
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userRepository.updateById(user);
        
        log.info("用户密码修改成功: {}", userId);
    }
    
    /**
     * 批量导入用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchImportUsers(List<UserRequest> userRequests) {
        log.info("批量导入用户: count={}", userRequests.size());
        
        for (UserRequest request : userRequests) {
            try {
                createUser(request);
            } catch (Exception e) {
                log.error("导入用户失败: username={}, error={}", 
                        request.getUsername(), e.getMessage());
                // 继续导入下一个用户
            }
        }
        
        log.info("批量导入用户完成");
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
    
    /**
     * 转换为列表视图对象
     */
    private UserListVO convertToListVO(User user) {
        return UserListVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .studentId(user.getStudentId())
                .email(user.getEmail())
                .role(user.getRole())
                .collegeId(user.getCollegeId())
                .enabled(user.getEnabled())
                .createTime(user.getCreateTime())
                .build();
    }
}