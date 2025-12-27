package com.blockchain.certificate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blockchain.certificate.shared.common.PageResult;
import com.blockchain.certificate.shared.exception.BusinessException;
import com.blockchain.certificate.model.dto.TemplateRequest;
import com.blockchain.certificate.model.dto.TemplateResponse;
import com.blockchain.certificate.domain.certificate.model.CertificateTemplate;
import com.blockchain.certificate.domain.certificate.repository.CertificateTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 证书模板管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {
    
    private final CertificateTemplateRepository templateRepository;
    
    /**
     * 创建证书模板
     */
    @Transactional(rollbackFor = Exception.class)
    public TemplateResponse createTemplate(TemplateRequest request) {
        log.info("创建证书模板: {}", request.getName());
        
        // 检查模板名称是否已存在
        LambdaQueryWrapper<CertificateTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CertificateTemplate::getName, request.getName());
        if (templateRepository.selectCount(wrapper) > 0) {
            throw new BusinessException("模板名称已存在");
        }
        
        // 创建模板实体
        CertificateTemplate template = CertificateTemplate.builder()
                .name(request.getName())
                .type(request.getType())
                .content(request.getContent())
                .description(request.getDescription())
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        templateRepository.insert(template);
        log.info("证书模板创建成功: {}", template.getId());
        
        return convertToResponse(template);
    }
    
    /**
     * 更新证书模板
     */
    @Transactional(rollbackFor = Exception.class)
    public TemplateResponse updateTemplate(String templateId, TemplateRequest request) {
        log.info("更新证书模板: {}", templateId);
        
        CertificateTemplate template = templateRepository.selectById(templateId);
        if (template == null) {
            throw new BusinessException("证书模板不存在");
        }
        
        // 检查模板名称是否被其他模板占用
        if (!template.getName().equals(request.getName())) {
            LambdaQueryWrapper<CertificateTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CertificateTemplate::getName, request.getName());
            wrapper.ne(CertificateTemplate::getId, templateId);
            if (templateRepository.selectCount(wrapper) > 0) {
                throw new BusinessException("模板名称已存在");
            }
        }
        
        // 更新模板信息
        template.setName(request.getName());
        template.setType(request.getType());
        template.setContent(request.getContent());
        template.setDescription(request.getDescription());
        if (request.getEnabled() != null) {
            template.setEnabled(request.getEnabled());
        }
        template.setUpdateTime(LocalDateTime.now());
        
        templateRepository.updateById(template);
        log.info("证书模板更新成功: {}", templateId);
        
        return convertToResponse(template);
    }
    
    /**
     * 删除证书模板
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(String templateId) {
        log.info("删除证书模板: {}", templateId);
        
        CertificateTemplate template = templateRepository.selectById(templateId);
        if (template == null) {
            throw new BusinessException("证书模板不存在");
        }
        
        templateRepository.deleteById(templateId);
        log.info("证书模板删除成功: {}", templateId);
    }
    
    /**
     * 获取证书模板详情
     */
    public TemplateResponse getTemplateById(String templateId) {
        log.info("获取证书模板详情: {}", templateId);
        
        CertificateTemplate template = templateRepository.selectById(templateId);
        if (template == null) {
            throw new BusinessException("证书模板不存在");
        }
        
        return convertToResponse(template);
    }
    
    /**
     * 分页查询证书模板列表
     */
    public PageResult<TemplateResponse> getTemplateList(Integer page, Integer size, 
                                                         String keyword, String type, 
                                                         Boolean enabled) {
        log.info("分页查询证书模板列表: page={}, size={}, keyword={}, type={}, enabled={}", 
                page, size, keyword, type, enabled);
        
        Page<CertificateTemplate> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CertificateTemplate> wrapper = new LambdaQueryWrapper<>();
        
        // 关键字搜索（模板名称、描述）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(CertificateTemplate::getName, keyword)
                    .or().like(CertificateTemplate::getDescription, keyword));
        }
        
        // 类型筛选
        if (StringUtils.hasText(type)) {
            wrapper.eq(CertificateTemplate::getType, type);
        }
        
        // 启用状态筛选
        if (enabled != null) {
            wrapper.eq(CertificateTemplate::getEnabled, enabled);
        }
        
        wrapper.orderByDesc(CertificateTemplate::getCreateTime);
        
        IPage<CertificateTemplate> templatePage = templateRepository.selectPage(pageParam, wrapper);
        
        List<TemplateResponse> responseList = templatePage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return PageResult.<TemplateResponse>builder()
                .records(responseList)
                .total(templatePage.getTotal())
                .current(templatePage.getCurrent())
                .size(templatePage.getSize())
                .build();
    }
    
    /**
     * 获取所有启用的模板（不分页）
     */
    public List<TemplateResponse> getEnabledTemplates() {
        log.info("获取所有启用的模板");
        
        LambdaQueryWrapper<CertificateTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CertificateTemplate::getEnabled, true);
        wrapper.orderByAsc(CertificateTemplate::getName);
        
        List<CertificateTemplate> templates = templateRepository.selectList(wrapper);
        
        return templates.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据类型获取启用的模板
     */
    public List<TemplateResponse> getEnabledTemplatesByType(String type) {
        log.info("根据类型获取启用的模板: type={}", type);
        
        LambdaQueryWrapper<CertificateTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CertificateTemplate::getType, type);
        wrapper.eq(CertificateTemplate::getEnabled, true);
        wrapper.orderByAsc(CertificateTemplate::getName);
        
        List<CertificateTemplate> templates = templateRepository.selectList(wrapper);
        
        return templates.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 启用/禁用模板
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleTemplateStatus(String templateId, Boolean enabled) {
        log.info("切换模板状态: templateId={}, enabled={}", templateId, enabled);
        
        CertificateTemplate template = templateRepository.selectById(templateId);
        if (template == null) {
            throw new BusinessException("证书模板不存在");
        }
        
        template.setEnabled(enabled);
        template.setUpdateTime(LocalDateTime.now());
        templateRepository.updateById(template);
        
        log.info("模板状态切换成功: templateId={}, enabled={}", templateId, enabled);
    }
    
    /**
     * 转换为响应对象
     */
    private TemplateResponse convertToResponse(CertificateTemplate template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .type(template.getType())
                .content(template.getContent())
                .description(template.getDescription())
                .enabled(template.getEnabled())
                .createTime(template.getCreateTime())
                .updateTime(template.getUpdateTime())
                .build();
    }
}