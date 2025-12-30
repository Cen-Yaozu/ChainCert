package com.blockchain.certificate.domain.organization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blockchain.certificate.shared.common.PageResult;
import com.blockchain.certificate.shared.exception.BusinessException;
import com.blockchain.certificate.model.dto.MajorRequest;
import com.blockchain.certificate.model.dto.MajorResponse;
import com.blockchain.certificate.domain.organization.model.College;
import com.blockchain.certificate.domain.organization.model.Major;
import com.blockchain.certificate.domain.user.model.User;
import com.blockchain.certificate.domain.organization.repository.CollegeRepository;
import com.blockchain.certificate.domain.organization.repository.MajorRepository;
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
 * 专业管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MajorService {
    
    private final MajorRepository majorRepository;
    private final CollegeRepository collegeRepository;
    private final UserRepository userRepository;
    
    /**
     * 创建专业
     */
    @Transactional(rollbackFor = Exception.class)
    public MajorResponse createMajor(MajorRequest request) {
        log.info("创建专业: {}", request.getName());
        
        // 验证学院是否存在
        Long collegeIdLong = Long.parseLong(request.getCollegeId());
        College college = collegeRepository.selectById(collegeIdLong);
        if (college == null) {
            throw new BusinessException("所属学院不存在");
        }
        
        // 检查专业名称在该学院下是否已存在
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Major::getName, request.getName());
        wrapper.eq(Major::getCollegeId, collegeIdLong);
        if (majorRepository.selectCount(wrapper) > 0) {
            throw new BusinessException("该学院下已存在同名专业");
        }
        
        // 检查专业代码是否已存在
        if (StringUtils.hasText(request.getCode())) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Major::getCode, request.getCode());
            if (majorRepository.selectCount(wrapper) > 0) {
                throw new BusinessException("专业代码已存在");
            }
        }
        
        // 创建专业实体
        Major major = Major.builder()
                .name(request.getName())
                .code(request.getCode())
                .collegeId(collegeIdLong)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        majorRepository.insert(major);
        log.info("专业创建成功: {}", major.getId());
        
        return convertToResponse(major);
    }
    
    /**
     * 更新专业信息
     */
    @Transactional(rollbackFor = Exception.class)
    public MajorResponse updateMajor(String majorId, MajorRequest request) {
        log.info("更新专业信息: {}", majorId);
        
        Long majorIdLong = Long.parseLong(majorId);
        Major major = majorRepository.selectById(majorIdLong);
        if (major == null) {
            throw new BusinessException("专业不存在");
        }
        
        // 验证学院是否存在
        Long collegeIdLong = Long.parseLong(request.getCollegeId());
        College college = collegeRepository.selectById(collegeIdLong);
        if (college == null) {
            throw new BusinessException("所属学院不存在");
        }
        
        // 检查专业名称在该学院下是否被其他专业占用
        if (!major.getName().equals(request.getName()) || !major.getCollegeId().equals(collegeIdLong)) {
            LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Major::getName, request.getName());
            wrapper.eq(Major::getCollegeId, collegeIdLong);
            wrapper.ne(Major::getId, majorIdLong);
            if (majorRepository.selectCount(wrapper) > 0) {
                throw new BusinessException("该学院下已存在同名专业");
            }
        }
        
        // 检查专业代码是否被其他专业占用
        if (StringUtils.hasText(request.getCode()) && !request.getCode().equals(major.getCode())) {
            LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Major::getCode, request.getCode());
            wrapper.ne(Major::getId, majorIdLong);
            if (majorRepository.selectCount(wrapper) > 0) {
                throw new BusinessException("专业代码已存在");
            }
        }
        
        // 更新专业信息
        major.setName(request.getName());
        major.setCode(request.getCode());
        major.setCollegeId(collegeIdLong);
        major.setUpdateTime(LocalDateTime.now());
        
        majorRepository.updateById(major);
        log.info("专业信息更新成功: {}", majorId);
        
        return convertToResponse(major);
    }
    
    /**
     * 删除专业
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMajor(String majorId) {
        log.info("删除专业: {}", majorId);
        
        Long majorIdLong = Long.parseLong(majorId);
        Major major = majorRepository.selectById(majorIdLong);
        if (major == null) {
            throw new BusinessException("专业不存在");
        }
        
        // 检查是否有用户关联该专业
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getMajorId, majorIdLong);
        long userCount = userRepository.selectCount(userWrapper);
        if (userCount > 0) {
            throw new BusinessException("该专业下还有用户，无法删除");
        }
        
        majorRepository.deleteById(majorIdLong);
        log.info("专业删除成功: {}", majorId);
    }
    
    /**
     * 获取专业详情
     */
    public MajorResponse getMajorById(String majorId) {
        log.info("获取专业详情: {}", majorId);
        
        Long majorIdLong = Long.parseLong(majorId);
        Major major = majorRepository.selectById(majorIdLong);
        if (major == null) {
            throw new BusinessException("专业不存在");
        }
        
        return convertToResponse(major);
    }
    
    /**
     * 分页查询专业列表
     */
    public PageResult<MajorResponse> getMajorList(Integer page, Integer size, 
                                                   String keyword, String collegeId) {
        log.info("分页查询专业列表: page={}, size={}, keyword={}, collegeId={}", 
                page, size, keyword, collegeId);
        
        Page<Major> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        
        // 学院筛选
        if (StringUtils.hasText(collegeId)) {
            wrapper.eq(Major::getCollegeId, Long.parseLong(collegeId));
        }
        
        // 关键字搜索（专业名称、专业代码）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Major::getName, keyword)
                    .or().like(Major::getCode, keyword));
        }
        
        wrapper.orderByDesc(Major::getCreateTime);
        
        IPage<Major> majorPage = majorRepository.selectPage(pageParam, wrapper);
        
        List<MajorResponse> responseList = majorPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return PageResult.<MajorResponse>builder()
                .records(responseList)
                .total(majorPage.getTotal())
                .current(majorPage.getCurrent())
                .size(majorPage.getSize())
                .build();
    }
    
    /**
     * 获取指定学院的所有专业（不分页）
     */
    public List<MajorResponse> getMajorsByCollege(String collegeId) {
        log.info("获取学院的所有专业: collegeId={}", collegeId);
        
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Major::getCollegeId, Long.parseLong(collegeId));
        wrapper.orderByAsc(Major::getName);
        
        List<Major> majors = majorRepository.selectList(wrapper);
        
        return majors.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有专业列表（不分页）
     */
    public List<MajorResponse> getAllMajors() {
        log.info("获取所有专业列表");
        
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Major::getName);
        
        List<Major> majors = majorRepository.selectList(wrapper);
        
        return majors.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换为响应对象
     */
    private MajorResponse convertToResponse(Major major) {
        MajorResponse response = MajorResponse.builder()
                .id(String.valueOf(major.getId()))
                .name(major.getName())
                .code(major.getCode())
                .collegeId(major.getCollegeId() != null ? String.valueOf(major.getCollegeId()) : null)
                .createTime(major.getCreateTime())
                .updateTime(major.getUpdateTime())
                .build();
        
        // 获取学院名称
        if (major.getCollegeId() != null) {
            College college = collegeRepository.selectById(major.getCollegeId());
            if (college != null) {
                response.setCollegeName(college.getName());
            }
        }
        
        return response;
    }
}