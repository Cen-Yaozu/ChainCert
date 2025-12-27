package com.blockchain.certificate.domain.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blockchain.certificate.domain.system.model.SystemLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统日志Repository接口
 */
@Mapper
public interface SystemLogRepository extends BaseMapper<SystemLog> {
}
