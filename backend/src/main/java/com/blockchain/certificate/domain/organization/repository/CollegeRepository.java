package com.blockchain.certificate.domain.organization.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blockchain.certificate.domain.organization.model.College;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学院Repository接口
 */
@Mapper
public interface CollegeRepository extends BaseMapper<College> {
}
