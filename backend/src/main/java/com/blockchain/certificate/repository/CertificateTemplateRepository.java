package com.blockchain.certificate.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blockchain.certificate.model.entity.CertificateTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 证书模板Repository接口
 */
@Mapper
public interface CertificateTemplateRepository extends BaseMapper<CertificateTemplate> {
}
