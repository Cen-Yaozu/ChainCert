package com.blockchain.certificate.domain.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blockchain.certificate.domain.system.model.SystemConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置Repository接口
 */
@Mapper
public interface SystemConfigRepository extends BaseMapper<SystemConfig> {
}
