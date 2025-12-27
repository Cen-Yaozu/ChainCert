package com.blockchain.certificate.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blockchain.certificate.domain.organization.model.Major;
import org.apache.ibatis.annotations.Mapper;

/**
 * 专业Repository接口
 */
@Mapper
public interface MajorRepository extends BaseMapper<Major> {
}
