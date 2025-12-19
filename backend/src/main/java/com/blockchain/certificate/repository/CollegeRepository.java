package com.blockchain.certificate.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blockchain.certificate.model.entity.College;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学院Repository接口
 */
@Mapper
public interface CollegeRepository extends BaseMapper<College> {
}
