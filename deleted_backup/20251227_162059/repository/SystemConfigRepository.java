package com.blockchain.certificate.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blockchain.certificate.model.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置Repository接口
 */
@Mapper
public interface SystemConfigRepository extends BaseMapper<SystemConfig> {
}
