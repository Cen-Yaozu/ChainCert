package com.blockchain.certificate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blockchain.certificate.shared.common.PageResult;
import com.blockchain.certificate.shared.exception.BusinessException;
import com.blockchain.certificate.model.dto.CollegeRequest;
import com.blockchain.certificate.model.dto.CollegeResponse;
import com.blockchain.certificate.domain.organization.model.College;
import com.blockchain.certificate.domain.user.model.User;
import com.blockchain.certificate.domain.organization.repository.CollegeRepository;
import com.blockchain.certificate.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学院管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollegeService {
    
    private final CollegeRepository collegeRepository;
    private final UserRepository userRepository;
    
    /**
     * 创建学院
     */
    @Transactional(rollbackFor = Exception.class)
    public CollegeResponse createCollege(CollegeRequest request) {
        log.info("创建学院: {}", request.getName());
        
        // 检查学院名称是否已存在
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(College::getName, request.getName());
        if (collegeRepository.selectCount(wrapper) > 0) {
            throw new BusinessException("学院名称已存在");
        }
        
        // 检查学院代码是否已存在
        if (StringUtils.hasText(request.getCode())) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(College::getCode, request.getCode());
            if (collegeRepository.selectCount(wrapper) > 0) {
                throw new BusinessException("学院代码已存在");
            }
        }
        
        // 验证审批人是否存在
        if (StringUtils.hasText(request.getApproverId())) {
            User approver = userRepository.selectById(request.getApproverId());
            if (approver == null) {
                throw new BusinessException("审批人不存在");
            }
            if (!"TEACHER".equals(approver.getRole()) && !"ADMIN".equals(approver.getRole())) {
                throw new BusinessException("审批人必须是教师或管理员");
            }
        }
        
        // 创建学院实体
        College college = College.builder()
                .name(request.getName())
                .code(request.getCode())
                .approverId(request.getApproverId())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        collegeRepository.insert(college);
        log.info("学院创建成功: {}", college.getId());
        
        return convertToResponse(college);
    }
    
    /**
     * 更新学院信息
     */
    @Transactional(rollbackFor = Exception.class)
    public CollegeResponse updateCollege(String collegeId, CollegeRequest request) {
        log.info("更新学院信息: {}", collegeId);
        
        College college = collegeRepository.selectById(collegeId);
        if (college == null) {
            throw new BusinessException("学院不存在");
        }
        
        // 检查学院名称是否被其他学院占用
        if (!college.getName().equals(request.getName())) {
            LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(College::getName, request.getName());
            wrapper.ne(College::getId, collegeId);
            if (collegeRepository.selectCount(wrapper) > 0) {
                throw new BusinessException("学院名称已存在");
            }
        }
        
        // 检查学院代码是否被其他学院占用
        if (StringUtils.hasText(request.getCode()) && !request.getCode().equals(college.getCode())) {
            LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(College::getCode, request.getCode());
            wrapper.ne(College::getId, collegeId);
            if (collegeRepository.selectCount(wrapper) > 0) {
                throw new BusinessException("学院代码已存在");
            }
        }
        
        // 验证审批人是否存在
        if (StringUtils.hasText(request.getApproverId())) {
            User approver = userRepository.selectById(request.getApproverId());
            if (approver == null) {
                throw new BusinessException("审批人不存在");
            }
            if (!"TEACHER".equals(approver.getRole()) && !"ADMIN".equals(approver.getRole())) {
                throw new BusinessException("审批人必须是教师或管理员");
            }
        }
        
        // 更新学院信息
        college.setName(request.getName());
        college.setCode(request.getCode());
        college.setApproverId(request.getApproverId());
        college.setUpdateTime(LocalDateTime.now());
        
        collegeRepository.updateById(college);
        log.info("学院信息更新成功: {}", collegeId);
        
        return convertToResponse(college);
    }
    
    /**
     * 删除学院
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCollege(String collegeId) {
        log.info("删除学院: {}", collegeId);
        
        College college = collegeRepository.selectById(collegeId);
        if (college == null) {
            throw new BusinessException("学院不存在");
        }
        
        // 检查是否有用户关联该学院
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getCollegeId, collegeId);
        long userCount = userRepository.selectCount(userWrapper);
        if (userCount > 0) {
            throw new BusinessException("该学院下还有用户，无法删除");
        }
        
        collegeRepository.deleteById(collegeId);
        log.info("学院删除成功: {}", collegeId);
    }
    
    /**
     * 获取学院详情
     */
    public CollegeResponse getCollegeById(String collegeId) {
        log.info("获取学院详情: {}", collegeId);
        
        College college = collegeRepository.selectById(collegeId);
        if (college == null) {
            throw new BusinessException("学院不存在");
        }
        
        return convertToResponse(college);
    }
    
    /**
     * 分页查询学院列表
     */
    public PageResult<CollegeResponse> getCollegeList(Integer page, Integer size, String keyword) {
        log.info("分页查询学院列表: page={}, size={}, keyword={}", page, size, keyword);
        
        Page<College> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
        
        // 关键字搜索（学院名称、学院代码）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(College::getName, keyword)
                    .or().like(College::getCode, keyword));
        }
        
        wrapper.orderByDesc(College::getCreateTime);
        
        IPage<College> collegePage = collegeRepository.selectPage(pageParam, wrapper);
        
        List<CollegeResponse> responseList = collegePage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return PageResult.<CollegeResponse>builder()
                .records(responseList)
                .total(collegePage.getTotal())
                .current(collegePage.getCurrent())
                .size(collegePage.getSize())
                .build();
    }
    
    /**
     * 获取所有学院列表（不分页）
     */
    public List<CollegeResponse> getAllColleges() {
        log.info("获取所有学院列表");
        
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(College::getName);
        
        List<College> colleges = collegeRepository.selectList(wrapper);
        
        return colleges.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 分配审批人
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignApprover(String collegeId, String approverId) {
        log.info("分配审批人: collegeId={}, approverId={}", collegeId, approverId);
        
        College college = collegeRepository.selectById(collegeId);
        if (college == null) {
            throw new BusinessException("学院不存在");
        }
        
        // 验证审批人是否存在
        User approver = userRepository.selectById(approverId);
        if (approver == null) {
            throw new BusinessException("审批人不存在");
        }
        if (!"TEACHER".equals(approver.getRole()) && !"ADMIN".equals(approver.getRole())) {
            throw new BusinessException("审批人必须是教师或管理员");
        }
        
        college.setApproverId(approverId);
        college.setUpdateTime(LocalDateTime.now());
        collegeRepository.updateById(college);
        
        log.info("审批人分配成功: collegeId={}, approverId={}", collegeId, approverId);
    }
    
    /**
     * 转换为响应对象
     */
    private CollegeResponse convertToResponse(College college) {
        CollegeResponse response = CollegeResponse.builder()
                .id(college.getId())
                .name(college.getName())
                .code(college.getCode())
                .approverId(college.getApproverId())
                .createTime(college.getCreateTime())
                .updateTime(college.getUpdateTime())
                .build();
        
        // 获取审批人姓名
        if (StringUtils.hasText(college.getApproverId())) {
            User approver = userRepository.selectById(college.getApproverId());
            if (approver != null) {
                response.setApproverName(approver.getRealName());
            }
        }
        
        return response;
    }
}