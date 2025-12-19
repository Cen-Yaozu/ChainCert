package com.blockchain.certificate.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blockchain.certificate.model.entity.Major;
import org.apache.ibatis.annotations.Mapper;

/**
 * 专业Repository接口
 */
@Mapper
public interface MajorRepository extends BaseMapper<Major> {
}
