package com.blockchain.certificate.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blockchain.certificate.domain.certificate.model.Certificate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 证书Repository接口
 */
@Mapper
public interface CertificateRepository extends BaseMapper<Certificate> {
}
